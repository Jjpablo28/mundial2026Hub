package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

public class BetDTO {

	private Long id;
	private Long userId; // Solo el ID del usuario
	private Long betGroupId; // Solo el ID del grupo (puede ser null)
	private Long matchId;
	private Integer homeScore;
	private Integer awayScore;
	private Integer points;
	private LocalDateTime createdAt;

	public BetDTO() {
	}

	public BetDTO(Long id, Long userId, Long betGroupId, Long matchId, Integer homeScore, Integer awayScore,
			Integer points, LocalDateTime createdAt) {
		this.id = id;
		this.userId = userId;
		this.betGroupId = betGroupId;
		this.matchId = matchId;
		this.homeScore = homeScore;
		this.awayScore = awayScore;
		this.points = points;
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

	public Long getBetGroupId() {
		return betGroupId;
	}

	public void setBetGroupId(Long betGroupId) {
		this.betGroupId = betGroupId;
	}

	public Long getMatchId() {
		return matchId;
	}

	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}

	public Integer getHomeScore() {
		return homeScore;
	}

	public void setHomeScore(Integer homeScore) {
		this.homeScore = homeScore;
	}

	public Integer getAwayScore() {
		return awayScore;
	}

	public void setAwayScore(Integer awayScore) {
		this.awayScore = awayScore;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "BetDTO [id=" + id + ", userId=" + userId + ", betGroupId=" + betGroupId + ", matchId=" + matchId
				+ ", homeScore=" + homeScore + ", awayScore=" + awayScore + ", points=" + points + ", createdAt="
				+ createdAt + "]";
	}
	
	
	
}