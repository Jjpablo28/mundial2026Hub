package co.edu.unbosque.mundialhub.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import co.edu.unbosque.mundialhub.security.JwtUtil;
import co.edu.unbosque.mundialhub.util.AESUtil;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                    "http://localhost:8080",
                    "http://localhost:8081",
                    "http://localhost:8082",
                    "https://mundial2026hub.netlify.app"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // ✅ Si no es un CONNECT, dejamos pasar sin tocar nada
                    return message;
                }

                try {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        // ✅ FIX: Log claro en lugar de explotar silenciosamente
                        System.out.println("[WS-AUTH] ⚠️ Conexión sin token Authorization. Se permite sin autenticar.");
                        return message;
                    }

                    String token = authHeader.substring(7);

                    // ✅ FIX: Extraemos el username cifrado del JWT
                    String encryptedUsername = jwtUtil.extractUsername(token);

                    if (encryptedUsername == null) {
                        System.out.println("[WS-AUTH] ⚠️ No se pudo extraer username del token. Se permite sin autenticar.");
                        return message;
                    }

                    // ✅ FIX: Cargamos el UserDetails con el username cifrado (como está en BD)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(encryptedUsername);

                    if (!jwtUtil.validateToken(token, userDetails)) {
                        System.out.println("[WS-AUTH] ⚠️ Token inválido o expirado. Se permite sin autenticar.");
                        return message;
                    }

                    // ✅ FIX CRÍTICO: Intentamos descifrar el username para el Principal
                    // Si AESUtil.decrypt() falla, usamos el username cifrado como fallback
                    String principalName;
                    try {
                        principalName = AESUtil.decrypt(encryptedUsername);
                        System.out.println("[WS-AUTH] ✅ Usuario autenticado en WebSocket: " + principalName);
                    } catch (Exception aesEx) {
                        // Si el decrypt falla, usamos el username tal como viene del JWT
                        principalName = encryptedUsername;
                        System.out.println("[WS-AUTH] ⚠️ AESUtil.decrypt falló, usando username raw: " + principalName + " | Error: " + aesEx.getMessage());
                    }

                    // ✅ Extraemos el userId del claim del JWT para usarlo como nombre del Principal
                    // Esto es lo CLAVE: Spring usa el Principal.getName() para rutear los mensajes
                    // de convertAndSendToUser(userId, ...) — debe coincidir con lo que manda el frontend
                    Object userIdClaim = jwtUtil.extractAllClaims(token).get("id");
                    if (userIdClaim != null) {
                        String userIdStr = userIdClaim.toString();
                        System.out.println("[WS-AUTH] ✅ Principal seteado como userId numérico: " + userIdStr);

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userIdStr, null, userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        accessor.setUser(auth);
                    } else {
                        // Fallback: usar el nombre descifrado
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principalName, null, userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        accessor.setUser(auth);
                    }

                } catch (Exception e) {
                    // ✅ FIX PRINCIPAL: En lugar de propagar la excepción y cerrar la conexión,
                    // logueamos el error y dejamos pasar el mensaje sin autenticar.
                    // Antes, cualquier excepción aquí mataba el handshake silenciosamente.
                    System.err.println("[WS-AUTH] ❌ Error inesperado en interceptor WebSocket: " + e.getMessage());
                    e.printStackTrace();
                }

                return message;
            }
        });
    }
}