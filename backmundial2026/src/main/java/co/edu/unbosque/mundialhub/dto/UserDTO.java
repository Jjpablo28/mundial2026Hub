package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.mundialhub.model.User.Role;

/**
 * DTO para la transferencia de datos del usuario. Se utiliza principalmente
 * como DTO de respuesta, ocultando la contraseña y simplificando las
 * relaciones.
 * <p>
 * Centraliza los atributos del perfil de usuario dentro de la plataforma
 * MundialHub, abarcando desde credenciales y tokens temporales de verificación,
 * hasta datos de gamificación (puntos, sobres disponibles), preferencias
 * geográficas/deportivas y control de bloqueo por intentos fallidos.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see Desarrollador
 * @see LocalDateTime
 */
public class UserDTO {

	/** Identificador numérico único (llave primaria) del usuario. */
	private Long id;

	/** Nombre de usuario único (nickname o alias) para la autenticación. */
	private String userName;

	/** Nombre completo o de pila de la persona titular de la cuenta. */
	private String name;

	/** Dirección de correo electrónico única vinculada al usuario. */
	private String email;

	/** Documento o número de identificación oficial del usuario. */
	private String identification;

	/** Código alfanumérico OTP para la activación o recuperación de la cuenta. */
	private String code;

	/** Fecha y hora de expiración del código de verificación (OTP). */
	private LocalDateTime verificationExpires;

	/** Contraseña o clave secreta de acceso al sistema. */
	private String password;

	/** Nivel de privilegios asignado al usuario basado en el enum {@link Role}. */
	private Role role;

	/** Dirección URL del avatar del usuario. */
	private String avatarUrl;

	/** Estado de habilitación de la cuenta (activa/inactiva). */
	private Boolean enabled;

	/** Puntuación acumulada en las dinámicas de gamificación del álbum. */
	private Integer totalPoints;

	/** Cantidad de paquetes (sobres) disponibles en el inventario del usuario. */
	private Integer packets;

	/** País de procedencia o residencia del usuario. */
	private String country;

	/** Selección nacional de fútbol predilecta del usuario. */
	private String favoriteTeam;

	/** Ciudad de origen o residencia del usuario. */
	private String city;

	/** Marca de tiempo de creación del perfil en la base de datos. */
	private LocalDateTime createdAt;

	/** Marca de tiempo de la modificación más reciente del usuario. */
	private LocalDateTime updatedAt;

	/** Registro temporal de la última sesión válida del usuario. */
	private LocalDateTime lastLogin;

	// ── NUEVOS CAMPOS: bloqueo por intentos fallidos ──────────────────────────

	/**
	 * Contador de intentos de inicio de sesión fallidos consecutivos. Solo se
	 * expone en respuestas de administración/soporte; nunca al usuario final
	 * directamente.
	 */
	private Integer failedLoginAttempts;

	/**
	 * Fecha y hora en que la cuenta fue bloqueada por exceder el máximo de
	 * intentos. {@code null} si la cuenta no está bloqueada por esta causa.
	 */
	private LocalDateTime lockedAt;

	// ── Constructores ──────────────────────────────────────────────────────────

	/** Constructor por defecto requerido por frameworks de serialización. */
	public UserDTO() {
	}

	/**
	 * Constructor completo con todos los campos incluyendo los de bloqueo.
	 *
	 * @param id                  Identificador único.
	 * @param userName            Alias de usuario.
	 * @param name                Nombre completo.
	 * @param email               Correo electrónico.
	 * @param identification      Documento de identidad.
	 * @param code                Código OTP temporal.
	 * @param verificationExpires Expiración del OTP.
	 * @param password            Contraseña de acceso.
	 * @param role                Rol asignado ({@link Role}).
	 * @param avatarUrl           Enlace URL del avatar.
	 * @param enabled             Estado de habilitación de la cuenta.
	 * @param totalPoints         Puntaje total acumulado.
	 * @param packets             Cantidad de sobres disponibles.
	 * @param country             País de residencia.
	 * @param favoriteTeam        Selección de fútbol favorita.
	 * @param city                Ciudad de residencia.
	 * @param createdAt           Fecha de registro inicial.
	 * @param updatedAt           Fecha del último cambio.
	 * @param lastLogin           Fecha del último acceso exitoso.
	 * @param failedLoginAttempts Contador de intentos fallidos actuales.
	 * @param lockedAt            Instante en que fue bloqueada la cuenta.
	 */
	public UserDTO(Long id, String userName, String name, String email, String identification, String code,
			LocalDateTime verificationExpires, String password, Role role, String avatarUrl, Boolean enabled,
			Integer totalPoints, Integer packets, String country, String favoriteTeam, String city,
			LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin, Integer failedLoginAttempts,
			LocalDateTime lockedAt) {
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
		this.failedLoginAttempts = failedLoginAttempts;
		this.lockedAt = lockedAt;
	}

	// ── Getters y Setters de los campos nuevos ────────────────────────────────

	/**
	 * Obtiene el contador de intentos de login fallidos consecutivos.
	 *
	 * @return Entero con el número de fallos acumulados.
	 */
	public Integer getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	/**
	 * Establece el contador de intentos de login fallidos.
	 *
	 * @param failedLoginAttempts El nuevo valor del contador.
	 */
	public void setFailedLoginAttempts(Integer failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	/**
	 * Obtiene la fecha y hora en que la cuenta fue bloqueada por intentos fallidos.
	 *
	 * @return {@link LocalDateTime} del bloqueo, o {@code null} si no está
	 *         bloqueada.
	 */
	public LocalDateTime getLockedAt() {
		return lockedAt;
	}

	/**
	 * Establece la fecha y hora del bloqueo de la cuenta.
	 *
	 * @param lockedAt El instante en que se ejecutó el bloqueo.
	 */
	public void setLockedAt(LocalDateTime lockedAt) {
		this.lockedAt = lockedAt;
	}

	// ── Getters y Setters existentes ──────────────────────────────────────────

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public void setIdentification(String id) {
		this.identification = id;
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

	public void setVerificationExpires(LocalDateTime v) {
		this.verificationExpires = v;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
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

	// ── toString ──────────────────────────────────────────────────────────────

	@Override
	public String toString() {
		return "UserDTO [id=" + id + ", userName=" + userName + ", name=" + name + ", email=" + email
				+ ", identification=" + identification + ", code=" + code + ", verificationExpires="
				+ verificationExpires + ", password=" + password + ", role=" + role + ", avatarUrl=" + avatarUrl
				+ ", enabled=" + enabled + ", totalPoints=" + totalPoints + ", packets=" + packets + ", country="
				+ country + ", favoriteTeam=" + favoriteTeam + ", city=" + city + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + ", lastLogin=" + lastLogin + ", failedLoginAttempts="
				+ failedLoginAttempts + ", lockedAt=" + lockedAt + "]";
	}
}