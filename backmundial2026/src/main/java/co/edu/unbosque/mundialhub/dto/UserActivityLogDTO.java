package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

public class UserActivityLogDTO {
	private Long id;
	private Long userId;
	private String userName;
	private String eventType;
	private String detail;
	private LocalDateTime createdAt;

	public UserActivityLogDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserActivityLogDTO(Long id, Long userId, String userName, String eventType, String detail,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.userName = userName;
		this.eventType = eventType;
		this.detail = detail;
		this.createdAt = createdAt;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
