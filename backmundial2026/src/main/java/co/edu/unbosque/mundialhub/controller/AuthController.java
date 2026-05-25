package co.edu.unbosque.mundialhub.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import co.edu.unbosque.mundialhub.dto.LoguinRequestDTO;
import co.edu.unbosque.mundialhub.dto.UserDTO;
import co.edu.unbosque.mundialhub.dto.VerifyUserDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.security.JwtUtil;
import co.edu.unbosque.mundialhub.service.UserService;
import co.edu.unbosque.mundialhub.util.AESUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de centralizar los servicios de autenticación,
 * registro y validación de seguridad de usuarios para la plataforma MundialHub.
 * <p>
 * Provee endpoints esenciales para el flujo de acceso de los clientes,
 * gestionando la emisión de tokens Bearer JWT, verificación multifactor
 * mediante códigos temporales de un solo uso, recuperación de contraseñas y
 * pasarelas de retransmisión HTTP hacia APIs externas para datos de la FIFA.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082/register",
		"https://mundial2026hub.netlify.app" })
@Tag(name = "Autenticación", description = "Login and register")
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	private final JwtUtil jwtUtil;

	private final AuthenticationManager authenticationManager;

	/**
	 * Token o firma secreta de autenticación inyectada desde las propiedades del
	 * sistema para consumir api-football.org.
	 */
	@Value("${api.footbalData}")
	private String apiKey;

	/**
	 * Generador seguro de secuencias numéricas aleatorias (Criptográficamente
	 * fuerte) para procesos del controlador.
	 */
	private static final SecureRandom random = new SecureRandom();

	/**
	 * Constructor principal parametrizado para la inyección explícita de
	 * componentes de seguridad de Spring Security y servicios lógicos de negocio.
	 *
	 * @param authenticationManager Administrador encargado de validar el proceso de
	 *                              login.
	 * @param jwtUtil               Utilidad criptográfica para la construcción y
	 *                              extracción de tokens JWT.
	 * @param userService           Servicio encargado de la lógica y operaciones
	 *                              transaccionales de usuarios.
	 */
	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	/**
	 * Registra una nueva cuenta de usuario asignando un rol de acceso específico en
	 * el sistema.
	 * <p>
	 * Realiza validaciones previas comprobando la existencia formal del rol enviado
	 * mediante la clase {@link Exceptions}. Si la validación de negocio es exitosa,
	 * delega el guardado seguro al servicio de usuarios, abstrayendo fallos por
	 * duplicidad de credenciales o caracteres HTML corruptos mediante excepciones
	 * controladas.
	 * </p>
	 *
	 * @param registerRequest Estructura de transferencia de datos con el perfil del
	 *                        nuevo usuario (DTO).
	 * @param rol             Cadena de texto con el rol solicitado por el cliente.
	 * @return Un objeto {@link ResponseEntity} con el estado HTTP de la operación y
	 *         el mensaje de confirmación JSON.
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserDTO registerRequest, @RequestParam String rol) {
	    Exceptions exValidator = new Exceptions();

	    if (!exValidator.containsRole(rol.toUpperCase().trim())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "El rol especificado no es válido", "success", false));
	    }

	    if (!exValidator.isValidPassword(registerRequest.getPassword())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y símbolo especial", "success", false));
	    }

	    if (!exValidator.isGmailValid(registerRequest.getEmail())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "Solo se permiten correos Gmail válidos", "success", false));
	    }

	    if (registerRequest.getUserName() != null && exValidator.containsHtmlSymbols(registerRequest.getUserName())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "El nombre de usuario contiene caracteres no permitidos", "success", false));
	    }

	    try {
	        userService.createWithRol(registerRequest, rol);
	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body(Map.of("message", "User registered successfully", "success", true));
	    } catch (Exceptions.ValidationException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(Map.of("message", e.getMessage(), "success", false));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("message", "Error interno del servidor: " + e.getMessage(), "success", false));
	    }
	}

	/**
	 * Consume de manera interna los servicios remotos de Football-Data para
	 * recuperar el listado oficial de selecciones nacionales clasificadas al
	 * torneo.
	 *
	 * @return Una respuesta HTTP que contiene el cuerpo JSON original enviado por
	 *         la API externa.
	 */
	@GetMapping("/teams")
	public ResponseEntity<?> getTeams() {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Auth-Token", apiKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"https://api.football-data.org/v4/competitions/WC/teams", HttpMethod.GET, entity, String.class);

		return ResponseEntity.ok(response.getBody());
	}

	/**
	 * Consume de manera interna los servicios de infraestructura remotos para
	 * recuperar los partidos y el fixture de la Copa del Mundo.
	 *
	 * @return Una respuesta HTTP con el catálogo estructurado de encuentros
	 *         deportivos de la API externa.
	 */
	@GetMapping("/matches")
	public ResponseEntity<?> getMatches() {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Auth-Token", apiKey);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"https://api.football-data.org/v4/competitions/WC/matches", HttpMethod.GET, entity, String.class);

		return ResponseEntity.ok(response.getBody());
	}

	/**
	 * Genera y reenvía un nuevo token de verificación multifactorial de un solo uso
	 * (OTP) al correo del usuario.
	 *
	 * @param identifier Nombre de usuario o correo para localizar la cuenta
	 *                   correspondiente.
	 * @return Un mapa JSON de confirmación junto con el código de estado HTTP
	 *         pertinente.
	 */
	@PutMapping("/resend")
	public ResponseEntity<?> resendCode(String identifier) {
		try {
			userService.updateVerificationCodeByUserName(identifier);
			return ResponseEntity.ok(Map.of("message", "Código reenviado correctamente", "success", true));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error interno", "success", false));
		}
	}

	/**
	 * Procesa la solicitud de inicio de sesión de un usuario y valida las
	 * credenciales provistas.
	 * <p>
	 * Realiza una búsqueda polimórfica (por email o username) aplicando un cifrado
	 * simétrico AES mediante {@link AESUtil#encrypt}. Ejecuta la validación de
	 * contraseñas delegando en el {@link AuthenticationManager}, actualiza la fecha
	 * de la última sesión y empaqueta un token portador (Bearer JWT) seguro
	 * adjuntando los claims públicos desencriptados.
	 * </p>
	 *
	 * @param loginRequest Objeto de datos con los identificadores de ingreso del
	 *                     usuario.
	 * @return Una estructura {@link AuthResponse} con la firma del token web
	 *         firmado y el rol de acceso.
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoguinRequestDTO loginRequest) {
	    try {
	        String identifier = loginRequest.getIdentifier();

	        Optional<User> userOpt = identifier.contains("@")
	                ? userRepo.findByEmail(AESUtil.encrypt(identifier.toLowerCase()))
	                : userRepo.findByUserName(AESUtil.encrypt(identifier));

	        if (userOpt.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body("Nombre de usuario o contraseña inválidos o usuario no encontrado");
	        }

	        User userFound = userOpt.get();

	        Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(userFound.getUserName(), loginRequest.getPassword()));

	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

	        String userNameDecrypted = userFound.getUsername() != null ? AESUtil.decrypt(userFound.getUsername())
	                : null;
	        String emailDecrypted = userFound.getEmail() != null ? AESUtil.decrypt(userFound.getEmail()) : null;

	        String jwt = jwtUtil.generateToken(userDetails, userNameDecrypted, userFound.getId(), emailDecrypted);

	        userFound.setLastLogin(LocalDateTime.now());
	        userRepo.save(userFound);

	        String role = null;
	        if (userDetails instanceof User) {
	            role = ((User) userDetails).getRole().name();
	        }

	        return ResponseEntity.ok(new AuthResponse(jwt, role));

	    } catch (AuthenticationException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Nombre de usuario o contraseña inválidos o usuario no encontrado");
	    }
	}

	/**
	 * Verifica la existencia de una dirección de correo específica dentro de la
	 * base de datos del sistema.
	 *
	 * @param email Correo electrónico en formato plano a ser consultado.
	 * @return Un valor booleano envuelto en una entidad de respuesta HTTP.
	 */
	@GetMapping("/email")
	public ResponseEntity<?> findByEmail(@RequestParam("email") String email) {
		try {
			boolean user = userService.findEmail(email);
			return ResponseEntity.ok(user);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

	/**
	 * Valida el código de verificación OTP ingresado por el usuario para activar la
	 * cuenta de forma definitiva.
	 * <p>
	 * Aplica filtros de control secuenciales: comprueba que el usuario no esté
	 * previamente activado, que las firmas coincidan y que el código se encuentre
	 * dentro del marco temporal de expiración legal. Si el usuario ostenta el rol
	 * de {@code TEMPORAL}, lo promueve automáticamente a su estatus activo final de
	 * {@code USER}.
	 * </p>
	 *
	 * @param dto Datos de transferencia con el correo y el código ingresado por el
	 *            cliente.
	 * @return Un mapa JSON descriptivo que detalla el éxito o el fallo técnico de
	 *         la activación.
	 */
	@PostMapping("/verify")
	public ResponseEntity<?> verify(@RequestBody VerifyUserDTO dto) {
	    User user = userService.findByEmail(dto.getEmail());

	    if (user == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", "Usuario no existe", "success", false));
	    }

	    if (user.getCode() == null) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "Genera el código nuevamente", "success", false));
	    }

	    if (user.isEnabled()) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(Map.of("message", "Usuario ya verificado", "success", false));
	    }

	    if (!user.getCode().equals(dto.getCode())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "Código incorrecto", "success", false));
	    }

	    if (user.getVerificationExpires().isBefore(LocalDateTime.now())) {
	        user.setCode(null);
	        user.setVerificationExpires(null);
	        userRepo.save(user);
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", "Código expirado", "success", false));
	    }

	    user.setEnabled(true);
	    user.setCode(null);
	    user.setVerificationExpires(null);
	    if (user.getRole() == Role.TEMPORAL) {
	        user.setRole(Role.USER);
	    }
	    userRepo.save(user);

	    // ✅ Generar JWT automáticamente igual que el login
	    UserDetails userDetails = org.springframework.security.core.userdetails.User
	            .withUsername(user.getUserName())
	            .password(user.getPassword())
	            .authorities(user.getRole().name())
	            .build();

	    String userNameDecrypted = user.getUserName() != null ? AESUtil.decrypt(user.getUserName()) : null;
	    String emailDecrypted    = user.getEmail()    != null ? AESUtil.decrypt(user.getEmail())    : null;

	    String jwt = jwtUtil.generateToken(userDetails, userNameDecrypted, user.getId(), emailDecrypted);

	    user.setLastLogin(LocalDateTime.now());
	    userRepo.save(user);

	    return ResponseEntity.ok(Map.of(
	            "message", "Usuario verificado correctamente",
	            "success", true,
	            "token",   jwt,
	            "role",    user.getRole().name()
	    ));
	}

	/**
	 * Clase utilitaria interna (Inner Class DTO) diseñada de forma inmutable para
	 * estructurar las respuestas de inicio de sesión exitosas devueltas al cliente.
	 */
	private static class AuthResponse {
		private final String token;
		private final String role;

		/**
		 * Constructor simplificado para respuestas que no requieren empaquetar un rol
		 * de acceso.
		 * 
		 * @param token Firma criptográfica Bearer JWT emitida.
		 */
		@SuppressWarnings("unused")
		public AuthResponse(String token) {
			this.token = token;
			this.role = null;
		}

		/**
		 * Constructor completo parametrizado para la inmutabilidad de la entidad.
		 * 
		 * @param token Firma criptográfica Bearer JWT emitida.
		 * @param role  Nombre del rol de acceso actual asociado.
		 */
		public AuthResponse(String token, String role) {
			this.token = token;
			this.role = role;
		}

		@SuppressWarnings("unused")
		public String getToken() {
			return token;
		}

		@SuppressWarnings("unused")
		public String getRole() {
			return role;
		}
	}

	/**
	 * Procesa los flujos de recuperación y actualización de contraseñas olvidadas
	 * por los usuarios.
	 *
	 * @param email       Correo del usuario que solicita la reasignación de
	 *                    credenciales.
	 * @param newPassword Nueva contraseña en texto plano, sujeta a hashing
	 *                    asimétrico en la capa de servicios.
	 * @return Un mapa JSON indicando el éxito técnico de la actualización de
	 *         seguridad.
	 */
	@PutMapping("/rememberPassword")
	public ResponseEntity<?> rememberPassword(@RequestParam String email, @RequestParam String newPassword) {
		try {
			UserDTO dto = new UserDTO();
			dto.setEmail(email);
			dto.setPassword(newPassword);

			userService.rememberPassword(dto);
			return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente", "success", true));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "success", false));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}

	/**
	 * Recupera la instancia compartida y segura del generador numérico
	 * pseudoaleatorio.
	 *
	 * @return La instancia única y estática de {@link SecureRandom}.
	 */
	public static SecureRandom getRandom() {
		return random;
	}
}