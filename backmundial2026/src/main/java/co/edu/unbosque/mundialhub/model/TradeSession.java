package co.edu.unbosque.mundialhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la sesión temporal de intercambio de láminas.
 * Se crea cuando el Usuario A genera un QR y se destruye (COMPLETED/CANCELLED)
 * una vez finalizado el proceso.
 *
 * Modos de unión soportados para el Usuario B:
 *  - Escanear el QR       → usa el UUID completo
 *  - Código alfanumérico  → usa shortCode (ej: "AB3X7Q")
 *  - Correo del Usuario A → usa userAEmail + búsqueda de sesión PENDING
 */
@Entity
@Table(name = "tradeSession")
public class TradeSession {

    /**
     * UUID generado por la API externa de QR.
     * Es la clave primaria de la sesión y el contenido codificado dentro del QR.
     */
    @Id
    private String uuid;

    /**
     * Código alfanumérico corto de 6 caracteres (ej: "AB3X7Q").
     * Se genera localmente al crear la sesión. Único en la tabla.
     * Usa solo caracteres sin ambigüedad visual (sin O, 0, I, 1).
     */
    @Column(unique = true, nullable = false, length = 6)
    private String shortCode;

    /**
     * Email del Usuario A (quien genera el QR).
     * Se guarda para permitir que el Usuario B se una usando el correo del anfitrión.
     */
    @Column(nullable = false)
    private String userAEmail;

    /** ID del usuario que generó la sesión (anfitrión). */
    @Column(nullable = false)
    private Long userAId;

    /**
     * ID del usuario que se une escaneando / ingresando el código.
     * Nulo hasta que el Usuario B se conecte.
     */
    private Long userBId;

    /**
     * Estado del ciclo de vida de la sesión.
     * Valores posibles: PENDING → NEGOTIATING → COMPLETED | CANCELLED
     *
     *  PENDING     : Sala creada, esperando que B se una.
     *  NEGOTIATING : Ambas partes conectadas, negociando láminas.
     *  COMPLETED   : Intercambio ejecutado correctamente.
     *  CANCELLED   : Sesión cancelada por tiempo o por alguna parte.
     */
    @Column(nullable = false)
    private String status;

    /** Timestamp de creación. Se inicializa automáticamente en el constructor. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ── Constructores ────────────────────────────────────────────────────────

    /** Constructor vacío requerido por JPA. Inicializa createdAt automáticamente. */
    public TradeSession() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor completo para crear una sesión con todos los campos obligatorios.
     *
     * @param uuid      UUID proveniente de la API externa de QR.
     * @param shortCode Código alfanumérico corto generado localmente.
     * @param userAId   ID del usuario anfitrión.
     * @param userAEmail Email del usuario anfitrión.
     * @param status    Estado inicial (normalmente "PENDING").
     */
    public TradeSession(String uuid, String shortCode, Long userAId, String userAEmail, String status) {
        this.uuid      = uuid;
        this.shortCode = shortCode;
        this.userAId   = userAId;
        this.userAEmail = userAEmail;
        this.userBId   = null;
        this.status    = status;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters y Setters ────────────────────────────────────────────────────

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getUserAEmail() {
        return userAEmail;
    }

    public void setUserAEmail(String userAEmail) {
        this.userAEmail = userAEmail;
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

    // ── toString para logs ───────────────────────────────────────────────────

    @Override
    public String toString() {
        return "TradeSession{" +
                "uuid='" + uuid + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", userAEmail='" + userAEmail + '\'' +
                ", userAId=" + userAId +
                ", userBId=" + userBId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}