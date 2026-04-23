package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class BetGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private Integer maxUsers;

    private Integer currentUsers;

    @OneToMany(mappedBy = "betGroup", cascade = CascadeType.ALL)
    private List<Bet> bets;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public BetGroup() {
		// TODO Auto-generated constructor stub
	}
        
	public BetGroup(Long id, User owner, Integer maxUsers, Integer currentUsers, List<Bet> bets,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.owner = owner;
		this.maxUsers = maxUsers;
		this.currentUsers = currentUsers;
		this.bets = bets;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}




	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
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
	public List<Bet> getBets() {
		return bets;
	}
	public void setBets(List<Bet> bets) {
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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "BetGroup [id=" + id + ", owner=" + owner + ", maxUsers=" + maxUsers + ", currentUsers=" + currentUsers
				+ ", bets=" + bets + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
    
    
}