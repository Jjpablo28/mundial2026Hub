package co.edu.unbosque.mundialhub.exception;

/**
 * Clase centralizada para validaciones y excepciones de negocio del sistema
 * VeltrixPass.
 * <p>
 * Actúa como el núcleo de control de políticas de seguridad y excepciones
 * personalizadas del sistema. Provee métodos utilitarios para sanitizar cadenas
 * contra ataques XSS (HTML), validar la robustez de contraseñas, evaluar
 * dominios de correo específicos y mapear un catálogo estructurado de
 * excepciones en tiempo de ejecución ({@link RuntimeException}) para tickets,
 * eventos, pagos y usuarios.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
public class Exceptions {

	/**
	 * Evalúa si una cadena de texto representa un rol de seguridad válido y
	 * configurado en la infraestructura del sistema central.
	 *
	 * @param rol Nombre del rol a evaluar.
	 * @return {@code true} si el rol coincide exactamente con ADMIN, USER, TEMPORAL
	 *         o CHECKER; {@code false} en caso contrario o si es nulo.
	 */
	public boolean containsRole(String rol) {
		return rol != null
				&& (rol.equals("ADMIN") || rol.equals("USER") || rol.equals("TEMPORAL") || rol.equals("CHECKER"));
	}

	/**
	 * Analiza una cadena de caracteres para determinar si contiene símbolos o
	 * marcadores sintácticos propios de HTML o scripts, ayudando a prevenir
	 * vulnerabilidades de Cross-Site Scripting (XSS).
	 *
	 * @param text Cadena de texto sujeta a inspección.
	 * @return {@code true} si contiene caracteres prohibidos como
	 *         {@code <, >, &, ", ', /, =}; {@code false} si está limpia, vacía o es
	 *         nula.
	 */
	public boolean containsHtmlSymbols(String text) {
		if (text == null || text.isBlank())
			return false;
		return text.matches(".*[<>&\"'/=].*");
	}

	/**
	 * Aplica las reglas corporativas de políticas de seguridad para validar la
	 * robustez de una contraseña propuesta por un usuario.
	 * <p>
	 * La contraseña requiere una longitud mínima de 8 caracteres y la cooficialidad
	 * de al menos una letra minúscula, una letra mayúscula, un dígito numérico y un
	 * símbolo especial permitido. Rechaza de forma fulminante la presencia de
	 * espacios o símbolos de inyección HTML.
	 * </p>
	 *
	 * @param password Contraseña en texto plano que se va a evaluar.
	 * @return {@code true} si cumple estrictamente con todos los criterios de
	 *         seguridad exigidos; {@code false} si incumple alguna regla o contiene
	 *         caracteres explícitamente prohibidos.
	 */
	public boolean isValidPassword(String password) {
		if (password == null || password.length() < 8)
			return false;

		boolean hasLower = false;
		boolean hasUpper = false;
		boolean hasDigit = false;
		boolean hasSymbol = false;

		String forbidden = "<>&\"'/= ";

		for (char c : password.toCharArray()) {
			if (Character.isLowerCase(c))
				hasLower = true;
			else if (Character.isUpperCase(c))
				hasUpper = true;
			else if (Character.isDigit(c))
				hasDigit = true;
			else if (forbidden.indexOf(c) >= 0)
				return false;
			else
				hasSymbol = true;
		}

		return hasLower && hasUpper && hasDigit && hasSymbol;
	}

	/**
	 * Verifica por medio de expresiones regulares si un correo electrónico posee
	 * una estructura sintáctica válida y pertenece de forma exclusiva al dominio
	 * global de Google (Gmail).
	 *
	 * @param email Dirección de correo electrónico a evaluar.
	 * @return {@code true} si se acopla al formato estándar de cuenta de Gmail
	 *         (insensible a mayúsculas); {@code false} en caso de discrepancia
	 *         sintáctica, vacío o valor nulo.
	 */
	public boolean isGmailValid(String email) {
		if (email == null || email.isBlank())
			return false;
		return email.matches("(?i)^[a-zA-Z0-9._%+-]+@gmail\\.com$");
	}

	/**
	 * Excepción de negocio lanzada cuando un recurso genérico solicitado no se
	 * encuentra disponible en la persistencia.
	 */
	public static class ResourceNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción con un mensaje detallado del recurso faltante.
		 * 
		 * @param message Texto explicativo del error.
		 */
		public ResourceNotFoundException(String message) {
			super(message);
		}
	}

	/**
	 * Excepción lanzada al fallar los filtros sanitarios de datos o las reglas
	 * básicas de validación.
	 */
	public static class ValidationException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción con la descripción de la regla de validación violada.
		 * 
		 * @param message Texto descriptivo del fallo de formato o semántica.
		 */
		public ValidationException(String message) {
			super(message);
		}
	}

	/**
	 * Excepción representativa de conflictos de estado o transacciones concurrentes
	 * inválidas en el negocio.
	 */
	public static class ConflictException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción con un mensaje que especifica el conflicto detectado.
		 * 
		 * @param message Motivo del estado conflictivo.
		 */
		public ConflictException(String message) {
			super(message);
		}
	}

	/**
	 * Excepción polimórfica lanzada cuando un ticket de acceso a eventos deportivos
	 * o culturales no es localizado en los almacenes de datos.
	 */
	public static class TicketNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción a partir de un ID de tipo entero.
		 * 
		 * @param integer Identificador numérico del ticket.
		 */
		public TicketNotFoundException(Integer integer) {
			super("Ticket no encontrado con ID: " + integer);
		}

		/**
		 * Construye la excepción a partir de la firma de una cadena de código QR.
		 * 
		 * @param qr Cadena de texto desencriptada o hash del QR.
		 */
		public TicketNotFoundException(String qr) {
			super("Ticket no encontrado para el QR: " + qr);
		}

		/**
		 * Construye la excepción a partir de un ID de tipo entero largo.
		 * 
		 * @param id Identificador numérico largo de base de datos.
		 */
		public TicketNotFoundException(Long id) {
			super("Ticket no encontrado con ID: " + id);

		}
	}

	/**
	 * Excepción de control de accesos lanzada al intentar validar o redimir un
	 * ticket que ya registra ingresos previos en los molinetes/lectores.
	 */
	public static class TicketAlreadyUsedException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción con un mensaje estático de restricción por duplicidad
		 * de uso.
		 */
		public TicketAlreadyUsedException() {
			super("El ticket ya fue utilizado");
		}
	}

	/**
	 * Excepción de control cronológico lanzada si el código QR o el derecho de
	 * acceso temporal del ticket ha superado su ventana horaria admisible.
	 */
	public static class TicketExpiredException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción con un mensaje explicativo sobre la caducidad del
		 * derecho de acceso.
		 */
		public TicketExpiredException() {
			super("El QR del ticket ha expirado");
		}
	}

	/**
	 * Excepción de auditoría financiera lanzada cuando no se encuentra el registro
	 * de cobro emitido por la pasarela de Mercado Pago.
	 */
	public static class PaymentNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción anexando el ID oficial devuelto por la API de Mercado
		 * Pago.
		 * 
		 * @param id Código único de la transacción monetaria externa.
		 */
		public PaymentNotFoundException(Long id) {
			super("Pago no encontrado con ID MercadoPago: " + id);
		}
	}

	/**
	 * Excepción de pasarela lanzada cuando el webhook o el flujo local recibe un
	 * estado financiero desconocido, corrupto o no homologado.
	 */
	public static class PaymentInvalidStatusException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción adjuntando el estado erróneo capturado en el payload.
		 * 
		 * @param status Cadena con la nomenclatura del estado anómalo.
		 */
		public PaymentInvalidStatusException(String status) {
			super("Estado de pago inválido: " + status);
		}
	}

	/**
	 * Excepción de unicidad lanzada en procesos de registro de usuarios cuando
	 * campos indexados exclusivos (username, email, cédula) ya se encuentran
	 * ocupados en el repositorio maestro.
	 */
	public static class UserAlreadyExistsException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción especificando el campo específico que generó la
		 * colisión.
		 * 
		 * @param field Nombre de la propiedad en conflicto (ej. "email", "username").
		 */
		public UserAlreadyExistsException(String field) {
			super("El " + field + " ya está registrado");
		}
	}

	/**
	 * Excepción de localización de cuentas lanzada al buscar un perfil por
	 * criterios únicos y no obtener resultados.
	 */
	public static class UserNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción indicando el criterio de búsqueda que no arrojó
		 * coincidencias.
		 * 
		 * @param value Valor textual (correo, username) utilizado en la consulta
		 *              fallida.
		 */
		public UserNotFoundException(String value) {
			super("Usuario no encontrado: " + value);
		}
	}

	/**
	 * Excepción clásica de seguridad e inicio de sesión arrojada ante contraseñas
	 * incorrectas o desajustes en la validación criptográfica de identidades.
	 */
	public static class InvalidCredentialsException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción con un mensaje genérico para evitar dar pistas de
		 * enumeración a atacantes externos.
		 */
		public InvalidCredentialsException() {
			super("Credenciales inválidas");
		}
	}

	/**
	 * Excepción de gestión logística lanzada cuando un evento agendado no se
	 * encuentra en el inventario global.
	 */
	public static class EventNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción mapeando el identificador numérico ausente.
		 * 
		 * @param event ID único del evento buscado.
		 */
		public EventNotFoundException(long event) {
			super("Evento no encontrado con ID: " + event);
		}
	}

	/**
	 * Excepción temporal arrojada al intentar comercializar, modificar o
	 * interactuar con un espectáculo cuya fecha de ejecución ya concluyó en la
	 * línea de tiempo real.
	 */
	public static class EventExpiredException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción informando del cierre definitivo de la línea temporal
		 * del evento.
		 */
		public EventExpiredException() {
			super("El evento ya ha finalizado");
		}
	}

	/**
	 * Excepción lógica lanzada cuando se intenta realizar transacciones con un
	 * evento que se encuentra en estado de borrador, suspendido o deshabilitado por
	 * la administración.
	 */
	public static class EventNotActiveException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción señalando la indisponibilidad operativa del evento.
		 */
		public EventNotActiveException() {
			super("El evento no se encuentra activo");
		}
	}

	/**
	 * Excepción de control de aforos lanzada en el flujo de ventas si el volumen de
	 * solicitudes de compra sobrepasa la capacidad total establecida para el
	 * recinto.
	 */
	public static class EventCapacityExceededException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción detallando el agotamiento de las plazas o boletería
		 * libre.
		 */
		public EventCapacityExceededException() {
			super("El evento ya alcanzó su capacidad máxima");
		}
	}

	/**
	 * Excepción de control de catálogos arrojada para impedir que se creen dos
	 * eventos con nombres homónimos que puedan distorsionar la indexación o
	 * confundir a los compradores.
	 */
	public static class EventAlreadyExistsException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		/**
		 * Construye la excepción incorporando el nombre del evento redundante.
		 * 
		 * @param name Nombre comercial duplicado detectado.
		 */
		public EventAlreadyExistsException(String name) {
			super("Ya existe un evento con el nombre: " + name);
		}
	}
}