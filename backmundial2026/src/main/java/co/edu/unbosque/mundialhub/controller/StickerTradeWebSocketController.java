package co.edu.unbosque.mundialhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.mundialhub.dto.TradeRequestDTO;
import co.edu.unbosque.mundialhub.service.TradeService;

@RestController

public class StickerTradeWebSocketController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Intercepta la propuesta final de intercambio múltiple y la procesa.
     * En el frontend, se debe enviar a la ruta /app/trade/propose
     */
    @MessageMapping("/trade/propose")
    public void processStickerExchange(TradeRequestDTO tradeRequest) {
        try {
            // Ejecutamos la lógica transaccional atómica
            tradeService.executeMultipleTrade(
                    tradeRequest.getSessionUuid(),
                    tradeRequest.getFromUserId(),
                    tradeRequest.getToUserId(),
                    tradeRequest.getFromStickerIds(),
                    tradeRequest.getToStickerIds()
            );

            // Mensaje de éxito a enviar
            String successMessage = "¡Intercambio exitoso de " + tradeRequest.getFromStickerIds().size() + " láminas!";

            // Notificamos al receptor (ToUser)
            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getToUserId().toString(),
                    "/queue/trade-notifications",
                    successMessage
            );

            // Notificamos al emisor (FromUser)
            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getFromUserId().toString(),
                    "/queue/trade-notifications",
                    successMessage
            );

        } catch (Exception e) {
            // Si algo falla (inventario insuficiente, paridad incorrecta, etc.)
            // Notificamos el error específicamente a quien envió la petición
            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getFromUserId().toString(),
                    "/queue/trade-errors",
                    "Error en el intercambio: " + e.getMessage()
            );
        }
    }
    @MessageMapping("/trade/join")
    public void notifyJoin(TradeRequestDTO tradeRequest) {
        // Notificar al Usuario A que el B ya está conectado y listo
        messagingTemplate.convertAndSendToUser(
            tradeRequest.getToUserId().toString(),
            "/queue/trade-notifications",
            "JOINED:" + tradeRequest.getFromUserId()
        );
        // Notificar al Usuario B con el ID del A
        messagingTemplate.convertAndSendToUser(
            tradeRequest.getFromUserId().toString(),
            "/queue/trade-notifications",
            "JOINED:" + tradeRequest.getToUserId()
        );
    }
}