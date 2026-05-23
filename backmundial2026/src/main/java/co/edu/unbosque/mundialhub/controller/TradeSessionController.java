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

    // ✅ NUEVO: Inyectamos el template de mensajería para notificar por WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Paso 1: El Usuario A solicita iniciar un intercambio.
     * Retorna el JSON con el uuid y el base64 del QR generado por la API externa.
     */
    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createSession(@PathVariable Long userId) {
        try {
            QrDTO qrData = sessionService.createTradeSession(userId);
            return ResponseEntity.ok(qrData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear sesión: " + e.getMessage());
        }
    }

    /**
     * Paso 2: El Usuario B escanea el código QR (o pega el UUID) y se une a la sesión.
     *
     * ✅ FIX CRÍTICO: Una vez que el Usuario B se registra correctamente,
     * se notifica al Usuario A por WebSocket con el formato "JOINED:{userBId}"
     * para que el frontend del Usuario A sepa quién es su contraparte y
     * pueda cargar los inventarios y navegar a la pantalla de intercambio.
     */
    @PostMapping("/join/{uuid}/user/{userBId}")
    public ResponseEntity<String> joinSession(
            @PathVariable String uuid,
            @PathVariable Long userBId) {
        try {
            TradeSession session = sessionRepo.findByUuid(uuid)
                    .orElseThrow(() -> new RuntimeException(
                        "La sesión de intercambio no existe o el QR ha expirado."));

            if (!"PENDING".equals(session.getStatus())) {
                return ResponseEntity.badRequest()
                        .body("Esta sesión ya se encuentra activa o ha finalizado.");
            }

            if (session.getUserAId().equals(userBId)) {
                return ResponseEntity.badRequest()
                        .body("Operación inválida: No puedes realizar un intercambio contigo mismo.");
            }

            // Actualizamos el estado de la sesión
            session.setUserBId(userBId);
            session.setStatus("NEGOTIATING");
            sessionRepo.save(session);

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
