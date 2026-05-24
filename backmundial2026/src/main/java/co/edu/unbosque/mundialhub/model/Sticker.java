package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Entidad de persistencia que representa la tabla o el registro de un
 * cromo/lámina (Sticker) en la base de datos de MundialHub.
 * <p>
 * Define las características estructurales de cada cromo dentro del catálogo
 * maestro del álbum, encapsulando los datos del futbolista (nombre, posición),
 * su nivel de escasez (rareza), el costo en tokens o créditos del sistema, el
 * recurso multimedia asociado y su relación con una selección nacional (Team).
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see Entity
 * @see Rarity
 * @see Team
 */
@Entity
public class Sticker {

	/**
	 * Identificador numérico único y autoincremental (llave primaria) del sticker
	 * en el sistema.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// nombre del jugador
	/** Nombre completo del jugador de fútbol representado en la lámina. */
	private String playerName;

	/**
	 * Nivel de escasez o exclusividad del cromo, mapeado en la base de datos como
	 * una cadena de texto (String).
	 */
	@Enumerated(EnumType.STRING)
	private Rarity rarity;

	/**
	 * Relación de muchos a uno que asocia el cromo con la selección nacional o
	 * equipo al que pertenece el jugador.
	 */
	@ManyToOne
	private Team team;

	// valor de venta o compra
	/**
	 * Costo transaccional o valor numérico asignado para las operaciones de compra,
	 * venta o intercambio del cromo.
	 */
	private Integer cost;

	/**
	 * Dirección URL pública o ruta del recurso gráfico/imagen representativa de la
	 * lámina.
	 */
	private String imageUrl;

	/**
	 * Demarcación, rol o posición táctica del jugador dentro del terreno de juego
	 * (ej. Portero, Delantero).
	 */
	private String position;

	/**
	 * Marca de tiempo exacta que registra el instante de creación del cromo en el
	 * catálogo maestro.
	 */
	private LocalDateTime createdAt;

	/**
	 * Marca de tiempo exacta que audita la última modificación aplicada a los datos
	 * de este cromo.
	 */
	private LocalDateTime updatedAt;

	/**
	 * Constructor por defecto (sin parámetros) exigido por la especificación de
	 * Jakarta Persistence (JPA) para los procesos internos de instanciación,
	 * hidratación de objetos y mapeo relacional.
	 */
	public Sticker() {
		super();
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización de
	 * un cromo con todas sus propiedades de catálogo, metadatos y relaciones
	 * asignadas.
	 *
	 * @param id         ID único de la lámina.
	 * @param playerName Nombre completo del futbolista.
	 * @param rarity     Grado de escasez del cromo (Enum {@link Rarity}).
	 * @param team       Entidad de la selección nacional asociada (Entidad
	 *                   {@link Team}).
	 * @param position   Posición táctica en el campo de juego.
	 * @param cost       Costo o valor transaccional en créditos.
	 * @param imageUrl   Ruta URL de la ilustración o arte gráfico.
	 * @param createdAt  Fecha y hora de inserción original en el sistema.
	 * @param updatedAt  Fecha y hora de la última modificación en el sistema.
	 */
	public Sticker(Long id, String playerName, Rarity rarity, Team team, String position, Integer cost, String imageUrl,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.playerName = playerName;
		this.rarity = rarity;
		this.team = team;
		this.position = position;
		this.cost = cost;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Obtiene el identificador numérico único de la lámina en la base de datos.
	 * 
	 * @return Un entero largo con el ID correspondiente.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador numérico único de la lámina.
	 * 
	 * @param id El nuevo ID de base de datos a asignar.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el nombre completo del jugador de fútbol.
	 * 
	 * @return Una cadena de texto con el nombre del futbolista.
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Define o modifica el nombre del jugador de fútbol de la lámina.
	 * 
	 * @param playerName El nuevo nombre del jugador.
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * Obtiene la categoría de escasez o rareza asignada al cromo.
	 * 
	 * @return El tipo de rareza basado en el enumerador {@link Rarity}.
	 */
	public Rarity getRarity() {
		return rarity;
	}

	/**
	 * Asigna un nivel de rareza específico al cromo.
	 * 
	 * @param rarity La nueva categoría del cromo (Enum {@link Rarity}).
	 */
	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}

	/**
	 * Obtiene la entidad del equipo o selección nacional a la que pertenece el
	 * cromo.
	 * 
	 * @return El objeto {@link Team} vinculado contractualmente al jugador.
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * Vincula el cromo a una selección nacional o equipo de fútbol específico.
	 * 
	 * @param team La entidad {@link Team} correspondiente al país.
	 */
	public void setTeam(Team team) {
		this.team = team;
	}

	/**
	 * Obtiene el costo o valor en créditos configurado para la adquisición del
	 * cromo.
	 * 
	 * @return Un número entero con el valor transaccional.
	 */
	public Integer getCost() {
		return cost;
	}

	/**
	 * Define el costo o valor en créditos requerido para interactuar comercialmente
	 * con el cromo.
	 * 
	 * @param cost El nuevo precio numérico asignado.
	 */
	public void setCost(Integer cost) {
		this.cost = cost;
	}

	/**
	 * Obtiene la ubicación URL de la imagen del cromo.
	 * 
	 * @return Una cadena de texto con el enlace del recurso multimedia.
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	/**
	 * Establece el enlace URL pública para renderizar el diseño visual de la
	 * lámina.
	 * 
	 * @param imageUrl Cadena de texto con la nueva ruta de la imagen.
	 */
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	/**
	 * Obtiene la marca de tiempo de inserción original de la lámina en el sistema.
	 * 
	 * @return Un objeto {@link LocalDateTime} con los datos temporales iniciales.
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Establece la marca de tiempo que registra la creación del cromo.
	 * 
	 * @param createdAt Objeto {@link LocalDateTime} correspondiente al instante de
	 *                  creación.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene la demarcación táctica en el terreno de juego del futbolista.
	 * 
	 * @return Cadena de texto descriptiva de la posición (ej. "Delantero").
	 */
	public String getPosition() {
		return position;
	}

	/**
	 * Define o modifica la posición de juego del futbolista.
	 * 
	 * @param position La nueva cadena de texto representativa de la demarcación
	 *                 táctica.
	 */
	public void setPosition(String position) {
		this.position = position;
	}

	/**
	 * Obtiene la marca de tiempo que registra la modificación más reciente de los
	 * datos de la lámina.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la última fecha y hora de cambio.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Establece la marca de tiempo para el control de auditoría de modificaciones
	 * del cromo.
	 * 
	 * @param updatedAt Objeto {@link LocalDateTime} con el instante exacto de la
	 *                  alteración.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Genera una representación textual formateada que expone el estado y los
	 * valores actuales de todas las variables de instancia que componen la entidad
	 * de este cromo.
	 * 
	 * @return Una cadena de texto con los atributos descriptivos de la clase.
	 */
	@Override
	public String toString() {
		return "Sticker [id=" + id + ", playerName=" + playerName + ", rarity=" + rarity + ", team=" + team + ", cost="
				+ cost + ", imageUrl=" + imageUrl + ", position=" + position + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
}