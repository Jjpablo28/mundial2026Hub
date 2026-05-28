package co.edu.unbosque.mundialhub.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.mundialhub.dto.TradeRequestDTO;
import co.edu.unbosque.mundialhub.service.TradeService;

/**
 * Controlador WebSocket encargado de gestionar en tiempo real los flujos de
 * negociación e intercambio de láminas entre usuarios de MundialHub.
 * <p>
 * Intercepta mensajes STOMP entrantes desde el frontend a través de los
 * destinos prefijados con {@code /app} y los retransmite a los canales
 * privados de los usuarios mediante {@link SimpMessagingTemplate}. Cubre
 * cuatro operaciones principales del protocolo de intercambio:
 * <ul>
 *   <li>Propuesta y ejecución atómica del intercambio múltiple.</li>
 *   <li>Notificación de unión a una sala entre dos contrapartes.</li>
 *   <li>Sincronización en tiempo real de las ofertas seleccionadas.</li>
 *   <li>Señalización de estado "listo" para confirmar el intercambio.</li>
 * </ul>
 * </p>
 * <p>
 * Aplica validaciones defensivas en cada handler para prevenir NullPointerException
 * ante payloads malformados o incompletos, y bloquea operaciones de intercambio
 * de un usuario consigo mismo.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see TradeService
 * @see SimpMessagingTemplate
 */
@RestController
public class StickerTradeWebSocketController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Intercepta la propuesta final de intercambio múltiple y ejecuta la
     * transacción atómica de láminas entre dos usuarios.
     * <p>
     * Realiza las siguientes validaciones antes de procesar:
     * <ol>
     *   <li>Verifica que el DTO no sea nulo y que contenga IDs de usuarios,
     *       UUID de sesión y listas de stickers no vacías.</li>
     *   <li>Bloquea el intercambio si el emisor y el receptor son el mismo usuario.</li>
     * </ol>
     * Si la transacción es exitosa, notifica a ambas partes por sus canales
     * privados {@code /queue/trade-notifications}. En caso de error, notifica
     * exclusivamente al emisor por {@code /queue/trade-errors}.
     * </p>
     *
     * @param tradeRequest DTO con los IDs de usuarios, UUID de sesión y listas de
     *                     stickers a intercambiar de cada parte.
     */
    @MessageMapping("/trade/propose")
    public void processStickerExchange(TradeRequestDTO tradeRequest) {
        if (tradeRequest == null
                || tradeRequest.getFromUserId() == null
                || tradeRequest.getToUserId() == null
                || tradeRequest.getSessionUuid() == null
                || tradeRequest.getFromStickerIds() == null
                || tradeRequest.getToStickerIds() == null
                || tradeRequest.getFromStickerIds().isEmpty()
                || tradeRequest.getToStickerIds().isEmpty()) {

            if (tradeRequest != null && tradeRequest.getFromUserId() != null) {
                messagingTemplate.convertAndSendToUser(
                        tradeRequest.getFromUserId().toString(),
                        "/queue/trade-errors",
                        "Error: datos del intercambio incompletos o inválidos.");
            }
            return;
        }

        if (tradeRequest.getFromUserId().equals(tradeRequest.getToUserId())) {
            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getFromUserId().toString(),
                    "/queue/trade-errors",
                    "Error: no puedes intercambiar contigo mismo.");
            return;
        }

        try {
            tradeService.executeMultipleTrade(
                    tradeRequest.getSessionUuid(),
                    tradeRequest.getFromUserId(),
                    tradeRequest.getToUserId(),
                    tradeRequest.getFromStickerIds(),
                    tradeRequest.getToStickerIds());

            String successMessage = "¡Intercambio exitoso de "
                    + tradeRequest.getFromStickerIds().size() + " láminas!";

            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getToUserId().toString(),
                    "/queue/trade-notifications",
                    successMessage);

            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getFromUserId().toString(),
                    "/queue/trade-notifications",
                    successMessage);

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    tradeRequest.getFromUserId().toString(),
                    "/queue/trade-errors",
                    "Error en el intercambio: " + e.getMessage());
        }
    }

    /**
     * Notifica a ambas contrapartes que el Usuario B se unió exitosamente a la
     * sala de intercambio creada por el Usuario A.
     * <p>
     * Valida que el payload contenga las claves {@code fromUserId} y
     * {@code toUserId} antes de retransmitir. Descarta silenciosamente el mensaje
     * si alguna clave está ausente o si ambos IDs son iguales (self-join), evitando
     * notificaciones fantasma o bucles de mensajería.
     * </p>
     *
     * @param payload Mapa con las claves {@code fromUserId} (Usuario B) y
     *                {@code toUserId} (Usuario A) como cadenas de texto.
     */
    @MessageMapping("/trade/join")
    public void notifyJoin(@Payload Map<String, Object> payload) {
        if (payload == null
                || !payload.containsKey("fromUserId")
                || !payload.containsKey("toUserId")) {
            return;
        }

        String fromUserId = payload.get("fromUserId").toString();
        String toUserId = payload.get("toUserId").toString();

        if (fromUserId.equals(toUserId)) return;

        System.out.println("[TRADE-JOIN] B=" + fromUserId + " notifica a A=" + toUserId);

        messagingTemplate.convertAndSendToUser(
                toUserId,
                "/queue/trade-notifications",
                "JOINED:" + fromUserId);

        messagingTemplate.convertAndSendToUser(
                fromUserId,
                "/queue/trade-notifications",
                "JOINED:" + toUserId);
    }

    /**
     * Sincroniza en tiempo real la oferta de láminas seleccionadas por un usuario
     * hacia su contraparte activa en la mesa de negociación.
     * <p>
     * Retransmite el payload de oferta al canal privado del receptor con el prefijo
     * {@code SYNC_OFFER:}, permitiendo que el frontend actualice la vista de la
     * contraparte de forma reactiva sin necesidad de polling. Descarta el mensaje
     * si faltan las claves requeridas en el payload.
     * </p>
     *
     * @param payload Mapa con las claves {@code toUserId} (receptor) y
     *                {@code offer} (estructura JSON serializada de la oferta).
     */
    @MessageMapping("/trade/sync-offer")
    public void syncOffer(@Payload Map<String, Object> payload) {
        if (payload == null
                || !payload.containsKey("toUserId")
                || !payload.containsKey("offer")) {
            return;
        }

        String toUserId = payload.get("toUserId").toString();
        String offerStr = payload.get("offer").toString();

        messagingTemplate.convertAndSendToUser(
                toUserId,
                "/queue/trade-notifications",
                "SYNC_OFFER:" + offerStr);
    }

    /**
     * Señaliza a la contraparte que el usuario emisor ha confirmado y está listo
     * para ejecutar el intercambio final.
     * <p>
     * Envía la señal {@code READY:TRUE} al canal privado del receptor. Cuando
     * ambas partes hayan enviado esta señal, el frontend habilita el botón de
     * confirmación final y dispara el endpoint {@code /app/trade/propose}.
     * Descarta silenciosamente el mensaje si la clave {@code toUserId} no está
     * presente en el payload.
     * </p>
     *
     * @param payload Mapa con la clave {@code toUserId} indicando el destinatario
     *                de la señal de confirmación.
     */
    @MessageMapping("/trade/ready")
    public void tradeReady(@Payload Map<String, Object> payload) {
        if (payload == null || !payload.containsKey("toUserId")) {
            return;
        }

        String toUserId = payload.get("toUserId").toString();

        messagingTemplate.convertAndSendToUser(
                toUserId,
                "/queue/trade-notifications",
                "READY:TRUE");
    }
}