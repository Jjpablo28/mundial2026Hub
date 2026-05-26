package co.edu.unbosque.mundialhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import co.edu.unbosque.mundialhub.dto.PaymentRequestDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.Payments;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.repository.PaymentRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;

/**
 * Servicio encargado de gestionar los procesos de pagos, transacciones de Mercado Pago
 * y recarga de tokens/puntos para los usuarios del sistema VeltrixPass.
 * * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
@Service
public class PaymentService implements CRUDOperation<PaymentRequestDTO, Payments> {

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PaymentRepository paymentRepo;

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserStickerService stickerServ;

	@Autowired
	private ActivityLogService activityLogService;

	/**
	 * Crea un registro inicial de pago en la base de datos a partir de su DTO.
	 *
	 * @param data DTO con los datos de la transacción a registrar.
	 * @return El DTO mapeado del objeto persistido.
	 */
	@Override
	@Transactional
	public PaymentRequestDTO create(PaymentRequestDTO data) {
		Payments paymentToSave = modelMapper.map(data, Payments.class);
		Payments savedPayment = paymentRepo.save(paymentToSave);
		return modelMapper.map(savedPayment, PaymentRequestDTO.class);
	}

	/**
	 * Recupera todos los pagos registrados en el sistema.
	 *
	 * @return Lista de DTOs que representan todos los pagos.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PaymentRequestDTO> findAll() {
		return paymentRepo.findAll().stream()
				.map(p -> modelMapper.map(p, PaymentRequestDTO.class))
				.toList();
	}

	/**
	 * Elimina un registro de pago por su ID único.
	 *
	 * @param id Identificador único del pago en base de datos.
	 * @throws Exceptions.PaymentNotFoundException Si el pago con el ID provisto no existe.
	 */
	@Override
	@Transactional
	public void deleteById(Long id) {
		Optional<Payments> found = paymentRepo.findById(id);
		if (found.isEmpty()) {
			throw new Exceptions.PaymentNotFoundException(id);
		}
		paymentRepo.delete(found.get());
	}

	/**
	 * Actualiza los campos de estado y pago de un registro existente.
	 *
	 * @param id      Identificador único del pago a actualizar.
	 * @param newData DTO con los nuevos datos de estado a aplicar.
	 * @return El DTO con la información de pago actualizada.
	 * @throws Exceptions.PaymentNotFoundException Si el pago no se encuentra registrado.
	 */
	@Override
	@Transactional
	public PaymentRequestDTO updateById(Long id, PaymentRequestDTO newData) {
		Optional<Payments> found = paymentRepo.findById(id);
		if (found.isEmpty()) {
			throw new Exceptions.PaymentNotFoundException(id);
		}

		Payments existing = found.get();
		if (newData.getStatus() != null) {
			existing.setStatus(newData.getStatus());
		}
		existing.setPaid(newData.isPaid());
		
		Payments updatedPayment = paymentRepo.save(existing);
		return modelMapper.map(updatedPayment, PaymentRequestDTO.class);
	}

	/**
	 * Cuenta la cantidad total de registros de pagos en el sistema.
	 *
	 * @return Cantidad total de filas en la tabla de pagos.
	 */
	@Override
	@Transactional(readOnly = true)
	public long count() {
		return paymentRepo.count();
	}

	/**
	 * Verifica la existencia de un pago en el sistema mediante su ID.
	 *
	 * @param id Identificador único del pago.
	 * @return true si el pago existe, de lo contrario false.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean exist(Long id) {
		return paymentRepo.existsById(id);
	}

	/**
	 * Crea una preferencia de pago en Mercado Pago para la compra de Tokens/Puntos.
	 *
	 * @param dto      DTO base con datos informativos del monto.
	 * @param usuario  ID numérico del usuario que realiza la compra.
	 * @param quantity Cantidad de tokens solicitados.
	 * @return Objeto {@link Preference} generado por la API externa de Mercado Pago.
	 * @throws Exceptions.UserNotFoundException Si el usuario provisto no existe en el sistema.
	 * @throws Exceptions.ValidationException   Si Mercado Pago rechaza los parámetros o si ocurre una falla inesperada.
	 */
	@Transactional
	public Preference createPayment(PaymentRequestDTO dto, int usuario, int quantity) {
		Optional<User> userFound = userRepo.findById(usuario);

		if (userFound.isEmpty()) {
			throw new Exceptions.UserNotFoundException(String.valueOf(usuario));
		}

		BigDecimal precioUnitarioToken = new BigDecimal("10.00");
		BigDecimal totalCobro = new BigDecimal(quantity).multiply(precioUnitarioToken);
		dto.setAmount(totalCobro);
		dto.setQuantity(quantity);

		Payments savedPayment = this.saveInitialPayment(dto, usuario);

		try {
			PreferenceItemRequest itemRequest = PreferenceItemRequest.builder().id(savedPayment.getId().toString())
					.title("Mundial Hub 2026 - Tokens").quantity(quantity).currencyId("COP").unitPrice(precioUnitarioToken)
					.build();

			Map<String, Object> metadata = new HashMap<>();
			metadata.put("tokens_quantity", quantity);

			PreferenceRequest request = PreferenceRequest.builder()
					.items(Collections.singletonList(itemRequest))
					.metadata(metadata)
					.backUrls(null).build();

			PreferenceClient client = new PreferenceClient();
			Preference preference = client.create(request);

			this.updatePreferenceId(savedPayment.getId(), preference.getId());

			return preference;

		} catch (MPApiException e) {
			String errorMsg = (e.getApiResponse() != null) ? e.getApiResponse().getContent() : "Respuesta vacía de MP";
			System.err.println("DETALLE ERROR API: " + errorMsg);
			this.markAsError(savedPayment.getId(), errorMsg);
			throw new Exceptions.ValidationException("Mercado Pago rechazó la petición: " + errorMsg);
		} catch (Exception e) {
			System.err.println("ERROR CRÍTICO: " + e.getMessage());
			throw new Exceptions.ValidationException("Error inesperado en el proceso de pago");
		}
	}

	/**
	 * Procesa y efectúa la transacción final de cobro con tarjeta/medio de pago a través del Checkout API de Mercado Pago.
	 * Si el estado de la respuesta externa es "approved", se acreditan los tokens correspondientes al usuario.
	 *
	 * @param formData Mapa con los datos estructurados del formulario de pago (token, método, payer, etc.).
	 * @return Objeto {@link Payment} con el estado final procesado por la pasarela de pagos.
	 * @throws Exceptions.ValidationException   Si no se localiza el registro previo mapeado de la preferencia cifrada 
	 * o si ocurre un fallo en el cliente de Mercado Pago.
	 * @throws Exceptions.UserNotFoundException Si el usuario asociado al pago original no existe.
	 */
	@Transactional
	@SuppressWarnings("unchecked")
	public Payment createPayment(Map<String, Object> formData) {
		PaymentClient client = new PaymentClient();

		BigDecimal amount = new BigDecimal(formData.get("transaction_amount").toString());
		String token = (String) formData.get("token");
		String paymentMethodId = (String) formData.get("payment_method_id");
		Integer installments = Integer.parseInt(formData.get("installments").toString());

		String email = "";
		Object payerObj = formData.get("payer");
		if (payerObj instanceof Map<?, ?>) {
			Map<String, Object> payerMap = (Map<String, Object>) payerObj;
			email = (String) payerMap.get("email");
		}

		String preferenceId = (String) formData.get("preference_id");

		int tokensCompradosReal = 0;
		if (formData.get("metadata") instanceof Map) {
			Map<String, Object> metaMap = (Map<String, Object>) formData.get("metadata");
			if (metaMap.containsKey("tokens_quantity")) {
				tokensCompradosReal = Integer.parseInt(metaMap.get("tokens_quantity").toString());
			}
		}

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("my_custom_preference_id", preferenceId);

		PaymentCreateRequest request = PaymentCreateRequest.builder().transactionAmount(amount).token(token)
				.metadata(metadata).description("Compra VeltrixPass").installments(installments)
				.paymentMethodId(paymentMethodId).payer(PaymentPayerRequest.builder().email(email).build()).build();

		Payments miPagoBD = null;

		try {
			Optional<Payments> selectPayment = paymentRepo.findByMpPreferenceId(AESUtil.encrypt(preferenceId));

			if (selectPayment.isEmpty()) {
				throw new Exceptions.ValidationException("No se encontró el registro de pago para la preferencia especificada.");
			}

			miPagoBD = selectPayment.get();
			Optional<User> userPago = userRepo.findById(miPagoBD.getUser().getId());

			if (userPago.isEmpty()) {
				throw new Exceptions.UserNotFoundException(String.valueOf(miPagoBD.getUser().getId()));
			}

			User usuario = userPago.get();

			Payment mpResponse = client.create(request);
			miPagoBD.setStatus(mpResponse.getStatus());
			miPagoBD.setMpPaymentId(mpResponse.getId());
			miPagoBD.setStatusDetail(mpResponse.getStatusDetail());

			if ("approved".equals(mpResponse.getStatus())) {
				miPagoBD.setPaid(true);
				miPagoBD.setPaidAt(LocalDateTime.now());

				if (tokensCompradosReal == 0) {
					tokensCompradosReal = miPagoBD.getQuantity();
				} else {
					miPagoBD.setQuantity(tokensCompradosReal);
				}

				Integer tokensActuales = usuario.getTotalPoints();
				if (tokensActuales == null) {
					tokensActuales = 0;
				}
				usuario.setTotalPoints(tokensActuales + tokensCompradosReal);
				usuario = userRepo.saveAndFlush(usuario);

				activityLogService.log(usuario.getId().longValue(), "PAYMENT_APPROVED",
						"Pago aprobado. Tokens acreditados: " + tokensCompradosReal + ". Nuevo saldo: " + usuario.getTotalPoints());

				miPagoBD.setUser(usuario);
			}

			paymentRepo.saveAndFlush(miPagoBD);
			return mpResponse;

		} catch (MPApiException e) {
			String contentError = (e.getApiResponse() != null) ? e.getApiResponse().getContent() : e.getMessage();
			if (miPagoBD != null) {
				activityLogService.log(miPagoBD.getUser().getId().longValue(), "PAYMENT_DECLINED",
						"Pago rechazado por Mercado Pago: " + contentError);
			}
			throw new Exceptions.ValidationException("Error en Mercado Pago API: " + contentError);
		} catch (Exception e) {
			throw new Exceptions.ValidationException("Error al procesar la creación del pago en el servidor: " + e.getMessage());
		}
	}

	/**
	 * Actualiza de forma asíncrona o por webhook el estado de un pago basándose en su identificador externo cifrado.
	 *
	 * @param mpPaymentId  Identificador de pago de Mercado Pago (se usará cifrado para la búsqueda).
	 * @param status       Nuevo estado de la transacción (ej. approved, pending, rejected).
	 * @param statusDetail Detalle extendido del estado de la transacción.
	 * @throws Exceptions.PaymentNotFoundException Si no se ubica ningún registro de pago con el ID provisto.
	 * @throws Exceptions.ValidationException      Si ocurre un fallo general en la capa de cifrado o base de datos.
	 */
	@Transactional
	public void updatePaymentStatus(String mpPaymentId, String status, String statusDetail) {
		try {
			Optional<Payments> optionalPayment = paymentRepo.findByMpPreferenceId(AESUtil.encrypt(mpPaymentId));

			if (optionalPayment.isEmpty()) {
				throw new Exceptions.PaymentNotFoundException(0L);
			}

			Payments payment = optionalPayment.get();
			payment.setStatus(status);
			payment.setStatusDetail(statusDetail);

			if ("approved".equalsIgnoreCase(status)) {
				payment.setPaid(true);
				payment.setPaidAt(LocalDateTime.now());
			}

			paymentRepo.save(payment);

		} catch (Exception e) {
			if (e instanceof Exceptions.PaymentNotFoundException) {
				throw e;
			}
			throw new Exceptions.ValidationException("Error al actualizar el estado del pago: " + e.getMessage());
		}
	}

	/**
	 * Guarda un registro de pago preliminar con estado latente de espera de fondos en la base de datos.
	 *
	 * @param dto  DTO con la información requerida del pago a crear.
	 * @param user ID del usuario solicitante de la recarga.
	 * @return La entidad {@link Payments} inicializada y guardada.
	 */
	@Transactional
	public Payments saveInitialPayment(PaymentRequestDTO dto, int user) {
		Optional<User> userFound = userRepo.findById(user);

		Payments payment = modelMapper.map(dto, Payments.class);
		payment.setId(null);
		payment.setPaid(false);
		payment.setUser(userFound.get());
		payment.setStatus("waiting_payment");
		return paymentRepo.save(payment);
	}

	/**
	 * Asocia de forma segura el ID de una preferencia generada en Mercado Pago al registro interno de base de datos.
	 *
	 * @param id     ID del registro interno de pago.
	 * @param prefId ID de preferencia de la API externa (se guarda encriptado mediante AES).
	 */
	@Transactional
	public void updatePreferenceId(Long id, String prefId) {
		paymentRepo.findById(id).ifPresent(p -> {
			p.setMpPreferenceId(AESUtil.encrypt(prefId));
			paymentRepo.save(p);
		});
	}

	/**
	 * Marca de manera explícita un registro de transacción indicando un fallo en las llamadas de API de la pasarela.
	 *
	 * @param id    ID del registro de pago interno.
	 * @param error Detalle del mensaje de error de rechazo.
	 */
	@Transactional
	public void markAsError(Long id, String error) {
		paymentRepo.findById(id).ifPresent(p -> {
			p.setStatus("error_api");
			p.setStatusDetail(error);
			paymentRepo.save(p);
		});
	}
}