package co.edu.unbosque.mundialhub.dto;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para capturar y
 * transportar las credenciales de autenticación enviadas por el usuario al
 * intentar iniciar sesión.
 * <p>
 * Agrupa de forma estructurada el identificador del usuario (que puede
 * corresponder polimórficamente al apodo/username o al correo electrónico)
 * junto con la contraseña en texto plano, facilitando su procesamiento síncrono
 * en los controladores de seguridad de la plataforma MundialHub.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
public class LoguinRequestDTO {

	/**
	 * * Identificador único de acceso provisto por el usuario. Puede representar la
	 * dirección de correo electrónico o el nombre de usuario (username).
	 */
	private String identifier;

	/**
	 * Contraseña o clave secreta en texto plano asociada a la cuenta del usuario.
	 */
	private String password;

	/**
	 * Constructor por defecto (sin parámetros) requerido obligatoriamente para los
	 * procesos de serialización y deserialización de frameworks de mapeo JSON como
	 * Jackson.
	 */
	public LoguinRequestDTO() {
	}

	/**
	 * Obtiene el identificador de acceso (username o email) registrado en la
	 * solicitud.
	 * 
	 * @return Una cadena de texto con el identificador del usuario.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Establece o modifica el identificador de acceso para el inicio de sesión.
	 * 
	 * @param identifier El nombre de usuario o correo electrónico provisto por el
	 *                   cliente.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Obtiene la contraseña en texto plano registrada en la solicitud.
	 * 
	 * @return Una cadena de texto con la contraseña.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Establece o modifica la contraseña provista para la autenticación de la
	 * sesión.
	 * 
	 * @param password La clave secreta ingresada por el cliente.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}