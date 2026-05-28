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
 * la administración avanzada de los usuarios dentro del ecosistema MundialHub.
 * <p>
 * Gestiona operaciones de creación de cuentas con asignación de roles
 * validados, actualizaciones de perfiles con reemisión dinámica de tokens
 * Bearer JWT, cambios seguros de credenciales y auditorías estadísticas de
 * cuentas activas en la base de datos.
 * </p>
 * <p>
 * Aplica validaciones de seguridad en cada endpoint: sanitización contra
 * ataques XSS mediante {@link Exceptions#containsHtmlSymbols}, verificación
 * de robustez de contraseñas con {@link Exceptions#isValidPassword}, validación
 * de formato de correo Gmail con {@link Exceptions#isGmailValid} y control de
 * roles con {@link Exceptions#containsRole}.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 * @see UserService
 * @see JwtUtil
 * @see Exceptions
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
     * Crea y registra un nuevo usuario en la plataforma aplicando validaciones
     * rigurosas de seguridad antes de delegar en la capa de servicios.
     * <p>
     * Ejecuta en orden las siguientes comprobaciones:
     * <ol>
     *   <li>Valida que el rol sea reconocido por el sistema.</li>
     *   <li>Verifica la robustez de la contraseña (mayúscula, minúscula,
     *       dígito y símbolo especial, mínimo 8 caracteres).</li>
     *   <li>Comprueba que el correo pertenezca al dominio Gmail.</li>
     *   <li>Detecta caracteres HTML o de inyección en el username (XSS).</li>
     * </ol>
     * </p>
     *
     * @param registerRequest DTO con los datos del perfil del usuario a registrar.
     * @param rol             Cadena de texto con el rol que se asignará a la cuenta.
     * @return Un objeto {@link ResponseEntity} con estado HTTP 201 (Created) en
     *         éxito, 400 (Bad Request) si alguna validación falla, 409 (Conflict)
     *         si el usuario ya existe, o 500 ante un error interno del servidor.
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UserDTO registerRequest, @RequestParam String rol) {
        String formattedRole = rol.toUpperCase().trim();
        Exceptions exValidator = new Exceptions();

        if (!exValidator.containsRole(formattedRole)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "El rol '" + rol + "' no es válido en el sistema.",
                    "success", false));
        }

        if (!exValidator.isValidPassword(registerRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y símbolo especial",
                    "success", false));
        }

        if (!exValidator.isGmailValid(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Solo se permiten correos Gmail válidos",
                    "success", false));
        }

        if (registerRequest.getUserName() != null && exValidator.containsHtmlSymbols(registerRequest.getUserName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "El nombre de usuario contiene caracteres no permitidos",
                    "success", false));
        }

        try {
            userServ.createWithRol(registerRequest, formattedRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User registered successfully",
                    "success", true,
                    "code", userServ.getCode()));

        } catch (Exceptions.UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "success", false));

        } catch (Exceptions.ValidationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "success", false));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno en el servidor: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Recupera la lista completa de todos los usuarios registrados en el backend.
     *
     * @return Un objeto {@link ResponseEntity} con la colección de DTOs de usuarios
     *         bajo estado HTTP 200 (OK), o 500 ante un error de persistencia.
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
     * Localiza y extrae el perfil detallado de un usuario por su identificador
     * numérico de base de datos.
     * <p>
     * Valida que el ID sea un valor positivo antes de consultar el repositorio,
     * previniendo consultas innecesarias con valores inválidos.
     * </p>
     *
     * @param id Identificador numérico (llave primaria) del usuario.
     * @return Un objeto {@link ResponseEntity} con el DTO del usuario bajo estado
     *         HTTP 200 (OK), 400 si el ID es inválido, o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "ID de usuario inválido", "success", false));
        }
        try {
            return ResponseEntity.ok(userServ.findById(id));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado", "success", false));
        }
    }

    /**
     * Actualiza de forma independiente el rol de seguridad asignado a un usuario.
     * <p>
     * Verifica previamente mediante {@link Exceptions#containsRole} que el nuevo
     * rol pertenezca al catálogo de roles válidos del sistema, evitando la
     * asignación de roles arbitrarios o malformados.
     * </p>
     *
     * @param id   Identificador único del usuario a modificar.
     * @param role Nombre del nuevo rol (basado en el enum {@link Role}).
     * @return Un objeto {@link ResponseEntity} con el DTO modificado y estado HTTP
     *         200 (OK), 400 si el rol es inválido, o 404 si el usuario no existe.
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestParam String role) {
        Exceptions exValidator = new Exceptions();

        if (!exValidator.containsRole(role.toUpperCase().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Rol inválido", "success", false));
        }

        try {
            UserDTO dto = new UserDTO();
            dto.setRole(Role.valueOf(role.toUpperCase().trim()));
            UserDTO updated = userServ.updateRol(id, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Rol actualizado correctamente",
                    "data", updated,
                    "success", true));

        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado", "success", false));
        }
    }

    /**
     * Modifica las propiedades de perfil de un usuario y regenera de manera
     * síncrona su token JWT de sesión.
     * <p>
     * Valida el ID positivo y sanitiza el username contra ataques XSS antes de
     * procesar la actualización. Tras persistir los cambios, desencripta
     * simétricamente el username y email mediante {@link AESUtil#decrypt} para
     * construir un nuevo token Bearer JWT válido, evitando que el cliente pierda
     * la sesión por desincronización de claims.
     * </p>
     *
     * @param id  Identificador numérico del usuario que actualiza su información.
     * @param dto Estructura de transferencia con los nuevos campos a sobreescribir.
     * @return Un objeto {@link ResponseEntity} que incluye la confirmación, el DTO
     *         guardado y el nuevo token JWT bajo estado HTTP 200 (OK), o 400/404
     *         ante fallos de validación o ausencia del recurso.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        Exceptions exValidator = new Exceptions();

        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "ID de usuario inválido", "success", false));
        }

        if (dto.getUserName() != null && exValidator.containsHtmlSymbols(dto.getUserName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "El nombre de usuario contiene caracteres no permitidos", "success", false));
        }

        try {
            UserDTO updated = userServ.updateById(id, dto);
            User userEntity = userRepo.findById(id)
                    .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Usuario no encontrado"));

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(userEntity.getUserName())
                    .authorities(userEntity.getRole().name())
                    .build();

            String userNameDecrypted = userEntity.getUserName() != null
                    ? AESUtil.decrypt(userEntity.getUserName()) : null;
            String emailDecrypted = userEntity.getEmail() != null
                    ? AESUtil.decrypt(userEntity.getEmail()) : null;

            String jwt = jwtUtil.generateToken(userDetails, userNameDecrypted, id, emailDecrypted);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuario actualizado correctamente",
                    "data", updated,
                    "success", true,
                    "token", jwt));

        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    /**
     * Procesa las solicitudes de cambio de contraseña dentro de la sesión activa
     * del usuario.
     * <p>
     * Valida la robustez de la nueva contraseña con {@link Exceptions#isValidPassword}
     * antes de delegar en el servicio, que a su vez verifica la contraseña anterior
     * contra el hash almacenado para prevenir suplantaciones de identidad.
     * </p>
     *
     * @param id   Identificador numérico del usuario.
     * @param dtoU Estructura con la contraseña anterior y la nueva clave solicitada.
     * @return Un objeto {@link ResponseEntity} indicando el éxito de la transacción
     *         bajo estado HTTP 200 (OK), o 400/404 si la validación o búsqueda falla.
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordDTO dtoU) {
        Exceptions exValidator = new Exceptions();

        if (!exValidator.isValidPassword(dtoU.getNewPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "La nueva contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y símbolo especial",
                    "success", false));
        }

        try {
            UserDTO dto = new UserDTO();
            dto.setPassword(dtoU.getNewPassword());
            userServ.updatePassword(id, dto, dtoU.getOldPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente", "success", true));

        } catch (Exceptions.ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado", "success", false));
        }
    }

    /**
     * Realiza una auditoría cuantitativa devolviendo el número total de cuentas
     * de usuario creadas en el sistema.
     *
     * @return Un objeto {@link ResponseEntity} con la cantidad de registros bajo
     *         estado HTTP 200 (OK), o 204 (No Content) si no hay usuarios.
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
     * Elimina de forma física o lógica a un usuario del sistema por su ID.
     * <p>
     * Valida que el identificador sea positivo antes de intentar la eliminación,
     * evitando operaciones con valores nulos o negativos.
     * </p>
     *
     * @param id Identificador numérico de la cuenta a remover.
     * @return Un mapa JSON con el estado HTTP 200 (OK) en éxito, 400 si el ID es
     *         inválido, o 404 si el usuario no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "ID de usuario inválido", "success", false));
        }
        try {
            userServ.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito", "success", true));
        } catch (Exceptions.ResourceNotFoundException | Exceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error inesperado: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Elimina de forma definitiva a un usuario localizándolo por su username.
     * <p>
     * Sanitiza el username recibido contra caracteres HTML o de inyección antes
     * de intentar la eliminación, protegiendo el sistema de ataques XSS o
     * inyección de datos en parámetros de consulta.
     * </p>
     *
     * @param username Nombre de usuario único de la cuenta a dar de baja.
     * @return Un mapa JSON con el estado HTTP 200 (OK) en éxito, 400 si el valor
     *         es inválido o contiene caracteres peligrosos, o 404 si no se encuentra.
     */
    @DeleteMapping
    public ResponseEntity<?> deleteByUsername(@RequestParam String username) {
        Exceptions exValidator = new Exceptions();

        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "El nombre de usuario no puede estar vacío", "success", false));
        }

        if (exValidator.containsHtmlSymbols(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "El nombre de usuario contiene caracteres no permitidos", "success", false));
        }

        try {
            userServ.deleteByUsername(username);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito", "success", true));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado", "success", false));
        }
    }
}