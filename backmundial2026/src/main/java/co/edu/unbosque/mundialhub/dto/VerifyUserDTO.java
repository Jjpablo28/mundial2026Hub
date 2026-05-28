package co.edu.unbosque.mundialhub.dto;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) diseñada para encapsular y
 * transportar las credenciales temporales requeridas durante el proceso de
 * verificación de identidad de la cuenta.
 * <p>
 * Se utiliza principalmente en el cuerpo de peticiones síncronas hacia el
 * backend, vinculando la dirección de correo electrónico del usuario con el
 * código OTP (One-Time Password) que le fue enviado previamente, permitiendo
 * así activar cuentas nuevas o validar cambios de seguridad.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
public class VerifyUserDTO {

	/**
	 * Dirección de correo electrónico de la cuenta de usuario sujeta a
	 * verificación.
	 */
	private String email;

	/**
	 * Código alfanumérico temporal (OTP) enviado al usuario para validar su
	 * identidad.
	 */
	private String code;

	/**
	 * Constructor completo parametrizado para la instanciación e inicialización
	 * directa del correo electrónico de destino y su correspondiente token de
	 * verificación.
	 *
	 * @param email Correo electrónico registrado del usuario.
	 * @param code  Código OTP de verificación asignado por el sistema.
	 */
	public VerifyUserDTO(String email, String code) {
		super();
		this.email = email;
		this.code = code;
	}

	/**
	 * Constructor por defecto (sin parámetros) requerido obligatoriamente por los
	 * frameworks de serialización y deserialización de datos (como Jackson) para
	 * procesar payloads JSON en las peticiones HTTP.
	 */
	public VerifyUserDTO() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Obtiene la dirección de correo electrónico registrada para el proceso de
	 * validación.
	 * 
	 * @return Una cadena de texto con el email de la cuenta.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Define o modifica la dirección de correo electrónico que se someterá a
	 * verificación.
	 * 
	 * @param email El nuevo correo electrónico de contacto del usuario.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Obtiene el código OTP de verificación encapsulado en la solicitud.
	 * 
	 * @return Una cadena de texto con el token de seguridad temporal.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Establece o modifica el código OTP requerido para completar la validación de
	 * identidad.
	 * 
	 * @param code El código de verificación alfanumérico provisto por el usuario.
	 */
	public void setCode(String code) {
		this.code = code;
	}

}