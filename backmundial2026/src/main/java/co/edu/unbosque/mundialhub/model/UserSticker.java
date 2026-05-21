package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sticker_id"})
	)
public class UserSticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Sticker sticker;

    private Integer quantity; // cuántas tiene

    private LocalDateTime obtainedAt;
    
    
    
	public UserSticker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserSticker(Long id, User user, Sticker sticker, Integer quantity, LocalDateTime obtainedAt) {
		super();
		this.id = id;
		this.user = user;
		this.sticker = sticker;
		this.quantity = quantity;
		this.obtainedAt = obtainedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Sticker getSticker() {
		return sticker;
	}

	public void setSticker(Sticker sticker) {
		this.sticker = sticker;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public LocalDateTime getObtainedAt() {
		return obtainedAt;
	}

	public void setObtainedAt(LocalDateTime obtainedAt) {
		this.obtainedAt = obtainedAt;
	}
    
    
    
}