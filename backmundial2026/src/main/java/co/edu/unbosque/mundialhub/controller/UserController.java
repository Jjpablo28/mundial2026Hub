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
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.User.Role;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.security.JwtUtil;
import co.edu.unbosque.mundialhub.service.UserService;
import co.edu.unbosque.mundialhub.util.AESUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de exponer los endpoints del ciclo de vida CRUD y
 * la administración avanzada de los usuarios dentro del ecosistema MundialHub
 * (sistema VeltrixPass).
 * <p>
 * Gestiona operaciones de creación de cuentas con asignación de roles
 * validados, actualizaciones de perfiles con reemisión dinámica de tokens
 * Bearer JWT, cambios seguros de credenciales y auditorías estadísticas de
 * cuentas activas en la base de datos.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 * @see UserService
 * @see JwtUtil
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082",
		"https://mundial2026hub.netlify.app" })
@Tag(name = "Users", description = "CRUD Users")
public class UserController {

	@Autowired
	private UserService userServ;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private JwtUtil jwtUtil;

	/**
	 * Crea y registra un nuevo usuario en la plataforma aplicando reglas rigurosas
	 * de validación.
	 * <p>
	 * El método formatea el rol ingresado y consulta la clase centralizada de
	 * excepciones para verificar que pertenezca a la configuración permitida del
	 * sistema. Delega en la capa de servicios la validación interna de duplicidad
	 * (por username o email) y el filtrado de ataques XSS o caracteres HTML
	 * corruptos.
	 * </p>
	 *
	 * @param registerRequest DTO con los datos del perfil del usuario a registrar.
	 * @param rol             Cadena de texto con el rol que se le asignará a la
	 *                        cuenta.
	 * @return Un objeto {@link ResponseEntity} con el resultado técnico de la
	 *         operación. Retorna 201 (Created) en caso de éxito, 400 (Bad Request)
	 *         si el rol es inválido, 409 (Conflict) si los datos ya existen o
	 *         violan las reglas, y 500 si ocurre una falla del servidor.
	 */
	@PostMapping("/create")
	public ResponseEntity<?> create(@RequestBody UserDTO registerRequest, @RequestParam String rol) {
		String formattedRole = rol.toUpperCase().trim();
		Exceptions exValidator = new Exceptions();

		if (!exValidator.containsRole(formattedRole)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",
					"El rol '" + rol + "' no es válido en el sistema VeltrixPass.", "success", false));
		}

		try {
			userServ.createWithRol(registerRequest, formattedRole);

			return ResponseEntity.status(HttpStatus.CREATED).body(
					Map.of("message", "User registered successfully", "success", true, "code", userServ.getCode()));

		} catch (Exceptions.UserAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage(), "success", false));

		} catch (Exceptions.ValidationException e) {
			System.err.println("Html u otro error de validación detectado: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage(), "success", false));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error interno en el servidor: " + e.getMessage(), "success", false));
		}
	}

	/**
	 * Recupera la lista completa de todos los usuarios registrados en el backend de
	 * la aplicación.
	 *
	 * @return Un objeto {@link ResponseEntity} con la colección de DTOs de usuarios
	 *         bajo estado HTTP 200 (OK).
	 */
	@GetMapping
	public ResponseEntity<?> getAll() {
		try {
			return ResponseEntity.ok(userServ.findAll());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener usuarios", "success", false));
		}
	}

	/**
	 * Localiza y extrae el perfil detallado de un usuario utilizando su
	 * identificador numérico de base de datos.
	 *
	 * @param id Identificador numérico (llave primaria) del usuario.
	 * @return Un objeto {@link ResponseEntity} con la información del usuario o un
	 *         estado HTTP 404 (Not Found) si no existe.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(userServ.findById(id));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Usuario no encontrado", "success", false));
		}
	}

	/**
	 * Actualiza de forma independiente el rol de seguridad asignado a un usuario
	 * específico.
	 *
	 * @param id   Identificador único del usuario a modificar.
	 * @param role Nombre del nuevo rol corporativo o técnico (basado en el enum
	 *             {@link Role}).
	 * @return Un objeto {@link ResponseEntity} con el DTO modificado y el estado
	 *         HTTP 200 (OK).
	 */
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

	/**
	 * Modifica las propiedades de perfil de un usuario y regenera de manera
	 * síncrona su token JWT de sesión.
	 * <p>
	 * Tras realizar los cambios en la base de datos, extrae la entidad, valida
	 * posibles campos nulos y desencripta simétricamente mediante
	 * {@link AESUtil#decrypt} el nombre de usuario y correo electrónico. Esto
	 * permite construir un nuevo token Bearer JWT válido para evitar que el cliente
	 * pierda la sesión por desincronización de claims.
	 * </p>
	 *
	 * @param id  Identificador numérico del usuario que actualiza su información.
	 * @param dto Estructura de transferencia con los nuevos campos de datos a
	 *            sobreescribir.
	 * @return Un objeto {@link ResponseEntity} que incluye la confirmación, el DTO
	 *         guardado y el nuevo token JWT.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
		try {
			UserDTO updated = userServ.updateById(id, dto);

			User userEntity = userRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			UserDetails userDetails = org.springframework.security.core.userdetails.User
					.withUsername(userEntity.getUserName()).authorities(userEntity.getRole().name()).build();

			String userNameDecrypted = userEntity.getUserName() != null ? AESUtil.decrypt(userEntity.getUserName())
					: null;
			String emailDecrypted = userEntity.getEmail() != null ? AESUtil.decrypt(userEntity.getEmail()) : null;

			String jwt = jwtUtil.generateToken(userDetails, userNameDecrypted, id, emailDecrypted);

			return ResponseEntity.ok(Map.of("message", "Usuario actualizado correctamente", "data", updated, "success",
					true, "token", jwt));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * Procesa las solicitudes de cambio de contraseña dentro de la sesión activa
	 * del usuario.
	 * <p>
	 * Requiere el envío obligatorio de la contraseña anterior para validarla en la
	 * capa de servicios contra el hash almacenado, previniendo suplantaciones antes
	 * de persistir la nueva clave.
	 * </p>
	 *
	 * @param id   Identificador numérico del usuario.
	 * @param dtoU Estructura de datos que contiene la contraseña anterior y la
	 *             nueva clave solicitada.
	 * @return Un objeto {@link ResponseEntity} indicando el éxito de la transacción
	 *         de seguridad.
	 */
	@PutMapping("/{id}/password")
	public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO dtoU) {
		try {
			UserDTO dto = new UserDTO();
			dto.setPassword(dtoU.getNewPassword());

			userServ.updatePassword(id, dto, dtoU.getOldPassword());

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
	 * Realiza una auditoría cuantitativa devolviendo el número total de cuentas de
	 * usuario creadas en el sistema.
	 *
	 * @return Un objeto {@link ResponseEntity} con la cantidad de registros.
	 *         Retorna estado HTTP 204 (No Content) si es igual a cero.
	 */
	@GetMapping("/count")
	public ResponseEntity<?> countAll() {
		Long count = userServ.count();

		if (count == 0) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(count);
		}
		return ResponseEntity.ok(count);
	}

	/**
	 * Elimina de forma física o lógica a un usuario del sistema utilizando su
	 * identificador único (ID).
	 *
	 * @param id Identificador numérico de la cuenta que se desea remover.
	 * @return Un mapa JSON descriptivo que detalla el éxito o fallo de la
	 *         eliminación.
	 */
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

	/**
	 * Elimina de forma definitiva a un usuario de la base de datos localizándolo
	 * por su nombre de usuario (Username).
	 *
	 * @param username Nombre de usuario único de la cuenta a dar de baja.
	 * @return Un mapa JSON descriptivo con el estado HTTP de la operación.
	 */
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