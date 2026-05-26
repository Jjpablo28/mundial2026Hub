package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.mundialhub.model.User.Role;

/**
 * DTO para la transferencia de datos del usuario. Se utiliza principalmente
 * como DTO de respuesta, ocultando la contraseña y simplificando las
 * relaciones.
 * <p>
 * Este Objeto de Transferencia de Datos (DTO) centraliza los atributos del
 * perfil de usuario dentro de la plataforma MundialHub, abarcando desde
 * credenciales y tokens temporales de verificación, hasta datos de gamificación
 * (puntos, sobres disponibles) y preferencias geográficas/deportivas.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see Role
 * @see LocalDateTime
 */
public class UserDTO {

	/** Identificador numérico único (llave primaria) del usuario en el sistema. */
	private Long id;

	/**
	 * Nombre de usuario único (nickname o alias) para la autenticación en la
	 * plataforma.
	 */
	private String userName;

	/** Nombre completo o de pila de la persona titular de la cuenta. */
	private String name;

	/** Dirección de correo electrónico única vinculada al usuario. */
	private String email;

	/** Documento o número de identificación oficial del usuario. */
	private String identification;

	/**
	 * Código alfanumérico OTP provisto temporalmente para la activación o
	 * recuperación de la cuenta.
	 */
	private String code;

	/**
	 * Fecha y hora exacta en la que expira la validez del código de verificación
	 * (OTP).
	 */
	private LocalDateTime verificationExpires;

	/** Contraseña o clave secreta encriptada/plana de acceso al sistema. */
	private String password;

	/**
	 * Nivel de privilegios y permisos de seguridad asignado al usuario basado en el
	 * enum {@link Role}.
	 */
	private Role role;

	/**
	 * Dirección URL pública del recurso gráfico o fotografía que el usuario utiliza
	 * de avatar.
	 */
	private String avatarUrl;
	
	private Boolean enabled;

	/**
	 * Puntuación acumulada por el usuario en las dinámicas de gamificación del
	 * álbum.
	 */
	private Integer totalPoints;

	/**
	 * Cantidad de paquetes (sobres de láminas) cerrados disponibles en el
	 * inventario del usuario.
	 */
	private Integer packets;

	/** País de procedencia o residencia del usuario. */
	private String country;

	/**
	 * Nombre o identificador de la selección nacional de fútbol predilecta del
	 * usuario.
	 */
	private String favoriteTeam;

	/** Ciudad de origen o residencia del usuario. */
	private String city;

	/**
	 * Marca de tiempo exacta que registra la creación del perfil en la base de
	 * datos.
	 */
	private LocalDateTime createdAt;

	/**
	 * Marca de tiempo exacta que registra la modificación más reciente de los datos
	 * del usuario.
	 */
	private LocalDateTime updatedAt;

	/**
	 * Registro temporal de la última vez que el usuario inició sesión de forma
	 * válida.
	 */
	private LocalDateTime lastLogin;

	/**
	 * Constructor por defecto (sin parámetros) requerido por los frameworks de
	 * persistencia, mapeo de componentes y utilidades de
	 * serialización/deserialización JSON (como Jackson).
	 */
	public UserDTO() {
	}

	/**
	 * Obtiene la contraseña almacenada transitoriamente en el DTO.
	 * 
	 * @return Una cadena de texto con la clave de acceso.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece o modifica la contraseña dentro de la estructura de transferencia.
	 * 
	 * @param password La nueva clave secreta de acceso.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización de
	 * todas las propiedades de control, gamificación y perfil del usuario.
	 *
	 * @param userName            Nombre de usuario único para acceso.
	 * @param name                Nombre completo de la persona.
	 * @param email               Dirección de correo electrónico.
	 * @param identification      Documento de identidad oficial.
	 * @param code                Código de verificación temporal.
	 * @param verificationExpires Instante de expiración del código OTP.
	 * @param password            Contraseña de acceso.
	 * @param role                Rol con permisos asignados (Enum {@link Role}).
	 * @param avatarUrl           Enlace URL de la imagen de perfil.
	 * @param totalPoints         Puntaje total acumulado en la plataforma.
	 * @param packets             Cantidad de sobres disponibles para abrir.
	 * @param country             País de residencia.
	 * @param favoriteTeam        Selección de fútbol favorita.
	 * @param city                Ciudad de residencia.
	 * @param createdAt           Fecha y hora de registro inicial en el sistema.
	 * @param updatedAt           Fecha y hora del último cambio en el perfil.
	 * @param lastLogin           Fecha y hora del último acceso exitoso.
	 */
	
	public UserDTO(Long id, String userName, String name, String email, String identification, String code,
			LocalDateTime verificationExpires, String password, Role role, String avatarUrl, Boolean enabled,
			Integer totalPoints, Integer packets, String country, String favoriteTeam, String city,
			LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin) {
		super();
		this.id = id;
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.identification = identification;
		this.code = code;
		this.verificationExpires = verificationExpires;
		this.password = password;
		this.role = role;
		this.avatarUrl = avatarUrl;
		this.enabled = enabled;
		this.totalPoints = totalPoints;
		this.packets = packets;
		this.country = country;
		this.favoriteTeam = favoriteTeam;
		this.city = city;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastLogin = lastLogin;
	}
	
	/**
	 * Obtiene el identificador numérico único (ID) del usuario.
	 * 
	 * @return Un entero largo con la llave primaria.
	 */
	public Long getId() {
		return id;
	}

	

	/**
	 * Define o modifica el identificador numérico del usuario.
	 * 
	 * @param id El nuevo ID de base de datos.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el apodo o nombre de usuario de la cuenta.
	 * 
	 * @return Cadena de texto con el alias (username).
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Modifica el nombre de usuario de la cuenta.
	 * 
	 * @param userName El nuevo alias exclusivo del usuario.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Obtiene el nombre completo del titular de la cuenta.
	 * 
	 * @return El nombre real del usuario.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modifica el nombre completo de la persona titular.
	 * 
	 * @param name El nuevo nombre completo.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtiene el correo electrónico asociado a la cuenta.
	 * 
	 * @return Cadena con la dirección de correo.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Modifica el correo electrónico de contacto del usuario.
	 * 
	 * @param email La nueva dirección de correo electrónico.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene el número de identificación personal del usuario.
	 * 
	 * @return El documento de identidad.
	 */
	public String getIdentification() {
		return identification;
	}

	/**
	 * Modifica el número de identificación personal de la cuenta.
	 * 
	 * @param identification El nuevo documento de identidad.
	 */
	public void setIdentification(String identification) {
		this.identification = identification;
	}

	/**
	 * Obtiene el rol de seguridad actual de la cuenta.
	 * 
	 * @return El nivel de permisos basado en el enumerador {@link Role}.
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Asigna un nuevo rol y jerarquía de accesos al usuario.
	 * 
	 * @param role El nuevo rol aplicable (Enum {@link Role}).
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Obtiene el código OTP de verificación asignado transitoriamente.
	 * 
	 * @return El código alfanumérico temporal.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Establece el código OTP para validaciones de seguridad de la cuenta.
	 * 
	 * @param code El código alfanumérico generado por el sistema.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Obtiene el instante en el que expira el token de verificación.
	 * 
	 * @return Objeto {@link LocalDateTime} con el límite de validez.
	 */
	public LocalDateTime getVerificationExpires() {
		return verificationExpires;
	}

	/**
	 * Define el límite temporal de validez para el código de seguridad.
	 * 
	 * @param verificationExpires El nuevo instante de expiración.
	 */
	public void setVerificationExpires(LocalDateTime verificationExpires) {
		this.verificationExpires = verificationExpires;
	}

	/**
	 * Obtiene la selección nacional de fútbol preferida por el usuario.
	 * 
	 * @return El nombre o ID de la escuadra favorita.
	 */
	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	/**
	 * Modifica o asigna la escuadra nacional predilecta del usuario.
	 * 
	 * @param favoriteTeam El nombre de la nueva selección elegida.
	 */
	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}

	/**
	 * Obtiene el instante cronológico de la creación del perfil.
	 * 
	 * @return El objeto {@link LocalDateTime} con la marca temporal original.
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Define la marca temporal que registra la creación del usuario.
	 * 
	 * @param createdAt El instante cronológico de alta en el sistema.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene el instante cronológico de la modificación más reciente del perfil.
	 * 
	 * @return El objeto {@link LocalDateTime} de auditoría de cambios.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Define la marca temporal para la auditoría de actualizaciones del perfil.
	 * 
	 * @param updatedAt El instante cronológico del cambio efectuado.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Obtiene la marca temporal correspondiente al último inicio de sesión exitoso.
	 * 
	 * @return El objeto {@link LocalDateTime} de último login.
	 */
	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	/**
	 * Establece la marca temporal que almacena el acceso síncrono más reciente del
	 * usuario.
	 * 
	 * @param lastLogin El instante del último login verificado.
	 */
	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * Obtiene el país geográfico de residencia del titular de la cuenta.
	 * 
	 * @return El nombre del país.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Modifica el país de procedencia o residencia del usuario.
	 * 
	 * @param country El nuevo nombre del territorio nacional.
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Obtiene la ciudad geográfica asociada al usuario.
	 * 
	 * @return El nombre de la ciudad.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Modifica la ciudad de residencia del usuario.
	 * 
	 * @param city El nombre de la nueva ciudad.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Obtiene el enlace o recurso URL de la foto de perfil (avatar).
	 * 
	 * @return La cadena de texto con la ruta de la imagen.
	 */
	public String getAvatarUrl() {
		return avatarUrl;
	}

	/**
	 * Define la localización multimedia del avatar del usuario.
	 * 
	 * @param avatarUrl El nuevo enlace de la foto de perfil.
	 */
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	/**
	 * Obtiene el saldo o sumatoria de puntos acumulados por la cuenta.
	 * 
	 * @return Un entero con el puntaje total.
	 */
	public Integer getTotalPoints() {
		return totalPoints;
	}

	/**
	 * Modifica el balance de puntos totales del usuario en la plataforma.
	 * 
	 * @param totalPoints La nueva cantidad entera de puntos.
	 */
	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}

	/**
	 * Obtiene el volumen disponible de sobres de láminas en propiedad del usuario.
	 * 
	 * @return El número de paquetes sin abrir.
	 */
	public Integer getPackets() {
		return packets;
	}

	/**
	 * Modifica el inventario de paquetes o sobres cerrados del usuario.
	 * 
	 * @param packets El nuevo número de paquetes en stock personal.
	 */
	public void setPackets(Integer packets) {
		this.packets = packets;
	}





	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Compila y genera una cadena textual detallada que expone los valores en
	 * tiempo real de cada uno de los campos encapsulados en esta instancia DTO.
	 * 
	 * @return Una cadena de texto formateada con los atributos del objeto.
	 */
	@Override
	public String toString() {
		return "UserDTO [id=" + id + ", userName=" + userName + ", name=" + name + ", email=" + email
				+ ", identification=" + identification + ", code=" + code + ", verificationExpires="
				+ verificationExpires + ", password=" + password + ", role=" + role + ", avatarUrl=" + avatarUrl
				+ ", enabled=" + enabled + ", totalPoints=" + totalPoints + ", packets=" + packets + ", country="
				+ country + ", favoriteTeam=" + favoriteTeam + ", city=" + city + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", lastLogin=" + lastLogin + "]";
	}
}