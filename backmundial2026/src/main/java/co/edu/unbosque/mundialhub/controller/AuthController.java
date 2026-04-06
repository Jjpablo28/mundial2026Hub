package co.edu.unbosque.mundialhub.controller;

import java.security.SecureRandom;
//import java.security.SecureRandom;
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
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.security.JwtUtil;
import co.edu.unbosque.mundialhub.service.UserService;
import co.edu.unbosque.mundialhub.util.AESUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082/register" })
@Tag(name = "Autenticación", description = "Login and register")
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	private final JwtUtil jwtUtil;

	private final AuthenticationManager authenticationManager;

	@Value("${api.footbalData}")
	private String apiKey;

	@Autowired
	private static final SecureRandom random = new SecureRandom();

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserDTO registerRequest, @RequestParam String rol) {
		if (userService.findUsernameAlreadyTaken(registerRequest.getUserName())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
		}

		int result = 1;
		if (rol.equals("ADMIN")) {
			result = userService.createWithRol(registerRequest, "ADMIN");

		} else if (rol.equals("USER")) {
			result = userService.createWithRol(registerRequest, "USER");

		} else if (rol.equals("TEMPORAL")) {
			result = userService.createWithRol(registerRequest, "TEMPORAL");

		} else if (rol.equals("SUPPORT")) {
			result = userService.createWithRol(registerRequest, "SUPPORT");

		} else if (rol.equals("OPERATOR")) {
			result = userService.createWithRol(registerRequest, "OPERATOR");

		}

		if (result == 0) {

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "User registered successfully", "success", true));
		} else if (result == 4) {
			System.err.println("Html");

			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "El nombre no puede tener caracteres de HTML", "success", false));

		} else if (result == 5) {
			System.err.println("Html");

			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "El usuario no puede tener caracteres de HTML", "success", false));
		} else if (result == 6) {
			System.err.println("contraseña");

			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
					"La contraseña no cumple con el estándar (mínimo 8 caracteres, al menos una letra minúscula, al menos una letra mayúscula, al menos un número y al menos un símbolo que no sea < > :)",
					"success", false));
		} else if (result == 7) {
			System.err.println("correo");

			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
					"El correo electrónico debe ser una dirección de Gmail válida.", "success", false));
		} else {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "Error registering user", "success", false));
		}
	}

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

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoguinRequestDTO loginRequest) {

		try {
			String identifier = loginRequest.getIdentifier();

			Optional<User> userOpt = identifier.contains("@") ? userRepo.findByEmail(AESUtil.encrypt(identifier))
					: userRepo.findByUserName(AESUtil.encrypt(identifier));

			if (userOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Nombre de usuario o contraseña inválidos o usuario no encontrado");
			}

			User userFound = userOpt.get();

			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(userFound.getUserName(), loginRequest.getPassword()));

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();

			String jwt = jwtUtil.generateToken(userDetails, AESUtil.encrypt(userFound.getUsername()));

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
	
	@GetMapping("/email")
	public ResponseEntity<?> findByEmail(@RequestParam("email") String email) {
	    try {
	        boolean user = userService.findEmail(email);
	        return ResponseEntity.ok(user);
	    } catch (RuntimeException e) {
	        
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    }
	}
	
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Código expirado", "success", false));
		}

		user.setEnabled(true);
		user.setCode(null);
		user.setVerificationExpires(null);
		userRepo.save(user);

		return ResponseEntity.ok(Map.of("message", "Usuario verificado correctamente", "success", true));
	}
	
	private static class AuthResponse {
		private final String token;

		private final String role;

		@SuppressWarnings("unused")
		public AuthResponse(String token) {
			this.token = token;
			this.role = null;
		}

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

	public static SecureRandom getRandom() {
		return random;
	}
}
