package co.edu.unbosque.mundialhub.exception;

/**
 * Clase centralizada para validaciones y excepciones de negocio
 * del sistema VeltrixPass.
 */
public class Exceptions {



    public boolean containsRole(String rol) {
        return rol != null && (
               rol.equals("ADMIN") ||
               rol.equals("USER") ||
               rol.equals("TEMPORAL") ||
               rol.equals("CHECKER"));
    }

    public boolean containsHtmlSymbols(String text) {
        if (text == null || text.isBlank()) return false;
        return text.matches(".*[<>&\"'/=].*");
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        String forbidden = "<>&\"'/= ";

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (forbidden.indexOf(c) >= 0) return false;
            else hasSymbol = true;
        }

        return hasLower && hasUpper && hasDigit && hasSymbol;
    }

    public boolean isGmailValid(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("(?i)^[a-zA-Z0-9._%+-]+@gmail\\.com$");
    }


    public static class ResourceNotFoundException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class ValidationException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ValidationException(String message) {
            super(message);
        }
    }

    public static class ConflictException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ConflictException(String message) {
            super(message);
        }
    }


    public static class TicketNotFoundException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public TicketNotFoundException(Integer integer) {
            super("Ticket no encontrado con ID: " + integer);
        }

        public TicketNotFoundException(String qr) {
            super("Ticket no encontrado para el QR: " + qr);
        }

		public TicketNotFoundException(Long id) {
            super("Ticket no encontrado con ID: " + id);

		}
    }

    public static class TicketAlreadyUsedException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public TicketAlreadyUsedException() {
            super("El ticket ya fue utilizado");
        }
    }

    public static class TicketExpiredException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public TicketExpiredException() {
            super("El QR del ticket ha expirado");
        }
    }



    public static class PaymentNotFoundException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PaymentNotFoundException(Long id) {
            super("Pago no encontrado con ID MercadoPago: " + id);
        }
    }

    public static class PaymentInvalidStatusException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PaymentInvalidStatusException(String status) {
            super("Estado de pago inválido: " + status);
        }
    }



    public static class UserAlreadyExistsException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UserAlreadyExistsException(String field) {
            super("El " + field + " ya está registrado");
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UserNotFoundException(String value) {
            super("Usuario no encontrado: " + value);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public InvalidCredentialsException() {
            super("Credenciales inválidas");
        }
    }

    public static class EventNotFoundException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EventNotFoundException(long event) {
            super("Evento no encontrado con ID: " + event);
        }
    }

    public static class EventExpiredException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EventExpiredException() {
            super("El evento ya ha finalizado");
        }
    }

    public static class EventNotActiveException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EventNotActiveException() {
            super("El evento no se encuentra activo");
        }
    }

    public static class EventCapacityExceededException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EventCapacityExceededException() {
            super("El evento ya alcanzó su capacidad máxima");
        }
    }

    public static class EventAlreadyExistsException extends RuntimeException {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EventAlreadyExistsException(String name) {
            super("Ya existe un evento con el nombre: " + name);
        }
    }
}
