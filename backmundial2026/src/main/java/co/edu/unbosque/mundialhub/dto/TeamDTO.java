package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

import co.edu.unbosque.mundialhub.model.GroupName;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para estructurar,
 * transportar y exponer la información correspondiente a una selección nacional
 * de fútbol (equipo) en MundialHub.
 * <p>
 * Consolida tanto los metadatos de identidad del equipo (nombre, siglas,
 * escudo), como su posicionamiento competitivo (grupo asignado, puntos
 * acumulados y clasificación en el Ranking FIFA), junto con sus respectivas
 * marcas de tiempo para el control de auditorías lógicas.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see GroupName
 * @see LocalDateTime
 */
public class TeamDTO {

	/**
	 * Identificador numérico único (llave primaria) de la selección nacional en el
	 * sistema.
	 */
	private Long id;

	/**
	 * Nombre oficial de la selección nacional de fútbol (ej. Colombia, Argentina).
	 */
	private String name;

	/**
	 * Sigla o código de abreviación internacional representativo del equipo (ej.
	 * COL, ARG).
	 */
	private String shortName;

	/**
	 * Dirección URL pública donde se almacena el recurso gráfico o logotipo del
	 * escudo del equipo.
	 */
	private String crest;

	/** Nombre del país de origen al que representa la federación deportiva. */
	private String country;

	/**
	 * Identificador o etiqueta del grupo del torneo asignado al equipo, basado en
	 * el enumerador {@link GroupName}.
	 */
	private GroupName groupName;

	/**
	 * Posición numérica oficial que ocupa la selección en el escalafón global del
	 * Ranking FIFA.
	 */
	private Integer fifaRanking;

	/**
	 * Cantidad de puntos acumulados en la fase de grupos del torneo (útil para la
	 * construcción de tablas de posiciones).
	 */
	private Integer points;

	/**
	 * Marca de tiempo exacta que registra la creación original del equipo en la
	 * base de datos.
	 */
	private LocalDateTime createdAt;

	/**
	 * Marca de tiempo exacta que registra la última modificación de los datos del
	 * equipo.
	 */
	private LocalDateTime updatedAt;

	/**
	 * Constructor por defecto (sin parámetros) requerido por los frameworks de
	 * persistencia, mapeo de objetos de datos y serialización/deserialización JSON
	 * (como Jackson).
	 */
	public TeamDTO() {
		super();
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización de
	 * todos los atributos del DTO del equipo.
	 *
	 * @param id          ID único de la selección nacional.
	 * @param name        Nombre oficial del equipo.
	 * @param shortName   Código o acrónimo corto de tres letras.
	 * @param crest       Enlace URL de la imagen del escudo.
	 * @param country     País al que pertenece el equipo.
	 * @param groupName   Grupo de competición asignado (Enum {@link GroupName}).
	 * @param fifaRanking Posición actual en el escalafón FIFA.
	 * @param points      Puntaje acumulado en el torneo.
	 * @param createdAt   Fecha y hora de inserción del registro en el sistema.
	 * @param updatedAt   Fecha y hora de la última modificación en el sistema.
	 */
	public TeamDTO(Long id, String name, String shortName, String crest, String country, GroupName groupName,
			Integer fifaRanking, Integer points, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.shortName = shortName;
		this.crest = crest;
		this.country = country;
		this.groupName = groupName;
		this.fifaRanking = fifaRanking;
		this.points = points;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Obtiene el nombre oficial de la selección de fútbol.
	 * 
	 * @return Una cadena de texto con el nombre del equipo.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Define o modifica el nombre oficial de la selección de fútbol.
	 * 
	 * @param name El nuevo nombre del equipo.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtiene el acrónimo o abreviatura corta internacional del equipo.
	 * 
	 * @return El código corto del equipo (ej. "COL").
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Define o modifica la sigla o abreviación corta de la selección nacional.
	 * 
	 * @param shortName La nueva sigla internacional de tres letras.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * Obtiene la ubicación URL del archivo de imagen correspondiente al escudo.
	 * 
	 * @return La dirección URL del recurso multimedia.
	 */
	public String getCrest() {
		return crest;
	}

	/**
	 * Establece la ruta URL pública para renderizar el escudo oficial del equipo.
	 * 
	 * @param crest Cadena de texto con el enlace del escudo.
	 */
	public void setCrest(String crest) {
		this.crest = crest;
	}

	/**
	 * Obtiene el nombre del país asociado a la selección nacional.
	 * 
	 * @return El nombre del territorio nacional.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Define o modifica el país de origen de la selección.
	 * 
	 * @param country El nuevo nombre del país.
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Obtiene el sector o grupo del campeonato de la Copa del Mundo asignado al
	 * equipo.
	 * 
	 * @return El grupo correspondiente basado en el enumerador {@link GroupName}.
	 */
	public GroupName getGroupName() {
		return groupName;
	}

	/**
	 * Asigna la selección nacional a un grupo específico del torneo mundialista.
	 * 
	 * @param groupName El nuevo grupo de la fase clasificatoria (Enum
	 *                  {@link GroupName}).
	 */
	public void setGroupName(GroupName groupName) {
		this.groupName = groupName;
	}

	/**
	 * Obtiene el puesto oficial que ostenta el equipo en el Ranking FIFA.
	 * 
	 * @return Un número entero con la posición de la clasificación internacional.
	 */
	public Integer getFifaRanking() {
		return fifaRanking;
	}

	/**
	 * Modifica la posición de la selección nacional en el escalafón global de la
	 * FIFA.
	 * 
	 * @param fifaRanking El nuevo puesto numérico en el ranking.
	 */
	public void setFifaRanking(Integer fifaRanking) {
		this.fifaRanking = fifaRanking;
	}

	/**
	 * Obtiene la puntuación competitiva acumulada por el equipo dentro de la
	 * competición.
	 * 
	 * @return El número entero con los puntos actuales de la tabla de posiciones.
	 */
	public Integer getPoints() {
		return points;
	}

	/**
	 * Establece o actualiza la cantidad de puntos del equipo para los cálculos de
	 * clasificación.
	 * 
	 * @param points El nuevo valor numérico del puntaje acumulado.
	 */
	public void setPoints(Integer points) {
		this.points = points;
	}

	/**
	 * Obtiene la marca de tiempo exacta correspondiete a la creación del registro.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la información de fecha y hora
	 *         iniciales.
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Establece la marca de tiempo de creación física del equipo en el sistema.
	 * 
	 * @param createdAt Objeto {@link LocalDateTime} con los datos temporales
	 *                  iniciales.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene la marca de tiempo que registra la modificación más reciente de los
	 * datos de la selección.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora de la última
	 *         alteración.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Establece la marca de tiempo para el control de auditoría de modificaciones
	 * del equipo.
	 * 
	 * @param updatedAt Objeto {@link LocalDateTime} correspondiente al instante de
	 *                  alteración.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Obtiene el identificador numérico único de la selección nacional.
	 * 
	 * @return Un entero largo con la llave primaria del equipo.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Define o modifica el identificador numérico del equipo en el sistema.
	 * 
	 * @param id El nuevo ID de base de datos.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Genera una representación textual formateada que expone el estado actual de
	 * todas las propiedades lógicas y numéricas encapsuladas en la instancia.
	 * 
	 * @return Una cadena de texto con los nombres y valores de los atributos del
	 *         DTO.
	 */
	@Override
	public String toString() {
		return "TeamDTO [id=" + id + ", name=" + name + ", shortName=" + shortName + ", crest=" + crest + ", country="
				+ country + ", groupName=" + groupName + ", fifaRanking=" + fifaRanking + ", points=" + points
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}

}