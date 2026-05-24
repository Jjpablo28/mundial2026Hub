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
 * @author TuNombre / Grupo de Desarrollo
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

	/**
	 * Identificador numérico único y autoincremental (llave primaria) del usuario
	 * en la base de datos.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Nombre de usuario único (nickname o alias) utilizado para iniciar sesión en
	 * la plataforma.
	 */
	@Column(unique = true)
	private String userName;

	/** Nombre completo o de pila de la persona titular de la cuenta. */
	private String name;

	/**
	 * Dirección de correo electrónico única y verificada vinculada al perfil del
	 * usuario.
	 */
	@Column(unique = true)
	private String email;

	/**
	 * Contraseña o clave secreta de acceso almacenada en formato criptográfico
	 * seguro.
	 */
	private String password;

	/** Documento o número de identificación oficial de la persona. */
	private String identification;

	/**
	 * Código alfanumérico temporal (OTP) enviado por correo para la activación de
	 * la cuenta o flujos de seguridad.
	 */
	private String code;

	/**
	 * Dirección URL pública del recurso gráfico o fotografía que el usuario utiliza
	 * como avatar de perfil.
	 */
	private String avatarUrl;

	/**
	 * Colección indexada de láminas asociadas al inventario personal del usuario,
	 * administrada bajo cascada total.
	 */
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserSticker> stickers;

	/**
	 * Puntuación acumulada por el usuario en las distintas dinámicas de
	 * gamificación del álbum.
	 */
	private Integer totalPoints;

	/**
	 * Cantidad de paquetes (sobres de láminas) cerrados disponibles en el
	 * inventario del usuario para ser abiertos.
	 */
	private Integer packets;

	/**
	 * Marca de tiempo exacta que estipula el límite de validez del código
	 * alfanumérico OTP.
	 */
	private LocalDateTime verificationExpires;

	/**
	 * Marca de tiempo exacta que registra la fecha y hora de creación de la cuenta
	 * de usuario.
	 */
	private LocalDateTime createdAt;

	/**
	 * Marca de tiempo exacta que audita la última modificación aplicada a los datos
	 * del perfil.
	 */
	private LocalDateTime updatedAt;

	/**
	 * Registro temporal que almacena el último inicio de sesión síncrono del
	 * usuario.
	 */
	private LocalDateTime lastLogin;

	/**
	 * Nombre o identificador de la selección nacional de fútbol predilecta del
	 * usuario.
	 */
	private String favoriteTeam;

	/** País geográfico de residencia o procedencia del titular de la cuenta. */
	private String country;

	/** Ciudad geográfica de residencia del usuario. */
	private String city;

	/**
	 * Rol o nivel de privilegios asignado al usuario, mapeado en la base de datos
	 * como una cadena de texto.
	 */
	@Enumerated(EnumType.STRING)
	private Role role;

	/**
	 * Bandera de Spring Security que determina si el ciclo de vigencia cronológica
	 * de la cuenta expiró.
	 */
	private boolean accountNonExpired;

	/**
	 * Bandera de Spring Security que indica si la cuenta del usuario se encuentra
	 * suspendida o bloqueada.
	 */
	private boolean accountNonLocked;

	/**
	 * Bandera de Spring Security que define si las credenciales de acceso
	 * (contraseña) han caducado.
	 */
	private boolean credentialsNonExpired;

	/**
	 * Bandera de Spring Security que establece si la cuenta se encuentra activa y
	 * validada en el sistema.
	 */
	private boolean enabled;

	/**
	 * Constructor por defecto (sin parámetros) que inicializa las banderas de
	 * seguridad de la cuenta en su estado operativo por defecto (no expirada, no
	 * bloqueada, credenciales vigentes, deshabilitada hasta validar OTP y sin rol
	 * inicial asignado).
	 */
	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = false;
		this.role = null;
	}

	/**
	 * Enumerador interno que define los roles jerárquicos y niveles de acceso
	 * admisibles para los usuarios de MundialHub.
	 */
	public enum Role {
		/**
		 * Rol estándar para los usuarios coleccionistas de láminas en la plataforma.
		 */
		USER,
		/**
		 * Rol administrativo con control total sobre los catálogos de stickers,
		 * usuarios y finanzas.
		 */
		ADMIN,
		/**
		 * Rol provisional con accesos restringidos durante flujos específicos de
		 * registro.
		 */
		TEMPORAL,
		/**
		 * Rol operativo enfocado en la supervisión logística y validación de canjes.
		 */
		OPERATOR,
		/** Rol especializado para agentes de soporte técnico y atención al cliente. */
		SUPPORT
	}

	/**
	 * Obtiene la contraseña criptográfica del usuario para los procesos de
	 * autenticación.
	 * 
	 * @return Una cadena de texto con el hash de la clave.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Define o modifica la contraseña de acceso del usuario.
	 * 
	 * @param password La nueva contraseña secreta (encriptada o plana según la
	 *                 capa).
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización
	 * manual de perfiles de usuario junto con todas sus variables de estado de
	 * seguridad y auditoría.
	 *
	 * @param id                    Identificador numérico único.
	 * @param userName              Alias único de ingreso.
	 * @param name                  Nombre completo del titular.
	 * @param email                 Correo electrónico institucional/personal.
	 * @param password              Contraseña encriptada.
	 * @param identification        Documento de identidad.
	 * @param code                  Token OTP de verificación.
	 * @param avatarUrl             Ruta URL del avatar.
	 * @param totalPoints           Puntos totales acumulados.
	 * @param packets               Sobres disponibles en inventario.
	 * @param verificationExpires   Instante de expiración del OTP.
	 * @param createdAt             Fecha de registro inicial de la cuenta.
	 * @param updatedAt             Fecha del último cambio efectuado.
	 * @param lastLogin             Fecha del último login registrado.
	 * @param favoriteTeam          Equipo o selección favorita.
	 * @param country               País de procedencia.
	 * @param city                  Ciudad de procedencia.
	 * @param role                  Rol asignado (Enum {@link Role}).
	 * @param accountNonExpired     Estado de expiración de cuenta.
	 * @param accountNonLocked      Estado de bloqueo de cuenta.
	 * @param credentialsNonExpired Estado de expiración de la clave.
	 * @param enabled               Estado de habilitación general.
	 */
	public User(Long id, String userName, String name, String email, String password, String identification,
			String code, String avatarUrl, Integer totalPoints, Integer packets, LocalDateTime verificationExpires,
			LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLogin, String favoriteTeam,
			String country, String city, Role role, boolean accountNonExpired, boolean accountNonLocked,
			boolean credentialsNonExpired, boolean enabled) {
		super();
		this.id = id;
		this.userName = userName;
		this.name = name;
		this.email = email;
		this.password = password;
		this.identification = identification;
		this.code = code;
		this.avatarUrl = avatarUrl;
		this.totalPoints = totalPoints;
		this.verificationExpires = verificationExpires;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastLogin = lastLogin;
		this.favoriteTeam = favoriteTeam;
		this.country = country;
		this.city = city;
		this.packets = packets;
		this.role = role;
		this.accountNonExpired = accountNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.credentialsNonExpired = credentialsNonExpired;
		this.enabled = enabled;
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
	 * Define el identificador numérico único de la cuenta de usuario.
	 * 
	 * @param id El nuevo ID de base de datos.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el alias o apodo exclusivo registrado por el usuario.
	 * 
	 * @return Cadena de texto con el nombre de usuario (userName).
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Modifica el alias de acceso exclusivo del usuario.
	 * 
	 * @param userName El nuevo nombre de usuario a asignar.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Obtiene el nombre completo del usuario.
	 * 
	 * @return Cadena de texto con el nombre real de la persona.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modifica el nombre completo de la persona titular de la cuenta.
	 * 
	 * @param name El nuevo nombre completo.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtiene la dirección de correo electrónico vinculada a la cuenta.
	 * 
	 * @return Cadena de texto con el email registrado.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Modifica la dirección de correo electrónico asociada al usuario.
	 * 
	 * @param email La nueva dirección de correo electrónico.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene el documento o número de identificación oficial.
	 * 
	 * @return Cadena con el documento de identidad.
	 */
	public String getIdentification() {
		return identification;
	}

	/**
	 * Modifica el número de identificación del usuario.
	 * 
	 * @param identification El nuevo documento de identidad.
	 */
	public void setIdentification(String identification) {
		this.identification = identification;
	}

	/**
	 * Obtiene la lista indexada de láminas (UserStickers) asociadas al inventario
	 * del usuario.
	 * 
	 * @return Una lista con las relaciones de propiedad de cromos
	 *         {@link UserSticker}.
	 */
	public List<UserSticker> getStickers() {
		return stickers;
	}

	/**
	 * Asigna una nueva colección completa de láminas al inventario del usuario.
	 * 
	 * @param stickers La nueva lista de cromos asociados.
	 */
	public void setStickers(List<UserSticker> stickers) {
		this.stickers = stickers;
	}

	/**
	 * Obtiene el volumen disponible de sobres cerrados de láminas en propiedad del
	 * usuario.
	 * 
	 * @return El número entero de paquetes en stock.
	 */
	public Integer getPackets() {
		return packets;
	}

	/**
	 * Modifica la cantidad de sobres cerrados que posee el usuario en su
	 * inventario.
	 * 
	 * @param packets El nuevo número de paquetes disponibles.
	 */
	public void setPackets(Integer packets) {
		this.packets = packets;
	}

	/**
	 * Devuelve los privilegios y autorizaciones de seguridad del usuario basados en
	 * su rol, añadiendo de forma obligatoria el prefijo "ROLE_" exigido por los
	 * evaluadores de Spring Security. * @return Una colección inmutable que
	 * contiene el {@link SimpleGrantedAuthority} del rol actual.
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	/**
	 * Método contractual de {@link UserDetails} encargado de suministrar el
	 * identificador principal para la autenticación síncrona.
	 * 
	 * @return El nombre de usuario único (userName).
	 */
	@Override
	public String getUsername() {
		return userName;
	}

	/**
	 * Genera un código hash entero único basado en la identidad relacional y las
	 * credenciales de la instancia.
	 * 
	 * @return Valor entero con el hash representativo del objeto.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, password, userName);
	}

	/**
	 * Obtiene el rol jerárquico actual del usuario en la plataforma.
	 * 
	 * @return El tipo de rol basado en el enumerador {@link Role}.
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Asigna un nuevo rol y escala de permisos de seguridad al usuario.
	 * 
	 * @param role El nuevo rol aplicable (Enum {@link Role}).
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Evalúa si la cuenta de usuario ha expirado por inactividad o políticas
	 * corporativas.
	 * 
	 * @return {@code true} si la cuenta sigue vigente; {@code false} si ha
	 *         caducado.
	 */
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	/**
	 * Configura el estado lógico de expiración de la cuenta.
	 * 
	 * @param accountNonExpired {@code true} para mantenerla vigente; {@code false}
	 *                          para darla por caducada.
	 */
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	/**
	 * Evalúa si la cuenta se encuentra administrativamente bloqueada o suspendida.
	 * 
	 * @return {@code true} si la cuenta está liberada; {@code false} si se
	 *         encuentra bloqueada.
	 */
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	/**
	 * Modifica el estatus de bloqueo administrativo de la cuenta.
	 * 
	 * @param accountNonLocked {@code true} para desbloquear la cuenta;
	 *                         {@code false} para congelarla.
	 */
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	/**
	 * Determina si las credenciales (contraseña) del usuario han expirado obligando
	 * a un cambio de clave.
	 * 
	 * @return {@code true} si la clave es vigente; {@code false} si ha expirado.
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	/**
	 * Configura el estado de caducidad temporal de las credenciales de acceso.
	 * 
	 * @param credentialsNonExpired {@code true} para mantener la clave vigente;
	 *                              {@code false} para forzar su expiración.
	 */
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	/**
	 * Determina si el usuario se encuentra habilitado para realizar acciones
	 * operativas en el backend.
	 * 
	 * @return {@code true} si está plenamente habilitado; {@code false} si está
	 *         inactivo.
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Cambia el estado de habilitación general de la cuenta del usuario.
	 * 
	 * @param enabled {@code true} para activar el usuario; {@code false} para
	 *                desactivarlo.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Obtiene el identificador UID estático establecido para la serialización.
	 * 
	 * @return El valor numérico largo del serialVersionUID.
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Obtiene la marca cronológica en la que se creó el registro del usuario.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora iniciales.
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
	 * Obtiene la marca cronológica que audita la modificación más reciente de los
	 * datos de la cuenta.
	 * 
	 * @return Un objeto {@link LocalDateTime} de último cambio.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Establece la marca temporal para la auditoría de modificaciones del perfil
	 * del usuario.
	 * 
	 * @param updatedAt El instante cronológico de la última alteración de datos.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Obtiene la marca temporal correspondiente al último acceso exitoso del
	 * usuario al ecosistema.
	 * 
	 * @return Un objeto {@link LocalDateTime} con el último instante de login.
	 */
	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	/**
	 * Establece la marca temporal que registra el acceso síncrono verificado más
	 * reciente del usuario.
	 * 
	 * @param lastLogin El instante del último login efectuado.
	 */
	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * Obtiene el nombre de la selección nacional de fútbol preferida por el
	 * usuario.
	 * 
	 * @return Cadena de texto con la escuadra favorita.
	 */
	public String getFavoriteTeam() {
		return favoriteTeam;
	}

	/**
	 * Modifica o asigna la selección de fútbol predilecta del usuario.
	 * 
	 * @param favoriteTeam El nombre de la nueva selección elegida.
	 */
	public void setFavoriteTeam(String favoriteTeam) {
		this.favoriteTeam = favoriteTeam;
	}

	/**
	 * Compara estructuralmente la igualdad lúdica de esta instancia con otro objeto
	 * basándose estrictamente en las variables críticas de coincidencia: ID,
	 * contraseña y nombre de usuario. * @param obj Objeto de comparación.
	 * 
	 * @return {@code true} si comparten id, contraseña y username equivalentes;
	 *         {@code false} en caso contrario.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(id, other.id) && Objects.equals(password, other.password)
				&& Objects.equals(userName, other.userName);
	}

	/**
	 * Obtiene el código de verificación alfanumérico OTP asignado transitoriamente
	 * a la cuenta.
	 * 
	 * @return El código de validación vigente.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Establece el código OTP requerido para realizar activaciones o
	 * autenticaciones multifactor.
	 * 
	 * @param code El código alfanumérico provisto por el sistema.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Obtiene el instante temporal en el que expira la validez legal del código de
	 * verificación.
	 * 
	 * @return Objeto {@link LocalDateTime} con la marca de expiración del token
	 *         OTP.
	 */
	public LocalDateTime getVerificationExpires() {
		return verificationExpires;
	}

	/**
	 * Establece el límite temporal de validez para el código OTP asignado a la
	 * cuenta.
	 * 
	 * @param verificationExpires El nuevo instante de expiración.
	 */
	public void setVerificationExpires(LocalDateTime verificationExpires) {
		this.verificationExpires = verificationExpires;
	}

	/**
	 * Obtiene el país de residencia actual registrado por el usuario.
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
	 * Obtiene la ciudad de residencia informada por el usuario.
	 * 
	 * @return El nombre de la ciudad.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Modifica la ciudad de residencia asociada a la cuenta del usuario.
	 * 
	 * @param city El nombre de la nueva ciudad.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Obtiene el enlace de localización de la fotografía o avatar del usuario.
	 * 
	 * @return Cadena de texto con la ruta URL de la imagen.
	 */
	public String getAvatarUrl() {
		return avatarUrl;
	}

	/**
	 * Define la localización multimedia o URL del avatar gráfico de la cuenta.
	 * 
	 * @param avatarUrl El nuevo enlace de la foto de perfil.
	 */
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	/**
	 * Obtiene la sumatoria o balance total de puntos de gamificación de la cuenta.
	 * 
	 * @return Un entero con el puntaje total acumulado.
	 */
	public Integer getTotalPoints() {
		return totalPoints;
	}

	/**
	 * Modifica el balance de puntos globales del usuario en la plataforma.
	 * 
	 * @param totalPoints La nueva cantidad entera de puntos.
	 */
	public void setTotalPoints(Integer totalPoints) {
		this.totalPoints = totalPoints;
	}

	/**
	 * Compila una representación textual estructurada que describe en tiempo real
	 * el estado e información de todas las variables lógicas y de seguridad
	 * encapsuladas en la entidad.
	 * 
	 * @return Una cadena de texto formateada con las propiedades del objeto.
	 */
	@Override
	public String toString() {
		return "User [id=" + id + ", userName=" + userName + ", name=" + name + ", email=" + email + ", password="
				+ password + ", identification=" + identification + ", code=" + code + ", avatarUrl=" + avatarUrl
				+ ", stickers=" + stickers + ", totalPoints=" + totalPoints + ", packets=" + packets
				+ ", verificationExpires=" + verificationExpires + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + ", lastLogin=" + lastLogin + ", favoriteTeam=" + favoriteTeam + ", country=" + country
				+ ", city=" + city + ", role=" + role + ", accountNonExpired=" + accountNonExpired
				+ ", accountNonLocked=" + accountNonLocked + ", credentialsNonExpired=" + credentialsNonExpired
				+ ", enabled=" + enabled + "]";
	}
}