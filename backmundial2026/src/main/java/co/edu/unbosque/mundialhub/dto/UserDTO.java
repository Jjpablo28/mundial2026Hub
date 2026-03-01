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

    public UserDTO() {
    }

    
    public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	public UserDTO( String userName, String name, String email, String identification,String code,LocalDateTime time, String password,
			Role role) {
		super();
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.identification = identification;
		this.verificationExpires=time;
		this.code=code;
		this.password = password;
		this.role = role;
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
    
    
}