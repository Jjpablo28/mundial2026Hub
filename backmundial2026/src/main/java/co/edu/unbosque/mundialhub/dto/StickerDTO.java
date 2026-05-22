package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.mundialhub.model.Rarity;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para estructurar,
 * transportar y exponer las propiedades técnicas de una lámina o cromo en la
 * plataforma MundialHub.
 * <p>
 * Agrupa la información descriptiva del jugador (nombre, posición en el campo),
 * metadatos lógicos (rareza, selección de fútbol asociada), costos de
 * adquisición e imágenes, junto con las marcas de tiempo de auditoría para la
 * sincronización con los clientes de la aplicación.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see Rarity
 * @see LocalDateTime
 */
public class StickerDTO {

	/** Identificador numérico único (llave primaria) del sticker en el sistema. */
	private Long id;

	/** Nombre completo del jugador de fútbol representado en la lámina. */
	private String playerName;

	/**
	 * Nivel de escasez o rareza asignado a la lámina basado en el enumerador
	 * {@link Rarity}.
	 */
	private Rarity rarity;

	/**
	 * Identificador único de la selección nacional o equipo al que pertenece el
	 * jugador.
	 */
	private Long teamId;

	/**
	 * Posición táctica del jugador en el campo de juego (ej. Delantero, Portero).
	 */
	private String position;

	/**
	 * Costo monetario o valor en tokens requerido para adquirir o valorar el
	 * sticker.
	 */
	private Integer cost;

	/** Dirección URL pública del archivo multimedia o recurso gráfico del cromo. */
	private String imageUrl;

	/**
	 * Marca de tiempo exacta que registra la creación original del cromo en el
	 * catálogo maestro.
	 */
	private LocalDateTime createdAt;

	/**
	 * Marca de tiempo exacta que registra la última modificación de los datos de la
	 * lámina.
	 */
	private LocalDateTime updatedAt;

	/**
	 * Constructor por defecto (sin parámetros) requerido por las librerías de mapeo
	 * de objetos de datos y serialización/deserialización JSON (como Jackson).
	 */
	public StickerDTO() {
		super();
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización de
	 * todos los atributos del DTO del sticker.
	 *
	 * @param id         ID único del sticker.
	 * @param playerName Nombre completo del jugador.
	 * @param rarity     Grado de rareza o escasez (Enum {@link Rarity}).
	 * @param teamId     ID de la selección nacional asociada.
	 * @param position   Posición táctica en la cancha.
	 * @param cost       Costo en créditos o tokens del sistema.
	 * @param imageUrl   Ruta URL de la imagen del cromo.
	 * @param createdAt  Fecha de creación del registro.
	 * @param updatedAt  Fecha de última actualización del registro.
	 */
	public StickerDTO(Long id, String playerName, Rarity rarity, Long teamId, String position, Integer cost,
			String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.playerName = playerName;
		this.rarity = rarity;
		this.teamId = teamId;
		this.position = position;
		this.cost = cost;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Obtiene el identificador numérico único del sticker.
	 * 
	 * @return Un entero largo con el ID de base de datos.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Define o modifica el identificador numérico del sticker.
	 * 
	 * @param id El nuevo ID del registro.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el nombre completo del jugador de la lámina.
	 * 
	 * @return Una cadena de texto con el nombre del futbolista.
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Define o modifica el nombre completo del jugador.
	 * 
	 * @param playerName El nuevo nombre del jugador.
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * Obtiene el nivel de escasez o rareza de la lámina.
	 * 
	 * @return El tipo de rareza basado en el enumerador {@link Rarity}.
	 */
	public Rarity getRarity() {
		return rarity;
	}

	/**
	 * Asigna un nivel de rareza específico a la lámina.
	 * 
	 * @param rarity La nueva categoría del cromo (Enum {@link Rarity}).
	 */
	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}

	/**
	 * Obtiene el identificador de la selección nacional asociada al jugador.
	 * 
	 * @return El entero largo que representa el ID del equipo.
	 */
	public Long getTeamId() {
		return teamId;
	}

	/**
	 * Asocia el sticker a una selección nacional de fútbol a través de su ID.
	 * 
	 * @param teamId El nuevo identificador único del equipo de fútbol.
	 */
	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	/**
	 * Obtiene el costo transaccional o valor de adquisición en tokens de la lámina.
	 * 
	 * @return Un valor entero con el costo configurado.
	 */
	public Integer getCost() {
		return cost;
	}

	/**
	 * Establece o modifica el precio de tokens para la transacción del cromo.
	 * 
	 * @param cost El nuevo valor numérico del costo.
	 */
	public void setCost(Integer cost) {
		this.cost = cost;
	}

	/**
	 * Obtiene la dirección URL del recurso multimedia del cromo.
	 * 
	 * @return Una cadena de texto con el enlace de la imagen.
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	/**
	 * Define la ubicación URL pública para renderizar la ilustración o fotografía
	 * de la lámina.
	 * 
	 * @param imageUrl La cadena de texto con la ruta de la imagen.
	 */
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	/**
	 * Obtiene la demarcación o rol táctico en la cancha asignado al jugador.
	 * 
	 * @return La posición futbolística textual del jugador.
	 */
	public String getPosition() {
		return position;
	}

	/**
	 * Define o modifica la posición de juego del futbolista.
	 * 
	 * @param position La nueva cadena de texto descriptiva de la posición.
	 */
	public void setPosition(String position) {
		this.position = position;
	}

	/**
	 * Obtiene el registro temporal exacto de inserción del sticker en el sistema.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora de creación.
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Establece la marca de tiempo de creación física de la lámina.
	 * 
	 * @param createdAt Objeto {@link LocalDateTime} con los datos temporales
	 *                  iniciales.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene el registro temporal exacto de la última alteración de los datos del
	 * sticker.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora de modificación.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Establece la marca de tiempo para auditoría de modificaciones del cromo.
	 * 
	 * @param updatedAt Objeto {@link LocalDateTime} correspondiente al instante de
	 *                  alteración.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Genera una representación textual con los valores actuales de todas las
	 * propiedades lógicas y técnicas encapsuladas en la instancia del DTO.
	 * * @return Una cadena de texto formateada con los atributos del objeto.
	 */
	@Override
	public String toString() {
		return "StickerDTO [id=" + id + ", playerName=" + playerName + ", rarity=" + rarity + ", teamId=" + teamId
				+ ", position=" + position + ", cost=" + cost + ", imageUrl=" + imageUrl + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}

}