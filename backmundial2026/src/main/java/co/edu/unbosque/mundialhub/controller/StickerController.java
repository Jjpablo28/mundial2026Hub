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
import co.edu.unbosque.mundialhub.service.StickerService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/sticker")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082","https://mundial2026hub.netlify.app" })
@Tag(name = "stickers", description = "CRUD stickers")
public class StickerController {

	@Autowired
	private StickerService stickerserv;

	// CREAR
	@PostMapping
	public ResponseEntity<?> create(@RequestBody StickerDTO dto) {
		stickerserv.create(dto);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", "El sticker ha sido creado", "success", true));
	}

	// ACTUALIZAR 
	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StickerDTO dto) {
		try {
			StickerDTO updated = stickerserv.updateById(id, dto);

			return ResponseEntity.ok(Map.of(
					"message", "Sticker actualizado correctamente",
					"success", true
			));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "success", false));
		}
	}

	// ELIMINAR
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable Long id) {
		try {
			stickerserv.deleteById(id);

			return ResponseEntity.ok(Map.of(
					"message", "Sticker eliminado con éxito", 
					"success", true
			));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Sticker no encontrado", "success", false));
		}
	}

	// GET
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
	
	// GET por equipo
	@GetMapping("/team")
	public ResponseEntity<?> getByTeam(@RequestParam Long idTeam){
		try {
			return ResponseEntity.ok().body(stickerserv.findByTeam(idTeam));
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener los stickers", "success", false));
		}
	}

	// CONTAR
	@GetMapping("/count")
	public ResponseEntity<?> countAll() {
		try {
			Long count = stickerserv.count();

			if (count == 0) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT)
						.body(Map.of("count", count, "message", "No hay stickers registrados"));
			}

			// Devolvemos el conteo dentro de un JSON estructurado
			return ResponseEntity.ok(Map.of("count", count, "success", true));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al contar los stickers", "success", false));
		}
	}
}