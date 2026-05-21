package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.mundialhub.dto.UpdatePasswordDTO;
import co.edu.unbosque.mundialhub.dto.UserDTO;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.security.JwtUtil;
import co.edu.unbosque.mundialhub.service.UserService;
import co.edu.unbosque.mundialhub.util.AESUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082","https://mundial2026hub.netlify.app" })
@Tag(name = "Users", description = "CRUD Users")
public class UserController {

	@Autowired
	private UserService userServ;
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping
	public ResponseEntity<?> create(@RequestBody UserDTO registerRequest, @RequestParam String rol) {
		if (userServ.findUsernameAlreadyTaken(registerRequest.getUserName())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
		}

		int result = 1;
		if (rol.equals("ADMIN")) {
			result = userServ.createWithRol(registerRequest, "ADMIN");

		} else if (rol.equals("USER")) {
			result = userServ.createWithRol(registerRequest, "USER");

		} else if (rol.equals("TEMPORAL")) {
			result = userServ.createWithRol(registerRequest, "TEMPORAL");

		} else if (rol.equals("SUPPORT")) {
			result = userServ.createWithRol(registerRequest, "SUPPORT");

		} else if (rol.equals("OPERATOR")) {
			result = userServ.createWithRol(registerRequest, "OPERATOR");

		}

		if (result == 0) {

			return ResponseEntity.status(HttpStatus.CREATED).body(
					Map.of("message", "User registered successfully", "success", true, "code", userServ.getCode()));
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

	@GetMapping
	public ResponseEntity<?> getAll() {
		try {
			return ResponseEntity.ok(userServ.findAll());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener usuarios", "success", false));
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(userServ.findById(id));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}

	@PutMapping("/{id}/role")
	public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestParam String role) {

		try {
			UserDTO dto = new UserDTO();
			dto.setRole(Role.valueOf(role));

			UserDTO updated = userServ.updateRol(id, dto);

			return ResponseEntity
					.ok(Map.of("message", "Rol actualizado correctamente", "data", updated, "success", true));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "Rol inválido", "success", false));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
	    try {
	        UserDTO updated = userServ.updateById(id, dto);

	        User userEntity = userRepo.findById(id)
	                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

	        UserDetails userDetails = org.springframework.security.core.userdetails.User
	                .withUsername(userEntity.getUserName()) 
	                .authorities(userEntity.getRole().name())
	                .build();

	        // 1. Validamos nulos antes de desencriptar
	        String userNameDecrypted = userEntity.getUserName() != null ? AESUtil.decrypt(userEntity.getUserName()) : null;
	        String emailDecrypted = userEntity.getEmail() != null ? AESUtil.decrypt(userEntity.getEmail()) : null;
	        
	        // 2. Generamos el token de forma segura
	        String jwt = jwtUtil.generateToken(userDetails, userNameDecrypted, id, emailDecrypted);

	        return ResponseEntity.ok(Map.of(
	            "message", "Usuario actualizado correctamente", 
	            "data", updated, 
	            "success", true, 
	            "token", jwt
	        ));

	    } catch (Exception e) {

	    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
	    }
	}

	@PutMapping("/{id}/password")
	public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO dtoU) {

		try {
			UserDTO dto = new UserDTO();
			dto.setPassword(dtoU.getNewPassword());

			userServ.updatePassword(id, dto,dtoU.getOldPassword());

			return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente", "success", true));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "success", false));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}

	@GetMapping("/count")
	public ResponseEntity<?> countAll() {

		Long count = userServ.count();

		if (count == 0) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(count);
		}

		return ResponseEntity.ok(count);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable Long id) {

		try {
			userServ.deleteById(id);

			return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito", "success", true));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}

	@DeleteMapping
	public ResponseEntity<?> deleteByUsername(@RequestParam String username) {

		try {
			userServ.deleteByUsername(username);

			return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito", "success", true));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}
}