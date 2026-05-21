package co.edu.unbosque.mundialhub.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payments {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mp_preference_id", unique = true)
	private String mpPreferenceId;

	@Column(name = "mp_payment_id")
	private Long mpPaymentId;
	
	@Column(name = "quantity")
	private int quantity;

	@Column(name = "status")
	private String status;

	@Column(name = "status_detail")
	private String statusDetail;

	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "currency")
	private String currency;

	@Column(name = "paid")
	private boolean paid;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;


	@ManyToOne
	private User user;


	


	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.paid = false;
		this.status = "pending";
	}

	public Payments() {
		// TODO Auto-generated constructor stub
	}





	public Payments(Long id, String mpPreferenceId, Long mpPaymentId, int quantity, String status, String statusDetail,
			BigDecimal amount, String currency, boolean paid, LocalDateTime paidAt, User user, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.mpPreferenceId = mpPreferenceId;
		this.mpPaymentId = mpPaymentId;
		this.quantity = quantity;
		this.status = status;
		this.statusDetail = statusDetail;
		this.amount = amount;
		this.currency = currency;
		this.paid = paid;
		this.paidAt = paidAt;
		this.user = user;
		this.createdAt = createdAt;
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

	public boolean isPaid() {
		return paid;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}



	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
