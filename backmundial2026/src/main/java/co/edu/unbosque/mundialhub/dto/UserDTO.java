package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

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
    private String favoriteTeam;
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
			LocalDateTime verificationExpires, String password, Role role, String favoriteTeam, LocalDateTime createdAt,
			LocalDateTime updatedAt, LocalDateTime lastLogin) {
		super();
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.identification = identification;
		this.code = code;
		this.verificationExpires = verificationExpires;
		this.password = password;
		this.role = role;
		this.favoriteTeam = favoriteTeam;
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
    
    
}