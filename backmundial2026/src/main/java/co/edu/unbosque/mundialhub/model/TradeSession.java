package co.edu.unbosque.mundialhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad que representa la sala temporal o sesión de intercambio 
 * creada a partir del escaneo de un código QR.
 */
@Entity
@Table(name = "tradeSession")
public class TradeSession {

    @Id
    private String uuid;
    
    private Long userAId;
    private Long userBId;
    
    // Estados posibles: PENDING, NEGOTIATING, COMPLETED, CANCELLED
    private String status; 
    
    private LocalDateTime createdAt;

    public TradeSession() {
        this.createdAt = LocalDateTime.now();
    }

    public TradeSession(String uuid, Long userAId, Long userBId, String status) {
        this.uuid = uuid;
        this.userAId = userAId;
        this.userBId = userBId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getUserAId() {
        return userAId;
    }

    public void setUserAId(Long userAId) {
        this.userAId = userAId;
    }

    public Long getUserBId() {
        return userBId;
    }

    public void setUserBId(Long userBId) {
        this.userBId = userBId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}