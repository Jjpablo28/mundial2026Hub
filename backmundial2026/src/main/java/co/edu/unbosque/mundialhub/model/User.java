package co.edu.unbosque.mundialhub.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Entidad de persistencia que representa la tabla "users" en la base de datos
 * de MundialHub e implementa el contrato de seguridad {@link UserDetails} de
 * Spring Security.
 * <p>
 * Centraliza la información del perfil del usuario (datos personales,
 * geográficos, avatar), los registros de gamificación (puntos, sobres
 * disponibles) y la relación con su colección de láminas (stickers).
 * Adicionalmente, provee la infraestructura lógica para la autenticación,
 * control de estados de cuenta (bloqueada, expirada, habilitada) y gestión de
 * roles jerárquicos.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see UserDetails
 * @see Entity
 * @see Table
 * @see UserSticker
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

	/** Identificador único para la serialización de la clase. */
	private static final long serialVersionUID = 1L;

	/** Identificador numérico único y autoincremental (llave primaria). */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Nombre de usuario único (nickname o alias) para iniciar sesión. */
	@Column(unique = true)
	private String userName;

	/** Nombre completo o de pila de la persona titular de la cuenta. */
	private String name;

	/** Dirección de correo electrónico única y verificada del usuario. */
	@Column(unique = true)
	private String email;

	/** Contraseña o clave secreta almacenada en formato criptográfico. */
	private String password;

	/** Documento o número de identificación oficial de la persona. */
	private String identification;

	/** Código alfanumérico temporal (OTP) enviado por correo para activación. */
	private String code;

	/** Dirección URL pública del avatar de perfil. */
	private String avatarUrl;

	/** Colección de láminas del inventario personal, administrada con cascada. */
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserSticker> stickers;

	/** Puntuación acumulada en las dinámicas de gamificación del álbum. */
	private Integer totalPoints;

	/** Cantidad de paquetes (sobres) cerrados disponibles en el inventario. */
	private Integer packets;

	/** Límite de validez del código alfanumérico OTP. */
	private LocalDateTime verificationExpires;

	/** Fecha y hora de creación de la cuenta. */
	private LocalDateTime createdAt;

	/** Fecha y hora de la última modificación del perfil. */
	private LocalDateTime updatedAt;

	/** Último inicio de sesión exitoso. */
	private LocalDateTime lastLogin;

	/** Selección nacional de fútbol predilecta del usuario. */
	private String favoriteTeam;

	/** País de residencia o procedencia. */
	private String country;

	/** Ciudad de residencia. */
	private String city;

	/** Rol o nivel de privilegios mapeado como cadena en la base de datos. */
	@Enumerated(EnumType.STRING)
	private Role role;

	/** Spring Security: indica si el ciclo de vigencia de la cuenta expiró. */
	private boolean accountNonExpired;

	/** Spring Security: indica si la cuenta está suspendida o bloqueada. */
	private boolean accountNonLocked;

	/** Spring Security: indica si las credenciales (contraseña) han caducado. */
	private boolean credentialsNonExpired;

	/** Spring Security: indica si la cuenta está activa y validada. */
	private boolean enabled;

	// ── NUEVOS CAMPOS: bloqueo por intentos fallidos ──────────────────────────

	/**
	 * Contador de intentos de inicio de sesión fallidos consecutivos. Se resetea a
	 * 0 en cada login exitoso. Al alcanzar el máximo configurado
	 * ({@code MAX_FAILED_ATTEMPTS}), la cuenta se deshabilita automáticamente.
	 */
	@Column(nullable = false, columnDefinition = "INT DEFAULT 0")
	private Integer failedLoginAttempts = 0;

	/**
	 * Fecha y hora en la que la cuenta fue bloqueada por intentos fallidos.
	 * {@code null} si la cuenta no está bloqueada por esta causa.
	 */
	@Column
	private LocalDateTime lockedAt;

	/** Número máximo de intentos fallidos antes de deshabilitar la cuenta. */
	public static final Integer MAX_FAILED_ATTEMPTS = 4;

	// ── Constructor por defecto ───────────────────────────────────────────────

	/**
	 * Constructor por defecto que inicializa las banderas de seguridad en su estado
	 * operativo por defecto (no expirada, no bloqueada, credenciales vigentes,
	 * deshabilitada hasta validar OTP y sin rol inicial asignado).
	 */
	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = false;
		this.role = null;
		this.failedLoginAttempts = 0;
		this.lockedAt = null;
	}

	// ── Enumerador de roles ───────────────────────────────────────────────────

	/**
	 * Enumerador interno que define los roles jerárquicos y niveles de acceso
	 * admisibles para los usuarios de MundialHub.
	 */
	public enum Role {
		/** Rol estándar para los usuarios coleccionistas de láminas. */
		USER,
		/** Rol administrativo con control total sobre la plataforma. */
		ADMIN,
		/** Rol provisional con accesos restringidos durante registro. */
		TEMPORAL,
		/** Rol operativo para supervisión logística y validación de canjes. */
		OPERATOR,
		/** Rol especializado para agentes de soporte técnico. */
		SUPPORT
	}

	// ── Constructor completo ──────────────────────────────────────────────────

	/**
	 * Constructor completo parametrizado para la instanciación manual de perfiles.
	 *
	 * @param id                    Identificador numérico único.
	 * @param userName              Alias único de ingreso.
	 * @param name                  Nombre completo del titular.
	 * @param email                 Correo electrónico.
	 * @param password              Contraseña encriptada.
	 * @param identification        Documento de identidad.
	 * @param code                  Token OTP de verificación.
	 * @param avatarUrl             Ruta URL del avatar.
	 * @param totalPoints           Puntos totales acumulados.
	 * @param packets               Sobres disponibles en inventario.
	 * @param verificationExpires   Instante de expiración del OTP.
	 * @param createdAt             Fecha de registro inicial.
	 * @param updatedAt             Fecha del último cambio.
	 * @param lastLogin             Fecha del último login.
	 * @param favoriteTeam          Equipo o selección favorita.
	 * @param country               País de procedencia.
	 * @param city                  Ciudad de procedencia.
	 * @param role                  Rol asignado (Enum {@link Role}).
	 * @param accountNonExpired     Estado de expiración de cuenta.
	 * @param accountNonLocked      Estado de bloqueo de cuenta.
	 * @param credentialsNonExpired Estado de expiración de la clave.
	 * @param enabled               Estado de habilitación general.
	 * @param failedLoginAttempts   Contador de intentos fallidos actuales.
	 * @param lockedAt              Instante en que fue bloqueada la cuenta.
	 */
	public User(Long id, String userName, String name, String email, String password, String identification,
			String code, String avatarUrl, List<UserSticker> stickers, Integer totalPoints, Integer packets,
			LocalDateTime verificationExpires, LocalDateTime createdAt, LocalDateTime updatedAt,
			LocalDateTime lastLogin, String favoriteTeam, String country, String city, Role role,
			boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, boolean enabled,
			Integer failedLoginAttempts, LocalDateTime lockedAt) {
		super();
		this.id = id;
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.password = password;
		this.identification = identification;
		this.code = code;
		this.avatarUrl = avatarUrl;
		this.stickers = stickers;
		this.totalPoints = totalPoints;
		this.packets = packets;
		this.verificationExpires = verificationExpires;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastLogin = lastLogin;
		this.favoriteTeam = favoriteTeam;
		this.country = country;
		this.city = city;
		this.role = role;
		this.accountNonExpired = accountNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.credentialsNonExpired = credentialsNonExpired;
		this.enabled = enabled;
		this.failedLoginAttempts = failedLoginAttempts;
		this.lockedAt = lockedAt;
	}


	// ── Getters y Setters de los campos nuevos ────────────────────────────────

	/**
	 * Obtiene el número de intentos de login fallidos consecutivos.
	 *
	 * @return Entero con el contador actual de fallos.
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

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public List<UserSticker> getStickers() {
		return stickers;
	}

	public void setStickers(List<UserSticker> stickers) {
		this.stickers = stickers;
	}

	public Integer getPackets() {
		return packets;
	}

	public void setPackets(Integer packets) {
		this.packets = packets;
	}

	public Integer getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}

	public LocalDateTime getVerificationExpires() {
		return verificationExpires;
	}

	public void setVerificationExpires(LocalDateTime v) {
		this.verificationExpires = v;
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

	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	// ── Spring Security ───────────────────────────────────────────────────────

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return userName;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public void setCredentialsNonExpired(boolean v) {
		this.credentialsNonExpired = v;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	// ── equals / hashCode / toString ──────────────────────────────────────────

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id) && Objects.equals(password, other.password)
				&& Objects.equals(userName, other.userName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, password, userName);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", userName=" + userName + ", name=" + name + ", email=" + email + ", password="
				+ password + ", identification=" + identification + ", code=" + code + ", avatarUrl=" + avatarUrl
				+ ", stickers=" + stickers + ", totalPoints=" + totalPoints + ", packets=" + packets
				+ ", verificationExpires=" + verificationExpires + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", lastLogin=" + lastLogin + ", favoriteTeam=" + favoriteTeam + ", country=" + country
				+ ", city=" + city + ", role=" + role + ", accountNonExpired=" + accountNonExpired
				+ ", accountNonLocked=" + accountNonLocked + ", credentialsNonExpired=" + credentialsNonExpired
				+ ", enabled=" + enabled + ", failedLoginAttempts=" + failedLoginAttempts + ", lockedAt=" + lockedAt
				+ "]";
	}
}