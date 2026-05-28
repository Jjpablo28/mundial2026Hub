package co.edu.unbosque.mundialhub.dto;

import java.util.List;

/**
 * DTO que transporta la propuesta de intercambio de láminas entre dos usuarios.
 * Es el payload que el Frontend envía al WebSocket en /app/trade/propose.
 */
public class TradeRequestDTO {

	/** UUID de la sesión de intercambio activa. */
	private String sessionUuid;

	/** ID del usuario que propone el intercambio (quien envía). */
	private Long fromUserId;

	/** ID del usuario que recibe la propuesta. */
	private Long toUserId;

	/** IDs de las láminas que fromUser entrega al toUser. */
	private List<Long> fromStickerIds;

	/** IDs de las láminas que fromUser solicita al toUser a cambio. */
	private List<Long> toStickerIds;


	public TradeRequestDTO() {
	}

	public TradeRequestDTO(String sessionUuid, Long fromUserId, Long toUserId, List<Long> fromStickerIds,
			List<Long> toStickerIds) {
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

	@Override
	public String toString() {
		return "TradeRequestDTO{" + "sessionUuid='" + sessionUuid + '\'' + ", fromUserId=" + fromUserId + ", toUserId="
				+ toUserId + ", fromStickerIds=" + fromStickerIds + ", toStickerIds=" + toStickerIds + '}';
	}
}