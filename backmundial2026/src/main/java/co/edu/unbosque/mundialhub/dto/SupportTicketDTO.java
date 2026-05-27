package co.edu.unbosque.mundialhub.dto;

import co.edu.unbosque.mundialhub.model.SupportTicket.TicketStatus;
import co.edu.unbosque.mundialhub.model.SupportTicket.TicketType;

import java.time.LocalDateTime;

/**
 * DTO de transferencia para los tickets de soporte. Se usa tanto para creación
 * (entrada) como para respuestas (salida).
 */
public class SupportTicketDTO {

	private Long id;

	/** ID del usuario que recibirá la acción */
	private Long targetUserId;

	/** Nombre de usuario destino (solo lectura, para mostrar en UI) */
	private String targetUserName;

	/** ID del agente que crea el ticket (usualmente extraído del JWT) */
	private Long agentUserId;

	/** Nombre del agente (solo lectura, para mostrar en UI) */
	private String agentUserName;

	private TicketType ticketType;

	private TicketStatus status;

	/**
	 * Cantidad relevante para la acción: - ADD_PACKETS: número de sobres a
	 * acreditar - ADD_TOKENS / REFUND_TOKENS: tokens a acreditar o reembolsar -
	 * Para otros tipos puede ser null
	 */
	private Integer quantity;

	private String notes;

	private String resolution;

	private LocalDateTime createdAt;

	private LocalDateTime resolvedAt;

	private LocalDateTime updatedAt;
	
	
	

	public SupportTicketDTO(Long id, Long targetUserId, String targetUserName, Long agentUserId, String agentUserName,
			TicketType ticketType, TicketStatus status, Integer quantity, String notes, String resolution,
			LocalDateTime createdAt, LocalDateTime resolvedAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.targetUserId = targetUserId;
		this.targetUserName = targetUserName;
		this.agentUserId = agentUserId;
		this.agentUserName = agentUserName;
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

	public SupportTicketDTO() {
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId) {
		this.targetUserId = targetUserId;
	}

	public String getTargetUserName() {
		return targetUserName;
	}

	public void setTargetUserName(String targetUserName) {
		this.targetUserName = targetUserName;
	}

	public Long getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(Long agentUserId) {
		this.agentUserId = agentUserId;
	}

	public String getAgentUserName() {
		return agentUserName;
	}

	public void setAgentUserName(String agentUserName) {
		this.agentUserName = agentUserName;
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