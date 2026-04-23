package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BetGroupDTO {

	private Long id;
	private Long ownerId; // Solo el ID del creador
	private String ownerName; // Extra: Útil para mostrar en el frontend rápido
	private Integer maxUsers;
	private Integer currentUsers;
	private List<BetDTO> bets; // Lista de DTOs, NO de Entidades
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public BetGroupDTO() {
	}

	public BetGroupDTO(Long id, Long ownerId, String ownerName, Integer maxUsers, Integer currentUsers,
			List<BetDTO> bets, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.ownerId = ownerId;
		this.ownerName = ownerName;
		this.maxUsers = maxUsers;
		this.currentUsers = currentUsers;
		this.bets = bets;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public Integer getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(Integer maxUsers) {
		this.maxUsers = maxUsers;
	}

	public Integer getCurrentUsers() {
		return currentUsers;
	}

	public void setCurrentUsers(Integer currentUsers) {
		this.currentUsers = currentUsers;
	}

	public List<BetDTO> getBets() {
		return bets;
	}

	public void setBets(List<BetDTO> bets) {
		this.bets = bets;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "BetGroupDTO [id=" + id + ", ownerId=" + ownerId + ", ownerName=" + ownerName + ", maxUsers=" + maxUsers
				+ ", currentUsers=" + currentUsers + ", bets=" + bets + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
	
	
}