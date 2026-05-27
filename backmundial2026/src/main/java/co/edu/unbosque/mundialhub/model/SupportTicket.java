package co.edu.unbosque.mundialhub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un ticket de soporte creado por un agente o
 * administrador para gestionar solicitudes manuales sobre usuarios: adición de
 * sobres, tokens, bloqueos de cuenta, restablecimiento de verificación y
 * solicitudes de reembolso.
 */
@Entity
@Table(name = "support_tickets")
public class SupportTicket {

	public enum TicketType {
		ADD_PACKETS, // Acreditar sobres al usuario
		ADD_TOKENS, // Acreditar tokens/puntos
		RESET_VERIFICATION, // Reenviar o reiniciar código de verificación
		UNLOCK_ACCOUNT, // Desbloquear cuenta suspendida
		REVOKE_ACCOUNT, // Suspender / revocar cuenta
		REFUND_TOKENS, // Reembolso de tokens
		CUSTOM // Acción libre con nota
	}

	public enum TicketStatus {
		OPEN, IN_PROGRESS, RESOLVED, REJECTED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Usuario beneficiario de la acción de soporte */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id", nullable = false)
	private User targetUser;

	/** Agente de soporte que crea y gestiona el ticket */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_user_id", nullable = false)
	private User agentUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketType ticketType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketStatus status = TicketStatus.OPEN;

	/** Cantidad aplicable (p.ej. tokens a acreditar, sobres a agregar) */
	@Column
	private Integer quantity;

	/** Descripción libre, motivo o notas internas del agente */
	@Column(length = 500)
	private String notes;

	/** Respuesta o resolución final del ticket */
	@Column(length = 500)
	private String resolution;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime resolvedAt;

	@Column
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	
	

	public SupportTicket(Long id, User targetUser, User agentUser, TicketType ticketType, TicketStatus status,
			Integer quantity, String notes, String resolution, LocalDateTime createdAt, LocalDateTime resolvedAt,
			LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.targetUser = targetUser;
		this.agentUser = agentUser;
		this.ticketType = ticketType;
		this.status = status;
		this.quantity = quantity;
		this.notes = notes;
		this.resolution = resolution;
		this.createdAt = createdAt;
		this.resolvedAt = resolvedAt;
		this.updatedAt = updatedAt;
	}

	// ── Getters y Setters ────────────────────────────────────────────────────

	public SupportTicket() {
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(User targetUser) {
		this.targetUser = targetUser;
	}

	public User getAgentUser() {
		return agentUser;
	}

	public void setAgentUser(User agentUser) {
		this.agentUser = agentUser;
	}

	public TicketType getTicketType() {
		return ticketType;
	}

	public void setTicketType(TicketType ticketType) {
		this.ticketType = ticketType;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(LocalDateTime resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}