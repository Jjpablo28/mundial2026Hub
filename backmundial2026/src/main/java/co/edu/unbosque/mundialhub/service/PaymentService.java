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

	@Override
	public PaymentRequestDTO create(PaymentRequestDTO data) {
		Payments paymentToSave = modelMapper.map(data, Payments.class);
		Payments savedPayment = paymentRepo.save(paymentToSave);
		return modelMapper.map(savedPayment, PaymentRequestDTO.class);
	}

	@Override
	public List<PaymentRequestDTO> findAll() {
		return paymentRepo.findAll().stream()
				.map(p -> modelMapper.map(p, PaymentRequestDTO.class))
				.toList();
	}

	@Override
	public void deleteById(Long id) {
		Optional<Payments> found = paymentRepo.findById(id);
		if (found.isEmpty()) {
			throw new Exceptions.PaymentNotFoundException(id);
		}
		paymentRepo.delete(found.get());
	}

	@Override
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

	@Override
	public long count() {
		return paymentRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return paymentRepo.existsById(id);
	}

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

			// PASAR LA QUANTITY REAL DENTRO DE LOS METADATOS DE LA PREFERENCIA
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
	@Transactional
	@SuppressWarnings("unchecked")
	public Payment createPayment(Map<String, Object> formData) throws Exception {
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

		// LEER LA CANTIDAD DIRECTAMENTE DESDE LOS METADATOS DEL FORMULARIO DE ANGULAR
		int tokensCompradosReal = 0;
		if (formData.get("metadata") instanceof Map) {
			Map<String, Object> metaMap = (Map<String, Object>) formData.get("metadata");
			if (metaMap.containsKey("tokens_quantity")) {
				tokensCompradosReal = Integer.parseInt(metaMap.get("tokens_quantity").toString());
			}
		}
		
		// Si el front no lo mapeó en la raíz, lo intentamos recuperar del DTO mapeado por el historial de base de datos
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("my_custom_preference_id", preferenceId);

		PaymentCreateRequest request = PaymentCreateRequest.builder().transactionAmount(amount).token(token)
				.metadata(metadata).description("Compra VeltrixPass").installments(installments)
				.paymentMethodId(paymentMethodId).payer(PaymentPayerRequest.builder().email(email).build()).build();

		try {
			Optional<Payments> selectPayment = paymentRepo.findByMpPreferenceId(AESUtil.encrypt(preferenceId));

			if (selectPayment.isEmpty()) {
				throw new Exceptions.ValidationException("No se encontró el registro de pago");
			}

			Payment mpResponse = client.create(request);
			Payments miPagoBD = selectPayment.get();
			Optional<User> userPago = userRepo.findById(miPagoBD.getUser().getId());
			
			if (userPago.isEmpty()) {
				throw new Exceptions.UserNotFoundException(String.valueOf(miPagoBD.getUser().getId()));
			}

			User usuario = userPago.get();
			miPagoBD.setStatus(mpResponse.getStatus());
			miPagoBD.setMpPaymentId(mpResponse.getId());
			miPagoBD.setStatusDetail(mpResponse.getStatusDetail());

			if ("approved".equals(mpResponse.getStatus())) {
				miPagoBD.setPaid(true);
				miPagoBD.setPaidAt(LocalDateTime.now());

				// Si falló la lectura del mapa, usamos el valor seguro de la base de datos pre-guardado
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
				miPagoBD.setUser(usuario);


			}

			paymentRepo.saveAndFlush(miPagoBD);
			return mpResponse;
		} catch (MPApiException e) {
			throw new Exceptions.ValidationException(e.getApiResponse().getContent());
		}
	}

	public int updatePaymentStatus(String mpPaymentId, String status, String statusDetail) {
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
			return 0;

		} catch (Exception e) {
			if (e instanceof Exceptions.PaymentNotFoundException) {
				throw e;
			}
			return 1;
		}
	}

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

	@Transactional
	public void updatePreferenceId(Long id, String prefId) {
		paymentRepo.findById(id).ifPresent(p -> {
			p.setMpPreferenceId(AESUtil.encrypt(prefId));
			paymentRepo.save(p);
		});
	}

	@Transactional
	public void markAsError(Long id, String error) {
		paymentRepo.findById(id).ifPresent(p -> {
			p.setStatus("error_api");
			p.setStatusDetail(error);
			paymentRepo.save(p);
		});
	}
}