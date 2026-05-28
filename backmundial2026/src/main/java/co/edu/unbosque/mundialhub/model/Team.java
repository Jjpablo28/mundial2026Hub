package co.edu.unbosque.mundialhub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad de persistencia que representa la tabla "teams" en la base de datos
 * de MundialHub.
 * <p>
 * Define la estructura relacional para gestionar la información de las
 * selecciones nacionales de fútbol que compiten en el torneo. Almacena
 * identidades únicas (nombres, siglas oficiales), los recursos gráficos de sus
 * insignias, parámetros de competitividad (puntos y clasificación FIFA) y su
 * encuadre logístico en los grupos del campeonato.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see Entity
 * @see Table
 * @see GroupName
 */
@Entity
@Table(name = "teams")
public class Team {

	/**
	 * Identificador numérico único y autoincremental (llave primaria) de la
	 * selección en la base de datos.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Nombre oficial y único de la selección nacional de fútbol. No admite valores
	 * nulos.
	 */
	@Column(unique = true, nullable = false)
	private String name;

	/**
	 * Sigla o código acrónimo internacional único de tres letras representativo del
	 * equipo (ej. COL, ARG).
	 */
	@Column(unique = true)
	private String shortName; // Ej: COL, ARG

	/**
	 * Dirección URL pública o ruta del recurso digital correspondiente al escudo o
	 * logotipo oficial de la selección.
	 */
	private String crest; // URL del escudo

	/** Nombre del país al que representa la federación o delegación deportiva. */
	private String country;

	/**
	 * Identificador del grupo del torneo asignado al equipo, almacenado en la base
	 * de datos como una cadena de texto (String).
	 */
	@Enumerated(EnumType.STRING)
	private GroupName groupName;

	/**
	 * Posición numérica que ocupa la selección en el escalafón global del Ranking
	 * FIFA.
	 */
	private Integer fifaRanking;

	/**
	 * Cantidad de puntos acumulados por el equipo dentro de la competición (útil
	 * para la clasificación en tablas de posiciones).
	 */
	private Integer points; // útil para tablas

	/**
	 * Marca de tiempo exacta que registra la inserción y creación original del
	 * equipo en la base de datos.
	 */
	private LocalDateTime createdAt;

	/**
	 * Marca de tiempo exacta que audita la última modificación aplicada a los datos
	 * de la selección.
	 */
	private LocalDateTime updatedAt;

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización de
	 * una selección nacional con todas sus variables competitivas, de identidad y
	 * marcas de tiempo de auditoría.
	 *
	 * @param id          ID único del equipo.
	 * @param name        Nombre oficial de la selección.
	 * @param shortName   Código o acrónimo internacional (tres letras).
	 * @param crest       Enlace URL de la imagen del escudo.
	 * @param country     País de procedencia de la delegación.
	 * @param groupName   Grupo de competición asignado (Enum {@link GroupName}).
	 * @param fifaRanking Puesto actual en el escalafón de la FIFA.
	 * @param points      Puntos acumulados en la tabla del torneo.
	 * @param createdAt   Fecha y hora de registro inicial en el sistema.
	 * @param updatedAt   Fecha y hora de la última modificación en el sistema.
	 */
	public Team(Long id, String name, String shortName, String crest, String country, GroupName groupName,
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
	 * Constructor por defecto (sin parámetros) exigido por la especificación de
	 * Jakarta Persistence (JPA) para permitir las tareas de reconstrucción de
	 * objetos, consultas reflexivas e hidratación de entidades.
	 */
	public Team() {
		super();
	}

	/**
	 * Obtiene el identificador numérico único de la selección nacional.
	 * 
	 * @return Un entero largo con la llave primaria de la tabla.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Establece el identificador numérico de la selección nacional.
	 * 
	 * @param id El nuevo ID de base de datos a asignar.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el nombre oficial único de la selección.
	 * 
	 * @return Cadena de texto con el nombre del equipo.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Define o modifica el nombre oficial de la selección de fútbol.
	 * 
	 * @param name El nuevo nombre único de la escuadra.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtiene el acrónimo o código corto internacional de tres letras.
	 * 
	 * @return La sigla de la selección (ej. "COL").
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Define o modifica el acrónimo o abreviación corta internacional del equipo.
	 * 
	 * @param shortName La nueva sigla representativa de tres letras.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * Obtiene la dirección URL del escudo oficial.
	 * 
	 * @return Cadena de texto con el enlace al recurso gráfico.
	 */
	public String getCrest() {
		return crest;
	}

	/**
	 * Establece la ubicación URL pública para renderizar el escudo representativo
	 * del equipo.
	 * 
	 * @param crest Cadena de texto con el nuevo enlace del escudo.
	 */
	public void setCrest(String crest) {
		this.crest = crest;
	}

	/**
	 * Obtiene el nombre del país al que representa la federación deportiva.
	 * 
	 * @return El nombre del territorio nacional.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Define el país de origen de la delegación futbolística.
	 * 
	 * @param country El nuevo nombre del país asociado.
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * Obtiene la etiqueta del grupo clasificatorio asignado al equipo en el
	 * campeonato.
	 * 
	 * @return El grupo correspondiente basado en el enumerador {@link GroupName}.
	 */
	public GroupName getGroupName() {
		return groupName;
	}

	/**
	 * Vincula la selección nacional a un grupo específico de la fase inicial del
	 * torneo mundialista.
	 * 
	 * @param groupName El nuevo grupo clasificatorio (Enum {@link GroupName}).
	 */
	public void setGroupName(GroupName groupName) {
		this.groupName = groupName;
	}

	/**
	 * Obtiene el puesto oficial de la selección en el escalafón de la FIFA.
	 * 
	 * @return Un número entero con el puesto del ranking internacional.
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
	 * Obtiene la puntuación competitiva acumulada por la escuadra en el torneo.
	 * 
	 * @return El número entero con los puntos vigentes de la tabla de posiciones.
	 */
	public Integer getPoints() {
		return points;
	}

	/**
	 * Establece o actualiza los puntos del equipo para los cálculos de la tabla de
	 * posiciones.
	 * 
	 * @param points El nuevo valor numérico del puntaje acumulado.
	 */
	public void setPoints(Integer points) {
		this.points = points;
	}

	/**
	 * Obtiene el registro temporal exacto en el que se dio de alta el equipo en la
	 * plataforma.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora de creación.
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Establece la marca de tiempo de creación física de la selección en el
	 * sistema.
	 * 
	 * @param createdAt Objeto {@link LocalDateTime} con la estampa de tiempo
	 *                  inicial.
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Obtiene la marca de tiempo que registra la última alteración de los datos de
	 * la selección.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora de la última
	 *         modificación.
	 */
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Establece la marca de tiempo para el control de auditoría de modificaciones
	 * del equipo.
	 * 
	 * @param updatedAt Objeto {@link LocalDateTime} correspondiente al instante del
	 *                  cambio de datos.
	 */
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Compila y genera una cadena de texto clara que detalla los valores en tiempo
	 * real de todas las variables relacionales y numéricas encapsuladas en la
	 * instancia.
	 * 
	 * @return Una cadena de texto formateada con los atributos de la entidad.
	 */
	@Override
	public String toString() {
		return "Team [id=" + id + ", name=" + name + ", shortName=" + shortName + ", crest=" + crest + ", country="
				+ country + ", groupName=" + groupName + ", fifaRanking=" + fifaRanking + ", points=" + points
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
}