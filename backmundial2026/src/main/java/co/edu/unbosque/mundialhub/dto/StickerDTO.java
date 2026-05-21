package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.mundialhub.model.Rarity;

public class StickerDTO {

    private Long id;
    private String playerName;
    private Rarity rarity;
    private Long teamId;
    private String position;
    private Integer cost;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
	public StickerDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public StickerDTO(Long id, String playerName, Rarity rarity, Long teamId, String position, Integer cost,
			String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.playerName = playerName;
		this.rarity = rarity;
		this.teamId = teamId;
		this.position = position;
		this.cost = cost;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public Rarity getRarity() {
		return rarity;
	}
	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}
	public Long getTeamId() {
		return teamId;
	}
	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}
	public Integer getCost() {
		return cost;
	}
	public void setCost(Integer cost) {
		this.cost = cost;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
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
		return "StickerDTO [id=" + id + ", playerName=" + playerName + ", rarity=" + rarity + ", teamId=" + teamId
				+ ", position=" + position + ", cost=" + cost + ", imageUrl=" + imageUrl + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}


    
    
    
}