package co.edu.unbosque.mundialhub.dto;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para encapsular y
 * transportar de manera estructurada las credenciales requeridas en un proceso
 * de cambio de contraseña.
 * <p>
 * Mapea directamente el cuerpo de las peticiones HTTP de tipo PUT en los
 * controladores de usuario, agrupando la contraseña actual (para validación de
 * identidad en el backend) junto con la nueva clave propuesta por el cliente,
 * asegurando la integridad del flujo de seguridad.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
public class UpdatePasswordDTO {

	/**
	 * Contraseña o clave secreta actual en texto plano utilizada para verificar la
	 * identidad del usuario.
	 */
	private String oldPassword;

	/**
	 * Nueva contraseña o clave secreta en texto plano que se desea persistir en el
	 * sistema.
	 */
	private String newPassword;

	/**
	 * Constructor por defecto (sin parámetros) requerido obligatoriamente por los
	 * frameworks de serialización y deserialización de datos (como Jackson) para
	 * transformar las peticiones JSON.
	 */
	public UpdatePasswordDTO() {
		super();
	}

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización
	 * directa de las claves de verificación y actualización.
	 *
	 * @param oldPassword Contraseña antigua/actual del usuario.
	 * @param newPassword Nueva contraseña solicitada.
	 */
	public UpdatePasswordDTO(String oldPassword, String newPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	/**
	 * Obtiene la contraseña antigua o actual registrada en la solicitud.
	 * 
	 * @return Una cadena de texto con la clave original de verificación.
	 */
	public String getOldPassword() {
		return oldPassword;
	}

	/**
	 * Define o modifica la contraseña antigua o actual requerida para la
	 * validación.
	 * 
	 * @param oldPassword La clave secreta actual ingresada por el usuario.
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	/**
	 * Obtiene la nueva contraseña propuesta por el usuario.
	 * 
	 * @return Una cadena de texto con la nueva clave a guardar.
	 */
	public String getNewPassword() {
		return newPassword;
	}

	/**
	 * Define o modifica la nueva contraseña que sustituirá a la anterior en el
	 * sistema.
	 * 
	 * @param newPassword La nueva clave secreta que cumple con las políticas de
	 *                    seguridad.
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * Genera una representación textual formateada de los atributos de la
	 * instancia.
	 * <p>
	 * Nota de seguridad: Se recomienda discreción al utilizar este método en
	 * entornos de producción, ya que podría exponer contraseñas en texto plano
	 * dentro de los archivos de log.
	 * </p>
	 *
	 * @return Una cadena de texto con las propiedades encapsuladas en el DTO.
	 */
	@Override
	public String toString() {
		return "UpdatePasswordDTO [oldPassword=" + oldPassword + ", newPassword=" + newPassword + "]";
	}

}