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
        // ✅ Igual que el amigo: setAllowedOriginPatterns + withSockJS, sin nada más
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

                String authHeader = accessor.getFirstNativeHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String encryptedUsername = jwtUtil.extractUsername(token);

                    if (encryptedUsername != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(encryptedUsername);
                        if (jwtUtil.validateToken(token, userDetails)) {

                            // ✅ FIX CRÍTICO: El Principal debe ser el userId numérico (String)
                            // porque TradeSessionController hace convertAndSendToUser(userId.toString(), ...)
                            // Spring rutea el mensaje buscando Principal.getName() == userId
                            String principalName;
                            try {
                                Object userIdClaim = jwtUtil.extractUserId(token);
                                if (userIdClaim != null) {
                                    principalName = userIdClaim.toString();
                                } else {
                                    principalName = AESUtil.decrypt(encryptedUsername);
                                }
                            } catch (Exception e) {
                                principalName = AESUtil.decrypt(encryptedUsername);
                            }

                            System.out.println("[WS-AUTH] ✅ Principal: " + principalName);

                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    principalName, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(auth);
                            accessor.setUser(auth);
                        }
                    }
                }

                return message;
            }
        });
    }
}