package co.edu.unbosque.mundialhub.service;

import co.edu.unbosque.mundialhub.dto.QrDTO;
import co.edu.unbosque.mundialhub.model.TradeSession;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.repository.TradeSessionRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class TradeSessionService {

    @Autowired
    private TradeSessionRepository sessionRepo;

    @Autowired
    private UserRepository userRepo;

    // Ejemplo en application.properties:  api.qr.url=http://localhost:8081/api/v1/generate-qr
    @Value("${api.qr.url}")
    private String apiQrUrl;

    // Caracteres permitidos: letras mayúsculas + dígitos, sin O/0/I/1 para evitar confusión visual
    private static final String SHORT_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    // ── Crear sesión (genera QR + shortCode + guarda email del usuario A) ────

    /**
     * Consume la API externa, genera el shortCode y registra la sesión en BD.
     *
     * @param userId ID del Usuario A que quiere mostrar su QR.
     * @return El QrDTO listo para enviarlo al Frontend (incluye uuid, base64 y shortCode).
     */
    public QrDTO createTradeSession(Long userId) {

        // 1. Validar existencia del usuario
        User userA = userRepo.findById(userId.intValue())
                .orElseThrow(() -> new RuntimeException("El usuario con ID " + userId + " no existe."));

        // 2. Consumir API externa de QR
        QrDTO qrData = ExternalHTTPRequestHandler.getQrFromQrApi(apiQrUrl);

        if (qrData == null || qrData.getUuid() == null) {
            throw new RuntimeException("La API externa retornó una estructura de QR vacía o inválida.");
        }

        // 3. Generar shortCode único (reintenta si hay colisión, muy improbable)
        String shortCode = generateUniqueShortCode();

        // 4. Registrar la sesión en BD
        TradeSession session = new TradeSession();
        session.setUuid(qrData.getUuid());
        session.setShortCode(shortCode);
        session.setUserAId(userId);
        session.setUserAEmail(userA.getEmail()); // Guarda el email para búsqueda posterior
        session.setUserBId(null);                // Vacío hasta que B escanee / ingrese el código
        session.setStatus("PENDING");

        sessionRepo.save(session);

        // 5. Adjuntar el shortCode al DTO para que el Frontend pueda mostrarlo
        qrData.setShortCode(shortCode);

        return qrData;
    }

    // ── Unirse por UUID completo ─────────────────────────────────────────────

    /**
     * Une al Usuario B usando el UUID completo (extraído del QR escaneado).
     */
    public TradeSession joinByUuid(String uuid, Long userBId) {
        TradeSession session = sessionRepo.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(
                        "No existe ninguna sesión con el UUID proporcionado."));
        return joinSession(session, userBId);
    }

    // ── Unirse por shortCode ─────────────────────────────────────────────────

    /**
     * Busca la sesión por el código alfanumérico corto.
     *
     * @param shortCode Código de 6 caracteres que el Usuario B recibió verbalmente.
     * @param userBId   ID del Usuario B que se quiere unir.
     * @return La TradeSession actualizada.
     */
    public TradeSession joinByShortCode(String shortCode, Long userBId) {
        TradeSession session = sessionRepo.findByShortCode(shortCode.toUpperCase())
                .orElseThrow(() -> new RuntimeException(
                        "No existe ninguna sesión activa con el código '" + shortCode + "'."));

        return joinSession(session, userBId);
    }

    // ── Unirse por email del Usuario A ───────────────────────────────────────

    /**
     * Busca la sesión PENDING más reciente asociada al email del Usuario A.
     *
     * @param email   Correo del Usuario A (el que generó el QR).
     * @param userBId ID del Usuario B que se quiere unir.
     * @return La TradeSession actualizada.
     */
    public TradeSession joinByEmail(String email, Long userBId) {
        TradeSession session = sessionRepo
                .findTopByUserAEmailAndStatusOrderByCreatedAtDesc(email.toLowerCase().trim(), "PENDING")
                .orElseThrow(() -> new RuntimeException(
                        "No hay ninguna sesión activa para el correo '" + email + "'."));

        return joinSession(session, userBId);
    }

    // ── Lógica compartida de unión ───────────────────────────────────────────

    private TradeSession joinSession(TradeSession session, Long userBId) {
        if ("COMPLETED".equals(session.getStatus()) || "CANCELLED".equals(session.getStatus())) {
            throw new RuntimeException("Esta sesión ya no está disponible (estado: " + session.getStatus() + ").");
        }
        if (session.getUserBId() != null && !session.getUserBId().equals(userBId)) {
            throw new RuntimeException("Esta sesión ya tiene un participante asignado.");
        }

        session.setUserBId(userBId);
        session.setStatus("NEGOTIATING");
        sessionRepo.save(session);

        return session;
    }

    // ── Generador de shortCode ───────────────────────────────────────────────

    private String generateUniqueShortCode() {
        String code;
        int attempts = 0;
        do {
            code = generateShortCode();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("No se pudo generar un shortCode único. Intenta de nuevo.");
            }
        } while (sessionRepo.findByShortCode(code).isPresent());
        return code;
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(SHORT_CODE_CHARS.charAt(RANDOM.nextInt(SHORT_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}