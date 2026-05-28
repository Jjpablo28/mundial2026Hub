package co.edu.unbosque.mundialhub.exception;

/**
 * Clase centralizada para validaciones de entrada y excepciones de negocio
 * del sistema MundialHub.
 * <p>
 * Actúa como núcleo de control de políticas de seguridad y excepciones
 * personalizadas. Provee métodos utilitarios para sanitizar cadenas contra
 * ataques XSS, validar robustez de contraseñas, evaluar dominios de correo
 * y mapear un catálogo estructurado de excepciones en tiempo de ejecución
 * ({@link RuntimeException}) para tickets, eventos, pagos y usuarios.
 * </p>
 * <p>
 * <b>Separación de responsabilidades:</b>
 * <ul>
 *   <li>Los métodos estáticos de validación son usados por los Controllers
 *       como primera línea de defensa (formato e input).</li>
 *   <li>Las inner classes de excepción son lanzadas desde los Services
 *       para representar fallos de reglas de negocio.</li>
 * </ul>
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
public class Exceptions {

    /**
     * Evalúa si una cadena representa un rol de seguridad válido y configurado
     * en la infraestructura del sistema.
     *
     * @param rol Nombre del rol a evaluar.
     * @return {@code true} si el rol coincide exactamente con ADMIN, USER,
     *         TEMPORAL o CHECKER; {@code false} en caso contrario o si es nulo.
     */
    public boolean containsRole(String rol) {
        return rol != null
                && (rol.equals("ADMIN")
                        || rol.equals("USER")
                        || rol.equals("TEMPORAL")
                        || rol.equals("CHECKER"));
    }

    /**
     * Analiza una cadena para determinar si contiene símbolos o marcadores
     * sintácticos propios de HTML o scripts, ayudando a prevenir vulnerabilidades
     * de Cross-Site Scripting (XSS) e inyección de código.
     *
     * @param text Cadena de texto sujeta a inspección.
     * @return {@code true} si contiene caracteres prohibidos como
     *         {@code <, >, &, ", ', /, =}; {@code false} si está limpia,
     *         vacía o es nula.
     */
    public boolean containsHtmlSymbols(String text) {
        if (text == null || text.isBlank())
            return false;
        return text.matches(".*[<>&\"'/=].*");
    }

    /**
     * Aplica las reglas corporativas de seguridad para validar la robustez de
     * una contraseña.
     * <p>
     * Requisitos obligatorios:
     * <ul>
     *   <li>Longitud mínima de 8 caracteres.</li>
     *   <li>Al menos una letra minúscula.</li>
     *   <li>Al menos una letra mayúscula.</li>
     *   <li>Al menos un dígito numérico.</li>
     *   <li>Al menos un símbolo especial (distinto a espacios y caracteres HTML).</li>
     *   <li>No puede ser una secuencia puramente numérica (ej. 12345678).</li>
     *   <li>No puede contener espacios.</li>
     * </ul>
     * </p>
     *
     * @param password Contraseña en texto plano a evaluar.
     * @return {@code true} si cumple todos los criterios; {@code false} si
     *         incumple alguna regla o contiene caracteres prohibidos.
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

        // Rechaza secuencias puramente numéricas como "123456789"
        if (password.matches("\\d+"))
            return false;

        return hasLower && hasUpper && hasDigit && hasSymbol;
    }

    /**
     * Verifica si un correo electrónico posee estructura sintáctica válida y
     * pertenece exclusivamente al dominio Gmail.
     *
     * @param email Dirección de correo electrónico a evaluar.
     * @return {@code true} si se acopla al formato estándar de Gmail
     *         (insensible a mayúsculas); {@code false} en caso de discrepancia,
     *         vacío o nulo.
     */
 // Después — cualquier dominio válido
    public boolean isGmailValid(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("(?i)^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    /**
     * Valida que una cadena de texto no sea nula, no esté vacía y no contenga
     * caracteres HTML peligrosos. Útil para campos de texto libre como nombres,
     * descripciones o identificadores de sesión.
     *
     * @param value Cadena a validar.
     * @return {@code true} si el valor es seguro y no está vacío;
     *         {@code false} si es nulo, vacío o contiene símbolos HTML.
     */
    public boolean isSafeText(String value) {
        if (value == null || value.isBlank())
            return false;
        return !containsHtmlSymbols(value);
    }

    /**
     * Valida que un identificador numérico sea positivo y no nulo.
     * Previene consultas o operaciones con IDs inválidos como 0 o negativos.
     *
     * @param id Identificador numérico a validar.
     * @return {@code true} si el ID es mayor que cero; {@code false} en caso
     *         contrario o si es nulo.
     */
    public boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    // =========================================================================
    // EXCEPCIONES DE NEGOCIO
    // =========================================================================

    /**
     * Excepción lanzada cuando un recurso genérico solicitado no se encuentra
     * disponible en la persistencia.
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
     * básicas de validación de entrada.
     */
    public static class ValidationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción con la descripción de la regla violada.
         *
         * @param message Texto descriptivo del fallo de formato o semántica.
         */
        public ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Excepción representativa de conflictos de estado o transacciones
     * concurrentes inválidas en el negocio.
     */
    public static class ConflictException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción con un mensaje que especifica el conflicto.
         *
         * @param message Motivo del estado conflictivo.
         */
        public ConflictException(String message) {
            super(message);
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
     * Excepción lanzada cuando el webhook recibe un estado financiero
     * desconocido, corrupto o no homologado.
     */
    public static class PaymentInvalidStatusException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción con el estado erróneo capturado.
         *
         * @param status Cadena con la nomenclatura del estado anómalo.
         */
        public PaymentInvalidStatusException(String status) {
            super("Estado de pago inválido: " + status);
        }
    }

    /**
     * Excepción lanzada en procesos de registro cuando campos indexados
     * exclusivos (username, email) ya se encuentran ocupados en el repositorio.
     */
    public static class UserAlreadyExistsException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción especificando el campo en conflicto.
         *
         * @param field Nombre de la propiedad en conflicto (ej. "email", "username").
         */
        public UserAlreadyExistsException(String field) {
            super("El " + field + " ya está registrado");
        }
    }

    /**
     * Excepción lanzada al buscar un perfil por criterios únicos sin obtener
     * resultados.
     */
    public static class UserNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción con el criterio de búsqueda fallido.
         *
         * @param value Valor textual (correo, username) utilizado en la consulta.
         */
        public UserNotFoundException(String value) {
            super("Usuario no encontrado: " + value);
        }
    }

    /**
     * Excepción lanzada ante contraseñas incorrectas o desajustes en la
     * validación criptográfica de identidades.
     */
    public static class InvalidCredentialsException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción con mensaje genérico para evitar enumeración
         * de usuarios por parte de atacantes.
         */
        public InvalidCredentialsException() {
            super("Credenciales inválidas");
        }
    }

    /**
     * Excepción lanzada cuando una sesión de intercambio QR no se encuentra
     * en el repositorio o ya expiró.
     */
    public static class TradeSessionNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción con el UUID de sesión que no pudo localizarse.
         *
         * @param uuid Identificador único de la sesión de intercambio buscada.
         */
        public TradeSessionNotFoundException(String uuid) {
            super("Sesión de intercambio no encontrada con UUID: " + uuid);
        }
    }

    /**
     * Excepción lanzada cuando se intenta realizar un intercambio pero el
     * inventario del usuario no contiene las láminas requeridas.
     */
    public static class InsufficientStickersException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Construye la excepción indicando el ID del usuario con inventario
         * insuficiente.
         *
         * @param userId ID del usuario que no tiene las láminas necesarias.
         */
        public InsufficientStickersException(Long userId) {
            super("El usuario con ID " + userId + " no tiene suficientes láminas para el intercambio");
        }
    }

   
}