package co.edu.unbosque.mundialhub.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import co.edu.unbosque.mundialhub.dto.PaymentRequestDTO;
import co.edu.unbosque.mundialhub.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de gestionar los endpoints relacionados con las
 * transacciones financieras, pasarelas de pago y la adquisición de
 * tokens/créditos de la plataforma MundialHub.
 * <p>
 * Centraliza la comunicación con la API de Mercado Pago, permitiendo tanto la
 * creación de preferencias de pago para el flujo interactivo (Checkout Pro)
 * como el procesamiento de cobros directos, además de proveer los métodos CRUD
 * estándar para auditoría e historial técnico de peticiones de pago.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 * @see PaymentService
 */
@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082",
		"https://mundial2026hub.netlify.app" })
@Tag(name = "Payments", description = "Payments")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	/**
	 * Crea una preferencia de pago (Checkout Pro) en Mercado Pago para la compra de
	 * tokens internos.
	 * <p>
	 * Aplica una restricción de negocio obligatoria donde la cantidad mínima
	 * permitida para la adquisición es de 500 tokens. Calcula el monto final
	 * utilizando operaciones matemáticas exactas con {@link java.math.BigDecimal}
	 * basándose en una tarifa estática de 10 unidades monetarias por token, e
	 * interactúa con el servicio para registrar la orden asociada al ID de usuario
	 * correspondiente.
	 * </p>
	 *
	 * @param dto     Objeto de transferencia de datos (DTO) que contiene los
	 *                parámetros de la solicitud de pago.
	 * @param usuario Identificador único (ID numérico) del usuario que realiza la
	 *                transacción.
	 * @return Un objeto {@link ResponseEntity} que contiene el ID de la preferencia
	 *         generada por Mercado Pago y el monto final calculado, bajo el estado
	 *         HTTP 201 (Created), o un error 400 (Bad Request) si no cumple la
	 *         cuota mínima.
	 */
	@PostMapping("/preference")
	public ResponseEntity<?> createPreference(@RequestBody PaymentRequestDTO dto, @RequestParam() int usuario) {

		// Validación de regla de negocio: compra mínima de tokens
		if (dto.getQuantity() == null || dto.getQuantity() < 500) {
			return ResponseEntity.badRequest().body(Map.of("error", "La cantidad mínima de compra son 500 tokens."));
		}

		// Multiplicación exacta para evitar pérdida de precisión de punto flotante en
		// transacciones
		java.math.BigDecimal precioPorToken = new java.math.BigDecimal("10");
		java.math.BigDecimal totalAmount = java.math.BigDecimal.valueOf(dto.getQuantity()).multiply(precioPorToken);

		dto.setAmount(totalAmount);

		Preference preference = paymentService.createPayment(dto, usuario, dto.getQuantity());

		Map<String, Object> response = Map.of("id", preference.getId(), "amount", totalAmount.doubleValue());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	/**
	 * Procesa un cargo directo a través de la pasarela de pagos utilizando la
	 * información estructurada del formulario.
	 *
	 * @param formData Mapa genérico que recopila los datos de la tarjeta, el
	 *                 pagador y el token de Mercado Pago.
	 * @return Un objeto {@link ResponseEntity} con la entidad {@link Payment}
	 *         procesada y el estado HTTP 200 (OK).
	 * @throws Exception Si ocurre un error técnico de comunicación, firma o fondos
	 *                   insuficientes en la API de Mercado Pago.
	 */
	@PostMapping("/charge")
	public ResponseEntity<Payment> processDirectCharge(@RequestBody Map<String, Object> formData) throws Exception {
		Payment payment = paymentService.createPayment(formData);
		return new ResponseEntity<>(payment, HttpStatus.OK);
	}

	/**
	 * Crea de forma manual un registro local o histórico de solicitud de pago en la
	 * base de datos del sistema.
	 *
	 * @param data Estructura DTO con la información de la solicitud a persistir.
	 * @return Un objeto {@link ResponseEntity} con el DTO guardado y el estado HTTP
	 *         201 (Created).
	 */
	@PostMapping
	public ResponseEntity<PaymentRequestDTO> create(@RequestBody PaymentRequestDTO data) {
		PaymentRequestDTO created = paymentService.create(data);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	/**
	 * Recupera el historial completo de solicitudes de pago registradas en el
	 * backend de la aplicación.
	 *
	 * @return Un objeto {@link ResponseEntity} con el listado completo de
	 *         estructuras DTO y el estado HTTP 200 (OK).
	 */
	@GetMapping
	public ResponseEntity<List<PaymentRequestDTO>> findAll() {
		List<PaymentRequestDTO> list = paymentService.findAll();
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	/**
	 * Actualiza los datos de una solicitud de pago específica localizando el
	 * registro mediante su identificador único.
	 *
	 * @param id   Identificador numérico (llave primaria) de la transacción a
	 *             modificar.
	 * @param data Objeto con las nuevas propiedades técnicas a sobreescribir.
	 * @return Un objeto {@link ResponseEntity} con el DTO actualizado y el estado
	 *         HTTP 200 (OK).
	 */
	@PutMapping("/{id}")
	public ResponseEntity<PaymentRequestDTO> updateById(@PathVariable() Long id, @RequestBody PaymentRequestDTO data) {
		PaymentRequestDTO updated = paymentService.updateById(id, data);
		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	/**
	 * Elimina de manera lógica o física una solicitud de pago del sistema
	 * utilizando su identificador único.
	 *
	 * @param id Identificador numérico de la transacción que se desea remover.
	 * @return Un objeto {@link ResponseEntity} vacío con el estado HTTP 24 (No
	 *         Content).
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable() Long id) {
		paymentService.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}