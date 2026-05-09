package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.mundialhub.service.StickerService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/sticker")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082" })
@Tag(name = "stickers", description = "CRUD stickers")
public class StickerController {

	@Autowired
	private StickerService stickerserv;

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
	
	@GetMapping("/team")
	public ResponseEntity<?>getByTeam(@RequestParam Long idTeam){
		try {
			return ResponseEntity.ok().body(stickerserv.findByTeam(idTeam));
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener los stickers", "success", false));

		}
	}

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