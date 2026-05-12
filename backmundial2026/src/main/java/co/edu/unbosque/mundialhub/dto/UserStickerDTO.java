package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

public class UserStickerDTO {

    private Long id;
    
    // Usamos IDs para facilitar el envío desde el frontend
    private Long userId;
    
    private Long stickerId;

    private Integer quantity;

    private LocalDateTime obtainedAt;

    public UserStickerDTO() {
        super();
    }

    public UserStickerDTO(Long id, Long userId, Long stickerId, Integer quantity, LocalDateTime obtainedAt) {
        super();
        this.id = id;
        this.userId = userId;
        this.stickerId = stickerId;
        this.quantity = quantity;
        this.obtainedAt = obtainedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStickerId() {
        return stickerId;
    }

    public void setStickerId(Long stickerId) {
        this.stickerId = stickerId;
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

    @Override
    public String toString() {
        return "UserStickerDTO [id=" + id + ", userId=" + userId + ", stickerId=" + stickerId + ", quantity=" + quantity
                + ", obtainedAt=" + obtainedAt + "]";
    }
}