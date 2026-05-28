package co.edu.unbosque.mundialhub.dto;

import java.time.LocalDateTime;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para estructurar,
 * transportar y gestionar la información del inventario de láminas
 * pertenecientes a un usuario.
 * <p>
 * Mapea la relación intermedia de posesión entre las cuentas de usuario y el
 * catálogo maestro de cromos, permitiendo conocer cuántas unidades físicas
 * (repetidas o copias) posee una persona de un cromo en particular, junto con
 * la marca de tiempo de su última adquisición.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see LocalDateTime
 */
public class UserStickerDTO {

	/**
	 * Identificador numérico único (llave primaria) del registro de inventario en
	 * el sistema.
	 */
	private Long id;

	// Usamos IDs para facilitar el envío desde el frontend
	/** Identificador único (ID) del usuario propietario de la lámina. */
	private Long userId;

	/** Identificador único (ID) de la lámina o cromo que posee el usuario. */
	private Long stickerId;
	
	private Long teamId;

	/**
	 * Cantidad total de ejemplares o stock físico que posee el usuario del mismo
	 * sticker (incluye repetidas).
	 */
	private Integer quantity;

	/**
	 * Marca de tiempo exacta que registra cuándo se obtuvo originalmente el cromo o
	 * se actualizó su inventario.
	 */
	private LocalDateTime obtainedAt;

	/**
	 * Constructor por defecto (sin parámetros) requerido por los frameworks de
	 * persistencia, mapeadores de objetos (como ModelMapper) y utilidades de
	 * serialización JSON como Jackson.
	 */
	public UserStickerDTO() {
		super();
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización
	 * directa de todas las propiedades de asignación del cromo al inventario del
	 * usuario.
	 *
	 * @param id         ID único del registro de inventario.
	 * @param userId     ID del usuario dueño del cromo.
	 * @param stickerId  ID del sticker en propiedad.
	 * @param quantity   Volumen o cantidad de copias del mismo cromo.
	 * @param obtainedAt Fecha y hora exacta de adquisición del elemento.
	 */
	public UserStickerDTO(Long id, Long userId, Long stickerId, Long teamId, Integer quantity,
			LocalDateTime obtainedAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.stickerId = stickerId;
		this.teamId = teamId;
		this.quantity = quantity;
		this.obtainedAt = obtainedAt;
	}

	
	/**
	 * Obtiene el identificador numérico de la relación de inventario.
	 * 
	 * @return Un entero largo con el ID de la base de datos.
	 */
	public Long getId() {
		return id;
	}


	/**
	 * Define o modifica el identificador numérico de la relación de inventario.
	 * 
	 * @param id El nuevo ID del registro intermedia.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Obtiene el identificador de la cuenta del usuario propietario.
	 * 
	 * @return El ID de base de datos asociado al usuario.
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * Define o modifica el identificador del usuario propietario de la lámina.
	 * 
	 * @param userId El nuevo ID del usuario a vincular.
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * Obtiene el identificador único del sticker o cromo poseído.
	 * 
	 * @return El ID de base de datos correspondiente a la lámina del catálogo.
	 */
	public Long getStickerId() {
		return stickerId;
	}

	/**
	 * Define o modifica la lámina del catálogo que será asociada al inventario
	 * personal.
	 * 
	 * @param stickerId El nuevo ID del cromo a registrar.
	 */
	public void setStickerId(Long stickerId) {
		this.stickerId = stickerId;
	}

	/**
	 * Obtiene la cantidad de copias que posee el usuario de la lámina actual.
	 * 
	 * @return El número entero con el stock o volumen disponible.
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * Establece o altera el volumen de copias disponibles de la misma lámina en la
	 * colección.
	 * 
	 * @param quantity La nueva cantidad de unidades físicas poseídas.
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	/**
	 * Obtiene la marca cronológica en la que se incorporó el elemento al inventario
	 * del usuario.
	 * 
	 * @return Un objeto {@link LocalDateTime} con la fecha y hora de asignación.
	 */
	public LocalDateTime getObtainedAt() {
		return obtainedAt;
	}

	/**
	 * Establece la marca temporal exacta de la transacción u obtención de la
	 * lámina.
	 * 
	 * @param obtainedAt El instante cronológico del evento de adjudicación.
	 */
	public void setObtainedAt(LocalDateTime obtainedAt) {
		this.obtainedAt = obtainedAt;
	}

	
	
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}



	/**
	 * Genera una representación en formato de texto claro que detalla los valores
	 * en tiempo real de todas las variables de instancia encapsuladas en este DTO
	 * de inventario.
	 * 
	 * @return Una cadena de texto formateada con las propiedades del objeto.
	 */
	@Override
	public String toString() {
		return "UserStickerDTO [id=" + id + ", userId=" + userId + ", stickerId=" + stickerId + ", teamId=" + teamId
				+ ", quantity=" + quantity + ", obtainedAt=" + obtainedAt + "]";
	}


}