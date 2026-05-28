package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad de persistencia que actúa como tabla intermedia (Join Table) entre
 * {@link User} y {@link Sticker}.
 * <p>
 * Representa el inventario dinámico de un usuario, modelando la relación de
 * posesión de láminas. Aplica una restricción de unicidad compuesta sobre los
 * campos {@code user_id} y {@code sticker_id}, garantizando que para cada
 * combinación de usuario y cromo, exista únicamente un registro que acumule el
 * volumen (cantidad) total poseído, evitando duplicidades innecesarias en la
 * base de datos.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see Entity
 * @see User
 * @see Sticker
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "sticker_id" }))
public class UserSticker {

	/**
	 * Identificador numérico único (llave primaria) de la relación de posesión en
	 * el sistema.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Referencia al usuario propietario del cromo. */
	@ManyToOne
	private User user;

	/** Referencia a la entidad del cromo o lámina específica poseída. */
	@ManyToOne

	private Sticker sticker;

	@ManyToOne

	private Team team;

	
	/**
	 * Volumen o cantidad total de copias que posee el usuario de esta lámina en
	 * particular.
	 */
	private Integer quantity; // cuántas tiene

	/**
	 * Marca de tiempo exacta que registra cuándo se adquirió o actualizó esta
	 * lámina en el inventario.
	 */
	private LocalDateTime obtainedAt;

	/**
	 * Constructor por defecto (sin parámetros) requerido por la especificación de
	 * Jakarta Persistence (JPA) para los procesos de mapeo relacional y
	 * reconstrucción de objetos.
	 */
	public UserSticker() {
		super();
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización
	 * directa de una relación de posesión de cromo.
	 *
	 * @param id         ID único del registro de inventario.
	 * @param user       Entidad del usuario dueño.
	 * @param sticker    Entidad del sticker poseído.
	 * @param quantity   Cantidad total de ejemplares de este cromo.
	 * @param obtainedAt Fecha y hora de la última actualización de esta posesión.
	 */
	public UserSticker(Long id, User user, Sticker sticker, Integer quantity,Team team, LocalDateTime obtainedAt) {
		super();
		this.id = id;
		this.user = user;
		this.sticker = sticker;
		this.team=team;
		this.quantity = quantity;
		this.obtainedAt = obtainedAt;
	}

	/**
	 * Obtiene el identificador único del registro de posesión.
	 * 
	 * @return El ID de base de datos.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Define el identificador único del registro.
	 * 
	 * @param id El nuevo ID a asignar.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene la entidad del usuario propietario de esta relación.
	 * 
	 * @return Objeto {@link User} asociado.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Asigna un usuario como propietario de este registro de inventario.
	 * 
	 * @param user La entidad {@link User} correspondiente.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Obtiene la entidad del sticker asociada a este registro.
	 * 
	 * @return Objeto {@link Sticker} poseído.
	 */
	public Sticker getSticker() {
		return sticker;
	}

	/**
	 * Asigna el sticker específico a este registro de inventario.
	 * 
	 * @param sticker La entidad {@link Sticker} a vincular.
	 */
	public void setSticker(Sticker sticker) {
		this.sticker = sticker;
	}

	/**
	 * Obtiene la cantidad de copias o unidades poseídas de este cromo.
	 * 
	 * @return Número entero con el stock disponible.
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * Define o actualiza el volumen de copias poseídas de este cromo.
	 * 
	 * @param quantity El nuevo valor entero de cantidad.
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * Obtiene la marca temporal en la que se registró la posesión o actualización
	 * del cromo.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la estampa de tiempo.
	 */
	public LocalDateTime getObtainedAt() {
		return obtainedAt;
	}

	/**
	 * Establece la marca temporal de obtención o actualización del cromo.
	 * 
	 * @param obtainedAt La fecha y hora exacta del evento.
	 */
	public void setObtainedAt(LocalDateTime obtainedAt) {
		this.obtainedAt = obtainedAt;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	
	
}