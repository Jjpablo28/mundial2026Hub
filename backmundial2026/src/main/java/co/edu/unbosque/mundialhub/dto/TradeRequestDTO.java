package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

/**
 * DTO de TradeSession.
 *
 * Se usa en dos sentidos:
 *  - Respuesta al Frontend cuando crea una sesión (incluye qrBase64 y shortCode).
 *  - Respuesta informativa al consultar el estado de una sesión existente.
 *
 * NO expone el UUID completo en listados públicos para evitar fuerza bruta;
 * se incluye solo cuando el backend lo necesita enviar explícitamente al dueño (Usuario A).
 */
public class TradeRequestDTO {

    /**
     * UUID completo de la sesión.
     * Solo se devuelve al Usuario A cuando acaba de crear la sesión,
     * o internamente para operaciones WebSocket.
     */
    private String uuid;

    /**
     * Código alfanumérico corto de 6 caracteres (ej: "AB3X7Q").
     * Se muestra en pantalla junto al QR para que el Usuario B
     * pueda unirse sin necesidad de escanear.
     */
    private String shortCode;

    /**
     * Imagen del QR en formato Base64.
     * El Frontend la renderiza como:
     *   <img src="data:image/png;base64,{{qrBase64}}">
     * Solo se incluye al crear la sesión; null en consultas de estado.
     */
    private String qrBase64;

    /** ID del usuario anfitrión (quien generó el QR). */
    private Long userAId;

    /** Email del usuario anfitrión. Visible solo para el propio Usuario A. */
    private String userAEmail;

    /**
     * ID del usuario que se unió escaneando/ingresando el código.
     * Nulo mientras la sesión esté en estado PENDING.
     */
    private Long userBId;

    /**
     * Estado actual de la sesión.
     * Valores: PENDING | NEGOTIATING | COMPLETED | CANCELLED
     */
    private String status;

    /** Timestamp de creación de la sesión. */
    private LocalDateTime createdAt;

    // ── Constructores ────────────────────────────────────────────────────────

    /** Constructor vacío para serialización/deserialización. */
    public TradeRequestDTO() {}

    /**
     * Constructor usado al crear una sesión nueva (incluye QR y shortCode).
     *
     * @param uuid      UUID de la sesión.
     * @param shortCode Código alfanumérico corto.
     * @param qrBase64  Imagen QR en Base64.
     * @param userAId   ID del Usuario A.
     * @param userAEmail Email del Usuario A.
     * @param status    Estado inicial ("PENDING").
     * @param createdAt Timestamp de creación.
     */
    public TradeRequestDTO(String uuid, String shortCode, String qrBase64,
                           Long userAId, String userAEmail,
                           String status, LocalDateTime createdAt) {
        this.uuid       = uuid;
        this.shortCode  = shortCode;
        this.qrBase64   = qrBase64;
        this.userAId    = userAId;
        this.userAEmail = userAEmail;
        this.userBId    = null;
        this.status     = status;
        this.createdAt  = createdAt;
    }

    /**
     * Constructor usado al consultar el estado de una sesión existente (sin QR).
     *
     * @param uuid      UUID de la sesión.
     * @param shortCode Código alfanumérico corto.
     * @param userAId   ID del Usuario A.
     * @param userAEmail Email del Usuario A.
     * @param userBId   ID del Usuario B (puede ser nulo).
     * @param status    Estado actual.
     * @param createdAt Timestamp de creación.
     */
    public TradeRequestDTO(String uuid, String shortCode,
                           Long userAId, String userAEmail, Long userBId,
                           String status, LocalDateTime createdAt) {
        this.uuid       = uuid;
        this.shortCode  = shortCode;
        this.qrBase64   = null;
        this.userAId    = userAId;
        this.userAEmail = userAEmail;
        this.userBId    = userBId;
        this.status     = status;
        this.createdAt  = createdAt;
    }

    // ── Método de mapeo estático ─────────────────────────────────────────────

    /**
     * Convierte una entidad TradeSession en su DTO equivalente (sin QR).
     * Útil en el Service para no exponer la entidad JPA directamente al controlador.
     *
     * @param session Entidad a convertir.
     * @return DTO listo para serializar.
     */
    public static TradeRequestDTO fromEntity(co.edu.unbosque.mundialhub.model.TradeSession session) {
        return new TradeRequestDTO(
                session.getUuid(),
                session.getShortCode(),
                session.getUserAId(),
                session.getUserAEmail(),
                session.getUserBId(),
                session.getStatus(),
                session.getCreatedAt()
        );
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

    public String getQrBase64() {
        return qrBase64;
    }

    public void setQrBase64(String qrBase64) {
        this.qrBase64 = qrBase64;
    }

    public Long getUserAId() {
        return userAId;
    }

    public void setUserAId(Long userAId) {
        this.userAId = userAId;
    }

    public String getUserAEmail() {
        return userAEmail;
    }

    public void setUserAEmail(String userAEmail) {
        this.userAEmail = userAEmail;
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
        return "TradeSessionDTO{" +
                "uuid='" + uuid + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", userAId=" + userAId +
                ", userAEmail='" + userAEmail + '\'' +
                ", userBId=" + userBId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}