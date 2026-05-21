package co.edu.unbosque.mundialhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class PaymentRequestDTO {
	private Long id;

	private String mpPreferenceId;

	private Long mpPaymentId;

	private String status;

	private String statusDetail;

	private BigDecimal amount;

	private String currency;

	private Boolean paid;

	private LocalDateTime paidAt;


	private Integer quantity;

	public PaymentRequestDTO() {
	}

	public PaymentRequestDTO(Long id, String mpPreferenceId, Long mpPaymentId, String status, String statusDetail,
			BigDecimal amount, String currency, Boolean paid, LocalDateTime paidAt,  Integer quantity) {
		super();
		this.id = id;
		this.mpPreferenceId = mpPreferenceId;
		this.mpPaymentId = mpPaymentId;
		this.status = status;
		this.statusDetail = statusDetail;
		this.amount = amount;
		this.currency = currency;
		this.paid = paid;
		this.paidAt = paidAt;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMpPreferenceId() {
		return mpPreferenceId;
	}

	public void setMpPreferenceId(String mpPreferenceId) {
		this.mpPreferenceId = mpPreferenceId;
	}

	public Long getMpPaymentId() {
		return mpPaymentId;
	}

	public void setMpPaymentId(Long mpPaymentId) {
		this.mpPaymentId = mpPaymentId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusDetail() {
		return statusDetail;
	}

	public void setStatusDetail(String statusDetail) {
		this.statusDetail = statusDetail;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Boolean isPaid() {
		return paid;
	}

	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}


	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}
