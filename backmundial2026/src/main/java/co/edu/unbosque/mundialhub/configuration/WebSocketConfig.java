package co.edu.unbosque.mundialhub.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // ✅ Leer orígenes desde application.properties para flexibilidad por entorno
    @Value("${websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ FIX: setAllowedOriginPatterns en lugar de setAllowedOrigins
        // Esto permite que Render y otros proxies no bloqueen el handshake por CORS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }

                try {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        System.out.println("[WS-AUTH] ⚠️ Conexión sin token. Se permite sin autenticar.");
                        return message;
                    }

                    String token = authHeader.substring(7);
                    String encryptedUsername = jwtUtil.extractUsername(token);

                    if (encryptedUsername == null) {
                        System.out.println("[WS-AUTH] ⚠️ Username nulo en token. Se permite sin autenticar.");
                        return message;
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(encryptedUsername);

                    if (!jwtUtil.validateToken(token, userDetails)) {
                        System.out.println("[WS-AUTH] ⚠️ Token inválido o expirado. Se permite sin autenticar.");
                        return message;
                    }

                    // ✅ FIX CRÍTICO: El Principal debe ser el userId numérico (como String)
                    // porque el frontend llama convertAndSendToUser con el ID numérico.
                    // Si el Principal fuera el username, los mensajes nunca llegarían.
                    String principalName;
                    try {
                        // Intentamos descifrar el username (puede fallar si AES falla)
                        String cleanUsername = AESUtil.decrypt(encryptedUsername);
                        System.out.println("[WS-AUTH] ✅ Username descifrado: " + cleanUsername);
                        principalName = cleanUsername;
                    } catch (Exception aesEx) {
                        principalName = encryptedUsername;
                        System.out.println("[WS-AUTH] ⚠️ AES decrypt falló, usando raw: " + principalName);
                    }

                    // ✅ Extraemos el userId del claim "id" del JWT para usarlo como Principal
                    // Spring usa Principal.getName() para rutear /user/{id}/queue/...
                    try {
                        Object userIdClaim = jwtUtil.extractUserId(token);
                        if (userIdClaim != null) {
                            principalName = userIdClaim.toString();
                            System.out.println("[WS-AUTH] ✅ Principal seteado como userId: " + principalName);
                        }
                    } catch (Exception ex) {
                        System.out.println("[WS-AUTH] ⚠️ No se pudo extraer userId del token, usando username: " + principalName);
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principalName, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    accessor.setUser(auth);

                } catch (Exception e) {
                    // ✅ FIX: Nunca propagar la excepción — si falla, dejamos pasar sin autenticar
                    // Antes, cualquier excepción aquí cerraba la conexión silenciosamente
                    System.err.println("[WS-AUTH] ❌ Error inesperado en interceptor: " + e.getMessage());
                    e.printStackTrace();
                }

                return message;
            }
        });
    }
}