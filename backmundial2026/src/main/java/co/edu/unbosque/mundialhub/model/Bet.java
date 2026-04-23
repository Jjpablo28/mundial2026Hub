package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bet_group_id")
    private BetGroup betGroup;

    // ID del partido (lo traes de la API)
    private Long matchId;

    private Integer homeScore;
    private Integer awayScore;

    private Integer points;

    private LocalDateTime createdAt;
    
    
    public Bet() {
		// TODO Auto-generated constructor stub
	}


	public Bet(Long id, User user, BetGroup betGroup, Long matchId, Integer homeScore, Integer awayScore,
			Integer points, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.user = user;
		this.betGroup = betGroup;
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


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public BetGroup getBetGroup() {
		return betGroup;
	}


	public void setBetGroup(BetGroup betGroup) {
		this.betGroup = betGroup;
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
		return "Bet [id=" + id + ", user=" + user + ", betGroup=" + betGroup + ", matchId=" + matchId + ", homeScore="
				+ homeScore + ", awayScore=" + awayScore + ", points=" + points + ", createdAt=" + createdAt + "]";
	}
    
   
}