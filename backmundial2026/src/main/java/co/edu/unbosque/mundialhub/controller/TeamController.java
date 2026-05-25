package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import co.edu.unbosque.mundialhub.dto.TeamDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.repository.TeamRepository;
import co.edu.unbosque.mundialhub.service.ExternalHTTPRequestHandler;
import co.edu.unbosque.mundialhub.service.StickerService;
import co.edu.unbosque.mundialhub.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de exponer y gestionar los endpoints
 * correspondientes a las selecciones nacionales de fútbol (equipos), el
 * calendario de partidos y la tabla de posiciones.
 * <p>
 * Centraliza las operaciones CRUD locales para el mantenimiento de equipos y
 * delega en {@link ExternalHTTPRequestHandler} el consumo asíncrono y
 * autenticado de la API externa Football-Data para recuperar estadísticas en
 * tiempo real de la Copa del Mundo.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see RestController
 * @see CrossOrigin
 * @see TeamService
 */
@RestController
@RequestMapping("/teams")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082",
		"https://mundial2026hub.netlify.app" })
@Tag(name = "teams", description = "CRUD teams")
public class TeamController {

	@Autowired
	private TeamService teamServ;

	@Autowired
	private TeamRepository teamRepo;

	@Autowired
	private StickerService stickerserv;

	/**
	 * Token secreto de autenticación inyectado para el consumo del API externa
	 * Football-Data.
	 */
	@Value("${api.footbalData}")
	private String apiKey;

	/**
	 * Crea y registra una nueva selección nacional en el sistema.
	 *
	 * @param dto Estructura de transferencia de datos (DTO) con los datos del
	 *            equipo a crear.
	 * @return Un objeto {@link ResponseEntity} con el mensaje JSON de confirmación
	 *         y el estado HTTP 201 (Created).
	 */
	@PostMapping
	public ResponseEntity<?> create(@RequestBody TeamDTO dto) {
	    Exceptions exValidator = new Exceptions();

	    if (dto.getName() != null && exValidator.containsHtmlSymbols(dto.getName())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "El nombre del equipo contiene caracteres no permitidos", "success", false));
	    }

	    try {
	        teamServ.create(dto);
	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body(Map.of("message", "El equipo ha sido creado", "success", true));
	    } catch (Exceptions.ValidationException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(Map.of("message", e.getMessage(), "success", false));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("message", "Error interno al crear el equipo", "success", false));
	    }
	}

	

	/**
	 * Recupera el listado global de equipos registrados bajo un ordenamiento
	 * condicional.
	 * <p>
	 * Este endpoint evalúa dinámicamente el parámetro de ruta {@code order} por
	 * medio de expresiones regulares. Si recibe el valor "ASC" o "DESC", invoca las
	 * consultas de ordenación correspondientes en el servicio; en cualquier otro
	 * caso o ante la ausencia del parámetro, retorna la lista por defecto.
	 * </p>
	 *
	 * @param order Criterio opcional de ordenación ("ASC" o "DESC").
	 * @return Un objeto {@link ResponseEntity} con el listado de equipos y el
	 *         estado HTTP 200 (OK).
	 */
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

	/**
	 * Realiza una consulta externa a la API de Football-Data para recuperar la
	 * minuta y detalles específicos de un partido por su identificador único.
	 *
	 * @param id Identificador único del partido en la plataforma de destino.
	 * @return Un objeto {@link ResponseEntity} con la estructura JSON original de
	 *         la API o un mapa con el error técnico correspondiente.
	 */
	@GetMapping("/matches/{id}")
	public ResponseEntity<?> getMatchById(@PathVariable Long id) {
		try {
			String url = "https://api.football-data.org/v4/matches/" + id;
			String responseBody = ExternalHTTPRequestHandler.doGetWithAuthToken(url, apiKey);

			if (responseBody != null) {
				return ResponseEntity.ok(responseBody);
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
						"Error al obtener los detalles del partido", "error", "La API no devolvió datos"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Error interno procesando el partido", "error", e.getMessage()));
		}
	}

	/**
	 * Recupera la tabla de posiciones y clasificaciones (Standings) actualizadas de
	 * la Copa del Mundo consumiendo la infraestructura del proveedor externo.
	 *
	 * @return Un objeto {@link ResponseEntity} con los grupos y puntajes en formato
	 *         JSON.
	 */
	@GetMapping("/standings")
	public ResponseEntity<?> getStandings() {
		String url = "https://api.football-data.org/v4/competitions/WC/standings";
		String responseBody = ExternalHTTPRequestHandler.doGetWithAuthToken(url, apiKey);

		if (responseBody != null) {
			return ResponseEntity.ok(responseBody);
		} else {
			return ResponseEntity.internalServerError()
					.body("{\"error\": \"No se pudieron obtener las posiciones (standings)\"}");
		}
	}

	/**
	 * Localiza y extrae la información de una selección nacional utilizando su
	 * identificador numérico local.
	 * <p>
	 * El endpoint restringe mediante expresiones regulares que la variable de ruta
	 * sea exclusivamente un valor de tipo numérico.
	 * </p>
	 *
	 * @param id Identificador numérico (llave primaria) del equipo.
	 * @return Un objeto {@link ResponseEntity} con el DTO correspondiente, o un
	 *         estado HTTP 404 (Not Found) si no existe.
	 */
	@GetMapping("/{id:\\d+}")
	public ResponseEntity<?> getById(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(teamServ.findById(id));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Equipo no encontrado", "success", false));
		}
	}

	/**
	 * Realiza una auditoría cuantitativa del número total de selecciones de fútbol
	 * almacenadas localmente.
	 *
	 * @return Un objeto {@link ResponseEntity} con el conteo de registros. Retorna
	 *         estado HTTP 204 (No Content) si es igual a cero.
	 */
	@GetMapping("/count")
	public ResponseEntity<?> countAll() {
		Long count = teamServ.count();

		if (count == 0) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(count);
		}
		return ResponseEntity.ok(count);
	}

	/**
	 * Realiza una auditoría cuantitativa secundaria para obtener el conteo de los
	 * stickers disponibles en el inventario global.
	 *
	 * @return Un objeto {@link ResponseEntity} con el total numérico de cromos
	 *         procesados.
	 */
	@GetMapping("countpl")
	public ResponseEntity<?> countAllPl() {
		Long count = stickerserv.count();

		if (count == 0) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(count);
		}
		return ResponseEntity.ok(count);
	}

	/**
	 * Remueve de forma definitiva un equipo del catálogo del sistema utilizando su
	 * identificador único.
	 *
	 * @param id Identificador numérico del equipo que se desea eliminar.
	 * @return Un mapa JSON descriptivo que detalla el éxito o fallo del proceso de
	 *         eliminación.
	 */

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable Long id) {
	    try {
	        teamServ.deleteById(id);
	        return ResponseEntity.ok(Map.of("message", "Equipo eliminado con éxito", "success", true));
	    } catch (Exceptions.ResourceNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", e.getMessage(), "success", false));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", "Equipo no encontrado", "success", false));
	    }
	}


	/**
	 * Modifica y actualiza las propiedades y metadatos de un equipo localizándolo
	 * mediante su ID.
	 *
	 * @param id  Identificador numérico de la selección a modificar.
	 * @param dto Estructura de transferencia de datos con los nuevos valores a
	 *            guardar.
	 * @return Un objeto {@link ResponseEntity} indicando el éxito técnico de la
	 *         actualización.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody TeamDTO dto) {
	    Exceptions exValidator = new Exceptions();

	    if (dto.getName() != null && exValidator.containsHtmlSymbols(dto.getName())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", "El nombre contiene caracteres no permitidos", "success", false));
	    }

	    try {
	        teamServ.updateById(id, dto);
	        return ResponseEntity.ok(Map.of("message", "Equipo actualizado correctamente", "success", true));
	    } catch (Exceptions.ResourceNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", e.getMessage(), "success", false));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("message", e.getMessage(), "success", false));
	    }
	}

	/**
	 * Consume de manera masiva los fixtures de partidos de fútbol y programaciones
	 * de la Copa del Mundo de la FIFA delegando en los servicios remotos.
	 *
	 * @return Un objeto {@link ResponseEntity} con el compendio completo de
	 *         encuentros deportivos.
	 */
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