package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName; // nombre del jugador

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @ManyToOne
    private Team team;

    private Integer cost; // valor de venta o compra

    private String imageUrl;
    
    private String position;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

	public Sticker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Sticker(Long id, String playerName, Rarity rarity, Team team,String position, Integer cost, String imageUrl,
			LocalDateTime createdAt,LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.playerName = playerName;
		this.rarity = rarity;
		this.team = team;
		this.position=position;
		this.cost = cost;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt=updatedAt;
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

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "Sticker [id=" + id + ", playerName=" + playerName + ", rarity=" + rarity + ", team=" + team + ", cost="
				+ cost + ", imageUrl=" + imageUrl + ", position=" + position + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}



    
    
}
