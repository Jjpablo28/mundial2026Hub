package co.edu.unbosque.mundialhub.service;

import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.TradeSession;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.UserSticker;
import co.edu.unbosque.mundialhub.repository.StickerRepository;
import co.edu.unbosque.mundialhub.repository.TradeSessionRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.repository.UserStickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TradeService {

    @Autowired
    private UserStickerRepository userStickerRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StickerRepository stickerRepo;

    @Autowired
    private TradeSessionRepository sessionRepo;
    
    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Ejecuta el intercambio atómico de múltiples láminas.
     * Si cualquier validación falla, se revierte toda la transacción.
     */
    @Transactional
    public void executeMultipleTrade(String sessionUuid, Long fromUserId, Long toUserId, List<Long> fromStickers, List<Long> toStickers) {
        
        // 1. Validar la sesión
        TradeSession session = sessionRepo.findByUuid(sessionUuid)
                .orElseThrow(() -> new RuntimeException("Sesión de intercambio no válida o inexistente."));
        
        if ("COMPLETED".equals(session.getStatus())) {
            throw new RuntimeException("Este intercambio ya fue procesado previamente.");
        }

        // 2. Validar Paridad (misma cantidad de láminas)
        if (fromStickers.size() != toStickers.size()) {
            throw new RuntimeException("Intercambio rechazado: La cantidad de láminas debe ser exactamente igual para ambas partes.");
        }

        // 3. Validar Propiedad y Cantidad Exacta (Evita fraude si mandan IDs repetidos)
        validarInventario(fromUserId, fromStickers);
        validarInventario(toUserId, toStickers);

        // 4. Ejecutar el cruce de láminas
        procesarTransferencia(fromUserId, toUserId, fromStickers);
        procesarTransferencia(toUserId, fromUserId, toStickers);

        // 5. Marcar sesión como completada
        session.setStatus("COMPLETED");
        sessionRepo.save(session);
        activityLogService.log(fromUserId, "TRADE_COMPLETED", "Intercambio completado con usuario ID " + toUserId + ". Láminas enviadas: " + fromStickers.size());
        activityLogService.log(toUserId,   "TRADE_COMPLETED", "Intercambio completado con usuario ID " + fromUserId + ". Láminas recibidas: " + toStickers.size());

    }

    /**
     * Valida que el usuario tenga suficientes copias de las láminas solicitadas.
     */
    private void validarInventario(Long userId, List<Long> stickerIds) {
        // Agrupar cuántas veces piden la misma lámina en la lista (ej: dos láminas de Messi = {10: 2})
        Map<Long, Long> requiredQuantities = stickerIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        for (Map.Entry<Long, Long> entry : requiredQuantities.entrySet()) {
            Long stickerId = entry.getKey();
            Long quantityNeeded = entry.getValue();

            UserSticker us = userStickerRepo.findByUserIdAndStickerId(userId, stickerId)
                    .orElseThrow(() -> new RuntimeException("El usuario ID " + userId + " no posee la lámina ID " + stickerId));

            if (us.getQuantity() < quantityNeeded) {
                throw new RuntimeException("El usuario ID " + userId + " no tiene copias suficientes de la lámina ID " + stickerId);
            }
        }
    }

    /**
     * Mueve las láminas de un inventario a otro, actualizando o borrando según corresponda.
     */
    private void procesarTransferencia(Long emisorId, Long receptorId, List<Long> stickerIds) {
        // Buscamos al usuario receptor usando findById (asumiendo que tu repositorio recibe Long o Integer)
        // Si tu UserRepository usa Integer en el findById, hacemos el cast (.intValue())
        User receptor = userRepo.findById(receptorId.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado."));

        for (Long stickerId : stickerIds) {
            
            // A. Restar del Emisor
            UserSticker emisorSticker = userStickerRepo.findByUserIdAndStickerId(emisorId, stickerId).get();
            if (emisorSticker.getQuantity() > 1) {
                emisorSticker.setQuantity(emisorSticker.getQuantity() - 1);
                userStickerRepo.save(emisorSticker);
            } else {
                userStickerRepo.delete(emisorSticker); // Si solo le quedaba 1, se borra el registro de posesión
            }

            // B. Sumar al Receptor
            Optional<UserSticker> receptorStickerOpt = userStickerRepo.findByUserIdAndStickerId(receptorId, stickerId);
            if (receptorStickerOpt.isPresent()) {
                // Ya la tenía, se suma una copia más a la pila
                UserSticker receptorSticker = receptorStickerOpt.get();
                receptorSticker.setQuantity(receptorSticker.getQuantity() + 1);
                receptorSticker.setObtainedAt(LocalDateTime.now());
                userStickerRepo.save(receptorSticker);
            } else {
                // No la tenía, se crea nuevo registro asociado al álbum del receptor
                Sticker stickerModel = stickerRepo.findById(stickerId)
                        .orElseThrow(() -> new RuntimeException("Lámina maestra no encontrada en el catálogo."));
                
                UserSticker nuevoSticker = new UserSticker();
                nuevoSticker.setUser(receptor);
                nuevoSticker.setSticker(stickerModel);
                nuevoSticker.setQuantity(1);
                nuevoSticker.setObtainedAt(LocalDateTime.now());
                userStickerRepo.save(nuevoSticker);
            }
        }
    }
}