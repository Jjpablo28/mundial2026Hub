package co.edu.unbosque.mundialhub.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.mundialhub.dto.UserStickerDTO;
import co.edu.unbosque.mundialhub.service.UserStickerService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de exponer y gestionar los endpoints asociados al
 * inventario personal de láminas (cromos) que poseen los usuarios en su álbum
 * digital de MundialHub.
 * <p>
 * Centraliza las operaciones transaccionales para añadir, actualizar, listar o
 * remover láminas de las colecciones de los usuarios, además de coordinar la
 * lógica de negocio para la compra y apertura de paquetes (sobres de láminas)
 * consumiendo créditos del sistema.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 * @see UserStickerService
 */
@RestController
@RequestMapping("/user-stickers")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082",
		"https://mundial2026hub.netlify.app" })
@Tag(name = "user-stickers", description = "Gestión del inventario de láminas de usuarios")
public class UserStickerController {

	@Autowired
	private UserStickerService userStickerServ;

	/**
	 * Añade de forma explícita o manual una lámina al inventario de un usuario
	 * específico.
	 *
	 * @param dto Estructura de transferencia de datos (DTO) que mapea la relación
	 *            entre el usuario y el sticker.
	 * @return Un objeto {@link ResponseEntity} con el mensaje JSON de confirmación
	 *         y el estado HTTP 21 (Created), u objeto 400 (Bad Request) con el
	 *         detalle técnico de la falla.
	 */
	@PostMapping
	public ResponseEntity<?> create(@RequestBody UserStickerDTO dto) {
		try {
			userStickerServ.create(dto);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Lámina añadida al inventario", "success", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
		}
	}

	/**
	 * Recupera la colección completa de láminas (tanto repetidas como únicas) que
	 * pertenecen al inventario de un usuario.
	 *
	 * @param userId Identificador único (llave primaria) del usuario consultado.
	 * @return Un objeto {@link ResponseEntity} que contiene el listado de DTOs y el
	 *         estado HTTP 200 (OK).
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<?> getByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userStickerServ.findByUser(userId));
	}

	/**
	 * Modifica o actualiza los metadatos o la cantidad de stock de una lámina
	 * específica en la colección del usuario.
	 *
	 * @param id  Identificador único del registro de inventario a modificar.
	 * @param dto Estructura de transferencia con los nuevos valores y cantidades a
	 *            sobreescribir.
	 * @return Un objeto {@link ResponseEntity} de confirmación con estado HTTP 200
	 *         (OK), o un estado 404 (Not Found) si el registro no existe.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserStickerDTO dto) {
		try {
			userStickerServ.updateById(id, dto);
			return ResponseEntity.ok(Map.of("message", "Cantidad actualizada", "success", true));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", e.getMessage(), "success", false));
		}
	}

	/**
	 * Remueve de manera definitiva una lámina o registro de cromo del inventario de
	 * un usuario.
	 *
	 * @param id Identificador único del registro de inventario (asociación) que se
	 *           desea eliminar.
	 * @return Un objeto {@link ResponseEntity} indicando el éxito técnico de la
	 *         remoción con estado HTTP 200 (OK).
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		try {
			userStickerServ.deleteById(id);
			return ResponseEntity.ok(Map.of("message", "Lámina removida del inventario", "success", true));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", e.getMessage(), "success", false));
		}
	}

	/**
	 * Procesa la apertura (unboxing) de un sobre de láminas disponible en el
	 * inventario del usuario.
	 * <p>
	 * Este método invoca de forma transaccional la generación aleatoria de cromos
	 * basada en las probabilidades de la plataforma y los inyecta dentro de la
	 * colección del cliente.
	 * </p>
	 *
	 * @param id Identificador único del usuario que ejecuta la acción de apertura.
	 * @return Un objeto {@link ResponseEntity} descriptivo indicando que las
	 *         láminas fueron añadidas bajo estado HTTP 201 (Created).
	 */
	@PostMapping("/open-pack/{id}")
	public ResponseEntity<?> createPack(@PathVariable Long id) {
		try {
			userStickerServ.openPack(id);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Lámina añadida al inventario", "success", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
		}
	}

	/**
	 * Ejecuta el flujo transaccional para la compra de un nuevo sobre (pack) de
	 * láminas para el usuario.
	 * <p>
	 * El servicio valida internamente que el usuario cuente con los tokens o
	 * créditos de saldo suficientes, realiza el descuento correspondiente y añade
	 * el paquete cerrado a su cuenta listo para ser abierto.
	 * </p>
	 *
	 * @param id Identificador único del usuario que solicita la compra del sobre.
	 * @return Un objeto {@link ResponseEntity} con la confirmación de la compra y
	 *         el estado HTTP 201 (Created).
	 */
	@PostMapping("/buy-pack/{id}")
	public ResponseEntity<?> buyPack(@PathVariable Long id) {
		try {
			userStickerServ.buyPack(id);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Pack añadido al inventario", "success", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "success", false));
		}
	}
	@GetMapping("/user/{userId}/team/{teamId}")
	public ResponseEntity<?> getByUserAndTeam(@PathVariable Long userId, @PathVariable Long teamId) {
	    try {
	        return ResponseEntity.ok(userStickerServ.findByUserAndTeam(userId, teamId));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", e.getMessage(), "success", false));
	    }
	}

}