package co.edu.unbosque.mundialhub.dto;

import co.edu.unbosque.mundialhub.model.User.Role;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para estructurar,
 * transportar y exponer la información pública o de lectura de un usuario en el
 * sistema.
 * <p>
 * Se utiliza principalmente en las respuestas de los controladores REST hacia
 * las vistas de la aplicación (ej. perfil de usuario, listados de
 * administración), abstrayendo campos sensibles que no deben ser expuestos en
 * la red, como contraseñas, códigos OTP de verificación o marcas de tiempo de
 * auditoría interna.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see Role
 */
public class GetUserDTO {

	/** Nombre completo o de pila del usuario. */
	private String name;

	/**
	 * Nombre de usuario único (nickname o alias) utilizado para la autenticación en
	 * el sistema.
	 */
	private String userName;

	/** Documento o número de identificación oficial del usuario. */
	private String identification;

	/** Dirección de correo electrónico asociada a la cuenta del usuario. */
	private String email;

	/**
	 * Nombre o identificador de la selección nacional o equipo de fútbol favorito
	 * del usuario.
	 */
	private String favoriteTeam;

	/** Rol o nivel de permisos de acceso asignado al usuario dentro del sistema. */
	private Role role;

	/** Ciudad de residencia del usuario. */
	private String city;

	/** País de residencia del usuario. */
	private String country;

	/**
	 * Constructor completo parametrizado para instanciar e inicializar todos los
	 * atributos del DTO de lectura de usuario.
	 *
	 * @param name         Nombre completo del usuario.
	 * @param userName     Nombre de usuario único para inicio de sesión.
	 * @param id           Documento de identificación.
	 * @param email        Dirección de correo electrónico.
	 * @param favoriteTeam Equipo o selección favorita.
	 * @param role         Rol de seguridad asignado (Enum {@link Role}).
	 * @param city         Ciudad de origen o residencia.
	 * @param country      País de origen o residencia.
	 */
	public GetUserDTO(String name, String userName, String id, String email, String favoriteTeam, Role role,
			String city, String country) {
		super();
		this.name = name;
		this.userName = userName;
		this.identification = id;
		this.email = email;
		this.favoriteTeam = favoriteTeam;
		this.role = role;
		this.city = city;
		this.country = country;
	}

	/**
	 * Obtiene el nombre completo del usuario.
	 * 
	 * @return Una cadena de texto con el nombre.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Define o modifica el nombre completo del usuario.
	 * 
	 * @param name El nuevo nombre completo del usuario.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtiene el nombre de usuario (username) del sistema.
	 * 
	 * @return El alias único de la cuenta.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Define o modifica el nombre de usuario de acceso.
	 * 
	 * @param userName El nuevo nombre de usuario único.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Obtiene el número de identificación o cédula del usuario.
	 * 
	 * @return El documento de identidad.
	 */
	public String getIdentification() {
		return identification;
	}

	/**
	 * Define o modifica el número de identificación oficial.
	 * 
	 * @param id El nuevo documento de identidad.
	 */
	public void setIdentification(String id) {
		this.identification = id;
	}

	/**
	 * Obtiene la dirección de correo electrónico registrada.
	 * 
	 * @return El correo electrónico de contacto.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Define o modifica el correo electrónico de la cuenta.
	 * 
	 * @param email La nueva dirección de correo electrónico.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene el equipo de fútbol favorito seleccionado por el usuario.
	 * 
	 * @return El nombre o ID de la selección favorita.
	 */
	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	/**
	 * Define o modifica la selección de fútbol predilecta del usuario.
	 * 
	 * @param favoriteTeam El nombre de la nueva selección favorita.
	 */
	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}

	/**
	 * Obtiene el rol o nivel de privilegios que ostenta el usuario en la
	 * plataforma.
	 * 
	 * @return El tipo de rol basado en el enumerador {@link Role}.
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Asigna un nuevo rol de seguridad y control de accesos al usuario.
	 * 
	 * @param role El nuevo rol de usuario (Enum {@link Role}).
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Obtiene la ciudad de residencia del usuario.
	 * 
	 * @return El nombre de la ciudad.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Define o modifica la ciudad de ubicación del usuario.
	 * 
	 * @param city El nombre de la nueva ciudad de residencia.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Obtiene el país donde se encuentra localizado el usuario.
	 * 
	 * @return El nombre del país.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Define o modifica el país de localización del usuario.
	 * 
	 * @param country El nombre del nuevo país de residencia.
	 */
	public void setCountry(String country) {
		this.country = country;
	}

}