package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import co.edu.unbosque.mundialhub.dto.StickerDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.service.StickerService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de exponer y gestionar los endpoints del ciclo de
 * vida CRUD y operaciones estadísticas de las láminas, cromos o stickers del
 * álbum digital de MundialHub.
 * <p>
 * Centraliza las peticiones HTTP orientadas a la manipulación de inventario de
 * stickers, filtrados por selecciones de fútbol (equipos) y auditorías de
 * conteo global para los usuarios de la plataforma.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 * @see StickerService
 */
@RestController
@RequestMapping("/sticker")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082",
		"https://mundial2026hub.netlify.app" })
@Tag(name = "stickers", description = "CRUD stickers")
public class StickerController {

	@Autowired
	private StickerService stickerserv;

	/**
	 * Crea y registra un nuevo cromo/sticker dentro del catálogo maestro del álbum.
	 *
	 * @param dto Estructura de transferencia de datos (DTO) que contiene la
	 *            información del sticker a crear.
	 * @return Un objeto {@link ResponseEntity} con el mensaje JSON de confirmación
	 *         y el estado HTTP 201 (Created).
	 */
	// ✅ CORRECCIÓN: Validar el DTO antes de crear
		@PostMapping
		public ResponseEntity<?> create(@RequestBody StickerDTO dto) {
		    Exceptions exValidator = new Exceptions();

		    if (dto.getPlayerName() != null && exValidator.containsHtmlSymbols(dto.getPlayerName())) {
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                .body(Map.of("message", "El nombre contiene caracteres no permitidos", "success", false));
		    }

		    try {
		        stickerserv.create(dto);
		        return ResponseEntity.status(HttpStatus.CREATED)
		                .body(Map.of("message", "El sticker ha sido creado", "success", true));
		    } catch (Exceptions.ValidationException e) {
		        return ResponseEntity.status(HttpStatus.CONFLICT)
		                .body(Map.of("message", e.getMessage(), "success", false));
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                .body(Map.of("message", "Error interno al crear el sticker", "success", false));
		    }
		}

		

	/**
	 * Actualiza las propiedades técnicas o descriptivas de un sticker existente
	 * localizándolo por su identificador único.
	 *
	 * @param id  Identificador numérico (ID único) del sticker que se desea
	 *            modificar.
	 * @param dto Objeto de transferencia con los nuevos valores a sobreescribir en
	 *            la entidad.
	 * @return Un objeto {@link ResponseEntity} que contiene el resultado técnico de
	 *         la transacción bajo el estado HTTP 200 (OK), o un estado 400 (Bad
	 *         Request) si se violan restricciones lógicas del servicio.
	 */
				@PutMapping("/{id}")
				public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StickerDTO dto) {
				    Exceptions exValidator = new Exceptions();

				    if (dto.getPlayerName() != null && exValidator.containsHtmlSymbols(dto.getPlayerName())) {
				        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				                .body(Map.of("message", "El nombre contiene caracteres no permitidos", "success", false));
				    }

				    try {
				        stickerserv.updateById(id, dto);
				        return ResponseEntity.ok(Map.of("message", "Sticker actualizado correctamente", "success", true));
				    } catch (Exceptions.ResourceNotFoundException e) {
				        return ResponseEntity.status(HttpStatus.NOT_FOUND)
				                .body(Map.of("message", e.getMessage(), "success", false));
				    } catch (Exception e) {
				        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				                .body(Map.of("message", e.getMessage(), "success", false));
				    }
				}

	/**
	 * Elimina de forma definitiva un cromo/sticker del sistema utilizando su
	 * identificador único.
	 *
	 * @param id Identificador numérico del sticker a remover de la base de datos.
	 * @return Un objeto {@link ResponseEntity} descriptivo con estado HTTP 200 (OK)
	 *         en caso de éxito, o un estado 404 (Not Found) si el recurso no
	 *         existía en el backend.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable Long id) {
		try {
			stickerserv.deleteById(id);

			return ResponseEntity.ok(Map.of("message", "Sticker eliminado con éxito", "success", true));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Sticker no encontrado", "success", false));
		}
	}

	/**
	 * Recupera la colección completa de todos los stickers registrados en el
	 * sistema de manera global.
	 *
	 * @return Un objeto {@link ResponseEntity} con el listado completo de DTOs y
	 *         estado HTTP 200 (OK), o un estado 500 (Internal Server Error) ante
	 *         cualquier eventualidad de persistencia.
	 */
	@GetMapping
	public ResponseEntity<?> getAll() {
		try {
			return ResponseEntity.ok(stickerserv.findAll());
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener los stickers", "success", false));
		}
	}

	/**
	 * Filtra y recupera la lista de stickers asociados exclusivamente a una
	 * selección de fútbol o equipo específico.
	 *
	 * @param idTeam Identificador único de la selección o equipo a consultar.
	 * @return Un objeto {@link ResponseEntity} con la subcolección correspondiente
	 *         y el estado HTTP 200 (OK).
	 */
	@GetMapping("/team")
	public ResponseEntity<?> getByTeam(@RequestParam Long idTeam) {
		try {
			return ResponseEntity.ok().body(stickerserv.findByTeam(idTeam));
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener los stickers", "success", false));
		}
	}

	// ✅ CORRECCIÓN: @PathVariable en lugar de @RequestParam
	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
	    try {
	        return ResponseEntity.ok().body(stickerserv.findById(id));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", "Sticker no encontrado", "success", false));
	    }
	}

	

	
	/**
	 * Realiza una auditoría cuantitativa devolviendo el número total de stickers
	 * almacenados en el backend.
	 * <p>
	 * En caso de que la base de datos se encuentre vacía, responde con un estado
	 * HTTP 204 (No Content) adjuntando un mensaje aclaratorio dentro de la
	 * respuesta.
	 * </p>
	 *
	 * @return Un objeto {@link ResponseEntity} que encapsula el total numérico
	 *         calculado dentro de una estructura JSON mapeada.
	 */
	@GetMapping("/count")
	public ResponseEntity<?> countAll() {
		try {
			Long count = stickerserv.count();

			if (count == 0) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT)
						.body(Map.of("count", count, "message", "No hay stickers registrados"));
			}

			return ResponseEntity.ok(Map.of("count", count, "success", true));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al contar los stickers", "success", false));
		}
	}
}