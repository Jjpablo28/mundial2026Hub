package co.edu.unbosque.mundialhub.service;

import co.edu.unbosque.mundialhub.dto.QrDTO;
import co.edu.unbosque.mundialhub.model.TradeSession;
import co.edu.unbosque.mundialhub.repository.TradeSessionRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TradeSessionService {

    @Autowired
    private TradeSessionRepository sessionRepo;
    
    @Autowired
    private UserRepository userRepo;

    // Puedes definir esta propiedad en tu src/main/resources/application.properties
    // Ejemplo: api.qr.url=http://localhost:8081/api/v1/generate-qr
    @Value("${api.qr.url}")
    private String apiQrUrl;

    /**
     * Consume la API externa, recupera el UUID/Base64 e inserta la sesión en BD.
     * * @param userId ID del Usuario A que quiere mostrar su QR.
     * @return El QrDTO listo para que el controlador lo envíe al Frontend.
     */
    public QrDTO createTradeSession(Long userId) {
        
        // 1. Validar que el usuario que solicita el intercambio exista en el sistema
        userRepo.findById(userId.intValue())
                .orElseThrow(() -> new RuntimeException("El usuario con ID " + userId + " no existe."));

        // 2. Consumir tu API externa usando tu handler HTTP
        QrDTO qrData = ExternalHTTPRequestHandler.getQrFromQrApi(apiQrUrl);
        
        if (qrData == null || qrData.getUuid() == null) {
            throw new RuntimeException("La API externa retornó una estructura de QR vacía o inválida.");
        }

        // 3. Registrar la sesión intermedia en la base de datos de MundialHub
        TradeSession session = new TradeSession();
        session.setUuid(qrData.getUuid());
        session.setUserAId(userId);
        session.setUserBId(null); // Queda vacío (null) hasta que el Usuario B escanee el código
        session.setStatus("PENDING");
        
        sessionRepo.save(session);

        // 4. Retornar el DTO (Contiene el Base64 que el Frontend usará en una etiqueta <img src="data:image/png;base64,..."/>)
        return qrData;
    }
}