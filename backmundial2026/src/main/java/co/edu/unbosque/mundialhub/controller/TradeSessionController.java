package co.edu.unbosque.mundialhub.controller;

import co.edu.unbosque.mundialhub.dto.QrDTO;
import co.edu.unbosque.mundialhub.model.TradeSession;
import co.edu.unbosque.mundialhub.service.TradeSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de sesiones de intercambio.
 *
 * Endpoints de unión disponibles:
 *  - POST /trade-session/join?sessionUuid=&userBId=       (UUID completo / escaneo QR)
 *  - POST /trade-session/join-by-code?shortCode=&userBId= (código alfanumérico corto)
 *  - POST /trade-session/join-by-email?email=&userBId=    (correo del Usuario A)
 */
@RestController
@RequestMapping("/trade-session")
@CrossOrigin(origins = "*")
public class TradeSessionController {

    @Autowired
    private TradeSessionService sessionService;

    // ── Crear sesión ─────────────────────────────────────────────────────────

    /**
     * Crea una nueva sesión de intercambio para el Usuario A.
     * Retorna el QR en Base64 + el shortCode alfanumérico.
     */
    @PostMapping("/create")
    public ResponseEntity<QrDTO> createSession(@RequestParam Long userId) {
        QrDTO result = sessionService.createTradeSession(userId);
        return ResponseEntity.ok(result);
    }

    // ── Unirse por UUID completo (escaneo QR o pegar URL) ────────────────────

    /**
     * Une al Usuario B mediante el UUID completo extraído del QR.
     * Retorna un String plano con el ID del Usuario A para que el Frontend
     * pueda establecer el canal WebSocket correcto.
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinSession(
            @RequestParam String sessionUuid,
            @RequestParam Long userBId) {

        TradeSession session = sessionService.joinByUuid(sessionUuid, userBId);
        return ResponseEntity.ok("Unido exitosamente. Usuario A ID: " + session.getUserAId());
    }

    // ── Unirse por código alfanumérico corto ─────────────────────────────────

    /**
     * Une al Usuario B mediante el código de 6 caracteres mostrado junto al QR.
     * Ideal para intercambios presenciales donde B no puede escanear.
     */
    @PostMapping("/join-by-code")
    public ResponseEntity<String> joinByShortCode(
            @RequestParam String shortCode,
            @RequestParam Long userBId) {

        TradeSession session = sessionService.joinByShortCode(shortCode, userBId);
        return ResponseEntity.ok("Unido exitosamente. Usuario A ID: " + session.getUserAId());
    }

    // ── Unirse por email del Usuario A ───────────────────────────────────────

    /**
     * Une al Usuario B buscando la sesión PENDING más reciente del correo indicado.
     * Útil cuando el Usuario B conoce el email del anfitrión pero no tiene el QR.
     */
    @PostMapping("/join-by-email")
    public ResponseEntity<String> joinByEmail(
            @RequestParam String email,
            @RequestParam Long userBId) {

        TradeSession session = sessionService.joinByEmail(email, userBId);
        return ResponseEntity.ok("Unido exitosamente. Usuario A ID: " + session.getUserAId());
    }
}