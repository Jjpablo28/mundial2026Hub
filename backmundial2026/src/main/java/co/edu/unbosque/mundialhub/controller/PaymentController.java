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

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082" })
@Tag(name = "Payments", description = "Payments")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@PostMapping("/preference")
	public ResponseEntity<?> createPreference(
			@RequestBody PaymentRequestDTO dto,
			@RequestParam() int usuario) {
		
		if (dto.getQuantity() == null || dto.getQuantity() < 500) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "La cantidad mínima de compra son 500 tokens."));
		}
		
		java.math.BigDecimal precioPorToken = new java.math.BigDecimal("10");
		java.math.BigDecimal totalAmount = java.math.BigDecimal.valueOf(dto.getQuantity()).multiply(precioPorToken);
		
		dto.setAmount(totalAmount);
		
		Preference preference = paymentService.createPayment(dto, usuario, dto.getQuantity());
		
		Map<String, Object> response = Map.of(
			"id", preference.getId(),         
			"amount", totalAmount.doubleValue()
		);

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	@PostMapping("/charge")
	public ResponseEntity<Payment> processDirectCharge(@RequestBody Map<String, Object> formData) throws Exception {
		Payment payment = paymentService.createPayment(formData);
		return new ResponseEntity<>(payment, HttpStatus.OK);
	}



	@PostMapping
	public ResponseEntity<PaymentRequestDTO> create(@RequestBody PaymentRequestDTO data) {
		PaymentRequestDTO created = paymentService.create(data);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<PaymentRequestDTO>> findAll() {
		List<PaymentRequestDTO> list = paymentService.findAll();
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PaymentRequestDTO> updateById(@PathVariable() Long id, @RequestBody PaymentRequestDTO data) {
		PaymentRequestDTO updated = paymentService.updateById(id, data);
		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable() Long id) {
		paymentService.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}