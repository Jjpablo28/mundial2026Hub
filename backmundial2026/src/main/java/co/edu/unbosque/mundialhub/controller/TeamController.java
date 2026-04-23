package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import co.edu.unbosque.mundialhub.dto.TeamDTO;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.repository.TeamRepository;
import co.edu.unbosque.mundialhub.service.ExternalHTTPRequestHandler;
import co.edu.unbosque.mundialhub.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/teams")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082" })
@Tag(name = "teams", description = "CRUD teams")
public class TeamController {

	@Autowired
	private TeamService teamServ;

	@Autowired
	private TeamRepository teamRepo;
	@Value("${api.footbalData}")
	private String apiKey;

	@PostMapping
	public ResponseEntity<?> create(@RequestBody TeamDTO dto) {

		teamServ.create(dto);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", "El equipo ha sido creado", "success", true));
	}

	@GetMapping({ "", "/", "/{order:[a-zA-Z]+}" })
	public ResponseEntity<?> getAll(@PathVariable(required = false) String order) {
		try {
			if ("ASC".equalsIgnoreCase(order)) {
				return ResponseEntity.ok(teamServ.findAllASC());
			} else if ("DESC".equalsIgnoreCase(order)) {
				return ResponseEntity.ok(teamServ.findAllDESC());
			} else {
				return ResponseEntity.ok(teamServ.findAll());
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error al obtener equipos", "success", false));
		}
	}

	@GetMapping("/matches/{id}")
	public ResponseEntity<?> getMatchById(@PathVariable Long id) {
		try {
			String url = "https://api.football-data.org/v4/matches/" + id;
			
			// Llamamos a tu clase HTTP pasándole la URL dinámica y tu apiKey
			String responseBody = ExternalHTTPRequestHandler.doGetWithAuthToken(url, apiKey);

			if (responseBody != null) {
				return ResponseEntity.ok(responseBody);
			} else {
				// Manejo en caso de que la petición falle o devuelva null
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("message", "Error al obtener los detalles del partido", "error", "La API no devolvió datos"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error interno procesando el partido", "error", e.getMessage()));
		}
	}
	
	@GetMapping("/standings")
	public ResponseEntity<?> getStandings() {
		String url = "https://api.football-data.org/v4/competitions/WC/standings";
		
		String responseBody = ExternalHTTPRequestHandler.doGetWithAuthToken(url, apiKey);

		if (responseBody != null) {
			return ResponseEntity.ok(responseBody);
		} else {
			return ResponseEntity.internalServerError().body("{\"error\": \"No se pudieron obtener las posiciones (standings)\"}");
		}
	}
	@GetMapping("/{id:\\d+}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(teamServ.findById(id));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Equipo no encontrado", "success", false));
		}
	}

	@GetMapping("/count")
	public ResponseEntity<?> countAll() {

		Long count = teamServ.count();

		if (count == 0) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(count);
		}

		return ResponseEntity.ok(count);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable Long id) {

		try {
			teamServ.deleteById(id);

			return ResponseEntity.ok(Map.of("message", "Equipo eliminado con éxito", "success", true));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Equipo no encontrado", "success", false));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody TeamDTO dto) {
		try {
			TeamDTO updated = teamServ.updateById(id, dto);

			Team userEntity = teamRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			return ResponseEntity.ok(Map.of("message", "Equipo actualizado correctamente",

					"success", true

			));

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
		}
	}
	@GetMapping("/matches")
	public ResponseEntity<?> getMatches() {
		String url = "https://api.football-data.org/v4/competitions/WC/matches";
		
		
		String responseBody = ExternalHTTPRequestHandler.doGetWithAuthToken(url, apiKey);

		if (responseBody != null) {
			return ResponseEntity.ok(responseBody);
		} else {
			return ResponseEntity.internalServerError().body("{\"error\": \"No se pudieron obtener los partidos\"}");
		}
	}
	

}
