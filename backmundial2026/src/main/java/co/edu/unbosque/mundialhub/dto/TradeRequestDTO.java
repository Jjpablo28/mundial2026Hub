package co.edu.unbosque.mundialhub.dto;

import java.util.List;

/**
 * Objeto de Transferencia de Datos (DTO) que transporta la información 
 * de la propuesta de intercambio de múltiples láminas entre dos usuarios.
 */
public class TradeRequestDTO {

    private String sessionUuid;
    private Long fromUserId;
    private Long toUserId;
    private List<Long> fromStickerIds;
    private List<Long> toStickerIds;

    public TradeRequestDTO() {
    }

    public TradeRequestDTO(String sessionUuid, Long fromUserId, Long toUserId, List<Long> fromStickerIds, List<Long> toStickerIds) {
        this.sessionUuid = sessionUuid;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.fromStickerIds = fromStickerIds;
        this.toStickerIds = toStickerIds;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public void setSessionUuid(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public List<Long> getFromStickerIds() {
        return fromStickerIds;
    }

    public void setFromStickerIds(List<Long> fromStickerIds) {
        this.fromStickerIds = fromStickerIds;
    }

    public List<Long> getToStickerIds() {
        return toStickerIds;
    }

    public void setToStickerIds(List<Long> toStickerIds) {
        this.toStickerIds = toStickerIds;
    }
}