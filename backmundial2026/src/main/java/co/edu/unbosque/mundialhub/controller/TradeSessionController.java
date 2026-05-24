package co.edu.unbosque.mundialhub.controller;

import co.edu.unbosque.mundialhub.dto.QrDTO;
import co.edu.unbosque.mundialhub.model.TradeSession;
import co.edu.unbosque.mundialhub.repository.TradeSessionRepository;
import co.edu.unbosque.mundialhub.service.TradeSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST convencional encargado de inicializar y emparejar
 * las sesiones de intercambio mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/trade-session")
@CrossOrigin(origins = {
    "http://localhost:8080",
    "http://localhost:8081",
    "http://localhost:8082",
    "https://mundial2026hub.netlify.app"
})
@Tag(name = "Trade Session", description = "Endpoints HTTP para la gestión de sesiones de intercambio mediante QR")
public class TradeSessionController {

    @Autowired
    private TradeSessionService sessionService;

    @Autowired
    private TradeSessionRepository sessionRepo;

    // ✅ Inyectamos el template de mensajería para notificar por WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Crea una nueva sesión de intercambio bajo demanda para el Host (Usuario A).
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSession(@RequestParam Long userId) {
        try {
            QrDTO qrData = sessionService.createTradeSession(userId);
            return ResponseEntity.ok(qrData);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al crear la sesión de intercambio: " + e.getMessage());
        }
    }

    /**
     * Endpoint HTTP que procesa la entrada del Invitado (Usuario B) a la sala segura.
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinSession(@RequestParam String sessionUuid, @RequestParam Long userBId) {
        try {
            TradeSession session = sessionRepo.findByUuid(sessionUuid)
                    .orElseThrow(() -> new RuntimeException("Sesión no encontrada con el UUID proporcionado."));

            if (session.getUserAId().equals(userBId)) {
                return ResponseEntity.badRequest()
                        .body("Operación inválida: No puedes realizar un intercambio contigo mismo.");
            }

            // Actualizamos las propiedades de la sesión con los datos de la contraparte
            session.setUserBId(userBId);
            session.setStatus("NEGOTIATING");
            
            // ✅ FIX: Guardado síncrono inmediato (Flush) para escribir directo en la base de datos
            sessionRepo.saveAndFlush(session);

            // ✅ NOTIFICACIÓN AL USUARIO A: Le avisamos que su contraparte llegó
            // El frontend detecta el prefijo "JOINED:" y extrae el ID del Usuario B
            messagingTemplate.convertAndSendToUser(
                    session.getUserAId().toString(),
                    "/queue/trade-notifications",
                    "JOINED:" + userBId
            );

            // ✅ NOTIFICACIÓN AL USUARIO B: Le confirmamos que entró correctamente
            // y le enviamos el ID del Usuario A para que él también sepa su contraparte
            messagingTemplate.convertAndSendToUser(
                    userBId.toString(),
                    "/queue/trade-notifications",
                    "JOINED:" + session.getUserAId()
            );

            return ResponseEntity.ok(
                "Te has unido exitosamente a la sesión de intercambio con el usuario ID: "
                + session.getUserAId()
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al unirse a la sesión: " + e.getMessage());
        }
    }
}