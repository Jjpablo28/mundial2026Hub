package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.mundialhub.model.GroupName;


public class TeamDTO {

   
    private String name;

    private String shortName; // Ej: COL, ARG

    private String crest; // URL del escudo

    private String country;

    private GroupName groupName; // Grupo A, B, C...

    private Integer fifaRanking;

    private Integer points; // útil para tablas

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    

	public TeamDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public TeamDTO(String name, String shortName, String crest, String country, GroupName groupName, Integer fifaRanking,
			Integer points, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.name = name;
		this.shortName = shortName;
		this.crest = crest;
		this.country = country;
		this.groupName = groupName;
		this.fifaRanking = fifaRanking;
		this.points = points;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getCrest() {
		return crest;
	}

	public void setCrest(String crest) {
		this.crest = crest;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public GroupName getGroupName() {
		return groupName;
	}

	public void setGroupName(GroupName groupName) {
		this.groupName = groupName;
	}

	public Integer getFifaRanking() {
		return fifaRanking;
	}

	public void setFifaRanking(Integer fifaRanking) {
		this.fifaRanking = fifaRanking;
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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}


	@Override
	public String toString() {
		return "TeamDTO [name=" + name + ", shortName=" + shortName + ", crest=" + crest + ", country=" + country
				+ ", groupName=" + groupName + ", fifaRanking=" + fifaRanking + ", points=" + points + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}
    
    
}
