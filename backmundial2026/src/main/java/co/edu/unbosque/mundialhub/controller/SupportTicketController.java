package co.edu.unbosque.mundialhub.controller;

import co.edu.unbosque.mundialhub.dto.SupportTicketDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.SupportTicket.TicketStatus;
import co.edu.unbosque.mundialhub.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de tickets de soporte técnico/operativo.
 * <p>
 * Expone endpoints para que los agentes de soporte y administradores puedan:
 * <ul>
 *   <li>Crear tickets para acciones sobre usuarios (sobres, tokens, cuentas)</li>
 *   <li>Resolver o rechazar tickets aplicando automáticamente la acción de negocio</li>
 *   <li>Consultar y filtrar el historial de tickets</li>
 * </ul>
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
@RestController
@RequestMapping("/support")
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:8082",
        "https://mundial2026hub.netlify.app"
})
@Tag(name = "Support", description = "Gestión de tickets de soporte operativo")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /support/tickets?agentId={id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo ticket de soporte en estado OPEN.
     *
     * @param dto     Cuerpo del ticket con targetUserId, ticketType, quantity y notes.
     * @param agentId ID del agente autenticado que registra el ticket.
     * @return HTTP 201 con el DTO creado, o 400/404/500 según el error.
     */
    @PostMapping("/tickets")
    @Operation(summary = "Crear ticket de soporte")
    public ResponseEntity<?> createTicket(@RequestBody SupportTicketDTO dto,
                                          @RequestParam Long agentId) {
        if (agentId == null || agentId <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El ID del agente es inválido", "success", false));
        }
        try {
            SupportTicketDTO created = supportService.createTicket(dto, agentId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("data", created, "message", "Ticket creado exitosamente", "success", true));
        } catch (Exceptions.ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno: " + e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /support/tickets/{id}/resolve?agentId={id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resuelve un ticket aplicando la acción de negocio automática sobre el usuario destino.
     *
     * @param id         ID del ticket a resolver.
     * @param body       JSON con campo "resolution" (texto del agente).
     * @param agentId    ID del agente que ejecuta la resolución.
     * @return HTTP 200 con el ticket resuelto, o 400/404 según error.
     */
    @PutMapping("/tickets/{id}/resolve")
    @Operation(summary = "Resolver un ticket y aplicar la acción al usuario")
    public ResponseEntity<?> resolveTicket(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @RequestParam Long agentId) {
        String resolution = body.getOrDefault("resolution", "Resuelto por soporte");
        try {
            SupportTicketDTO updated = supportService.resolveTicket(id, resolution, agentId);
            return ResponseEntity.ok(Map.of("data", updated, "message", "Ticket resuelto correctamente", "success", true));
        } catch (Exceptions.ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al resolver el ticket: " + e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /support/tickets/{id}/reject?agentId={id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Rechaza un ticket sin ejecutar ninguna acción sobre el usuario.
     *
     * @param id      ID del ticket a rechazar.
     * @param body    JSON con campo "resolution" (motivo del rechazo).
     * @param agentId ID del agente que rechaza.
     * @return HTTP 200 con el ticket actualizado.
     */
    @PutMapping("/tickets/{id}/reject")
    @Operation(summary = "Rechazar un ticket de soporte")
    public ResponseEntity<?> rejectTicket(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          @RequestParam Long agentId) {
        String reason = body.getOrDefault("resolution", "Rechazado por soporte");
        try {
            SupportTicketDTO updated = supportService.rejectTicket(id, reason, agentId);
            return ResponseEntity.ok(Map.of("data", updated, "message", "Ticket rechazado", "success", true));
        } catch (Exceptions.ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage(), "success", false));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /support/tickets (paginado)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Obtiene todos los tickets paginados, con filtro opcional por estado.
     *
     * @param page   Número de página (0-indexed, por defecto 0).
     * @param size   Tamaño de página (por defecto 20).
     * @param status Filtro opcional por estado: OPEN, IN_PROGRESS, RESOLVED, REJECTED.
     * @return Página de DTOs de tickets.
     */
    @GetMapping("/tickets")
    @Operation(summary = "Listar todos los tickets (paginado, con filtro por estado)")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(required = false) TicketStatus status) {
        try {
            Page<SupportTicketDTO> result = (status != null)
                    ? supportService.findByStatusPaged(status, page, size)
                    : supportService.findAllPaged(page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al consultar tickets: " + e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /support/tickets/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/tickets/{id}")
    @Operation(summary = "Obtener un ticket por ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(supportService.findById(id));
        } catch (Exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /support/tickets/user/{userId}
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/tickets/user/{userId}")
    @Operation(summary = "Obtener tickets de un usuario destino")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        try {
            List<SupportTicketDTO> result = supportService.findByTargetUser(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /support/tickets/agent/{agentId}
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/tickets/agent/{agentId}")
    @Operation(summary = "Obtener tickets gestionados por un agente")
    public ResponseEntity<?> getByAgent(@PathVariable Long agentId) {
        try {
            List<SupportTicketDTO> result = supportService.findByAgent(agentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /support/stats
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna un resumen de estadísticas rápidas para el dashboard de soporte.
     *
     * @return Mapa con contadores por estado.
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas rápidas de tickets (dashboard)")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
                "open",        supportService.countByStatus(TicketStatus.OPEN),
                "in_progress", supportService.countByStatus(TicketStatus.IN_PROGRESS),
                "resolved",    supportService.countByStatus(TicketStatus.RESOLVED),
                "rejected",    supportService.countByStatus(TicketStatus.REJECTED)
        ));
    }
}