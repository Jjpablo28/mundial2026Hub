package co.edu.unbosque.mundialhub.controller;

import co.edu.unbosque.mundialhub.dto.QrDTO;
import co.edu.unbosque.mundialhub.model.TradeSession;
import co.edu.unbosque.mundialhub.repository.TradeSessionRepository;
import co.edu.unbosque.mundialhub.service.TradeSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST convencional encargado de inicializar y emparejar
 * las sesiones de intercambio mediante peticiones HTTP.
 */
@RestController
@RequestMapping("/trade-session")
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:8082", "https://mundial2026hub.netlify.app" })
@Tag(name = "Trade Session", description = "Endpoints HTTP para la gestión de sesiones de intercambio mediante QR")
public class TradeSessionController {

    @Autowired
    private TradeSessionService sessionService;
    
    @Autowired
    private TradeSessionRepository sessionRepo;

    /**
     * Paso 1: El Usuario A solicita iniciar un intercambio. 
     * Retorna el JSON con el uuid y el base64 de tu API externa.
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
     * Paso 2: El Usuario B escanea el código QR y se une a la sesión.
     */
    @PostMapping("/join/{uuid}/user/{userBId}")
    public ResponseEntity<String> joinSession(@PathVariable String uuid, @PathVariable Long userBId) {
        try {
            TradeSession session = sessionRepo.findByUuid(uuid)
                    .orElseThrow(() -> new RuntimeException("La sesión de intercambio no existe o el QR ha expirado."));
            
            if (!"PENDING".equals(session.getStatus())) {
                return ResponseEntity.badRequest().body("Esta sesión ya se encuentra activa o ha finalizado.");
            }
            
            if (session.getUserAId().equals(userBId)) {
                return ResponseEntity.badRequest().body("Operación inválida: No puedes realizar un intercambio contigo mismo.");
            }

            session.setUserBId(userBId);
            session.setStatus("NEGOTIATING");
            sessionRepo.save(session);
            
            return ResponseEntity.ok("Te has unido exitosamente a la sesión de intercambio con el usuario ID: " + session.getUserAId());
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al unirse a la sesión: " + e.getMessage());
        }
    }
}