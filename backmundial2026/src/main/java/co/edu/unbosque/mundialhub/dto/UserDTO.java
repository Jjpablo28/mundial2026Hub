package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;
import java.util.List;


import co.edu.unbosque.mundialhub.model.User.Role;


/**
 * DTO para la transferencia de datos del usuario.
 * Se utiliza principalmente como DTO de respuesta, ocultando la contraseña
 * y simplificando las relaciones.
 */
public class UserDTO {

    private Long id;
    private String userName;
    private String name;
    private String email;
    private String identification;
    private String code;
    private LocalDateTime verificationExpires;
    private String password;
    private Role role; 
    private String avatarUrl;
	private Integer totalPoints;
    private Integer packets;
    private String country;
    private String favoriteTeam;
    private String city;
	


    
    private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime lastLogin;
	

    public UserDTO() {
    }

    
    public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}




	public UserDTO(String userName, String name, String email, String identification, String code,
			LocalDateTime verificationExpires, String password, Role role, String avatarUrl, Integer totalPoints,Integer packets, 
			String country, String favoriteTeam, String city,
			LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin) {
		super();
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.identification = identification;
		this.code = code;
		this.verificationExpires = verificationExpires;
		this.password = password;
		this.role = role;
		this.avatarUrl = avatarUrl;
		this.totalPoints = totalPoints;
		this.country = country;
		this.favoriteTeam = favoriteTeam;
		this.city = city;
		this.packets = packets;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastLogin = lastLogin;
	}


	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }




	public String getCode() {
		return code;
	}




	public void setCode(String code) {
		this.code = code;
	}




	public LocalDateTime getVerificationExpires() {
		return verificationExpires;
	}




	public void setVerificationExpires(LocalDateTime verificationExpires) {
		this.verificationExpires = verificationExpires;
	}


	public String getFavoriteTeam() {
		return favoriteTeam;
	}


	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
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


	public LocalDateTime getLastLogin() {
		return lastLogin;
	}


	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}


	public String getCountry() {
		return country;
	}


	public void setCountry(String country) {
		this.country = country;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getAvatarUrl() {
		return avatarUrl;
	}


	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	
	

	public Integer getTotalPoints() {
		return totalPoints;
	}


	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}



	public Integer getPackets() {
		return packets;
	}


	public void setPackets(Integer packets) {
		this.packets = packets;
	}


	@Override
	public String toString() {
		return "UserDTO [id=" + id + ", userName=" + userName + ", name=" + name + ", email=" + email
				+ ", identification=" + identification + ", code=" + code + ", verificationExpires="
				+ verificationExpires + ", password=" + password + ", role=" + role + ", avatarUrl=" + avatarUrl
				+ ", totalPoints=" + totalPoints + ", packets=" + packets + ", country=" + country + ", favoriteTeam="
				+ favoriteTeam + ", city=" + city + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ ", lastLogin=" + lastLogin + "]";
	}


	    
}