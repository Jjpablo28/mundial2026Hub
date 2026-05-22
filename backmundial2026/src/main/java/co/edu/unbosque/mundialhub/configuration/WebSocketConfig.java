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

/**
 * Clase de configuración encargada de habilitar y parametrizar la
 * infraestructura de mensajería bidireccional en tiempo real utilizando
 * WebSockets con el protocolo STOMP.
 * <p>
 * Implementa {@link WebSocketMessageBrokerConfigurer} para definir los brokers
 * de mensajes, registrar los puntos de conexión (endpoints) protegidos para
 * clientes externos (ej. Angular), y establecer un interceptor de canal
 * entrante (Inbound Channel) que extrae, valida y procesa la seguridad JWT de
 * cada cliente de forma asíncrona.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see Configuration
 * @see EnableWebSocketMessageBroker
 * @see WebSocketMessageBrokerConfigurer
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Configura el bróker de mensajería interna y define las reglas de ruteo de
	 * destinos.
	 * <p>
	 * Configura un bróker simple basado en memoria que expone el prefijo
	 * {@code /queue} para la mensajería punto a punto (peticiones privadas). Define
	 * además el prefijo {@code /app} para filtrar los mensajes enviados por el
	 * cliente dirigidos a métodos controladores mapeados y establece {@code /user}
	 * como el prefijo exclusivo para los destinos de usuario específicos.
	 * </p>
	 *
	 * @param config Registro dinámico de configuración del Bróker de Mensajería.
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// NOTA DE PRODUCCIÓN: Solo debe ir "/queue".
		// Si se incluye "/user" en el enableSimpleBroker se rompe el ruteo de mensajes
		// privados nativo de Spring.
		config.enableSimpleBroker("/queue");
		config.setApplicationDestinationPrefixes("/app");
		config.setUserDestinationPrefix("/user");
	}

	/**
	 * Registra los endpoints STOMP HTTP a los que se conectarán los clientes del
	 * lado del cliente.
	 * <p>
	 * Habilita el endpoint base {@code /ws}, restringe los orígenes permitidos
	 * (CORS) a la URL de la interfaz web de la aplicación y activa el soporte de
	 * retrocompatibilidad mediante SockJS en caso de que el navegador web del
	 * cliente no soporte WebSockets nativos.
	 * </p>
	 *
	 * @param registry Registro de endpoints de conexión STOMP.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// Permitimos explícitamente los mismos orígenes incluyendo el string "null"
		registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:8080", "http://localhost:8081",
				"http://localhost:8082", "https://mundial2026hub.netlify.app" // 👈 Duplicamos el origen null
																						// aquí
		).withSockJS();
	}

	/**
	 * Configura el canal de recepción de mensajes entrantes del cliente instalando
	 * un interceptor personalizado enfocado en la seguridad criptográfica y
	 * autenticación JWT.
	 * <p>
	 * El interceptor actúa exclusivamente sobre los comandos de conexión inicial
	 * ({@link StompCommand#CONNECT}). Extrae el encabezado {@code Authorization},
	 * valida la firma del token web de JSON (JWT), desencripta simétricamente el
	 * nombre de usuario de la base de datos mediante {@link AESUtil#decrypt} y
	 * establece formalmente la sesión autenticada en el contexto de seguridad
	 * asíncrono de Spring para su uso en las colas de comunicación privadas.
	 * </p>
	 *
	 * @param registration Registro y configurador del canal entrante de peticiones
	 *                     de red del cliente.
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				// Intercepta únicamente en la fase de negociación/apretón de manos (Handshake
				// CONNECT)
				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					String authHeader = accessor.getFirstNativeHeader("Authorization");
					if (authHeader != null && authHeader.startsWith("Bearer ")) {
						String token = authHeader.substring(7);
						String encryptedUsername = jwtUtil.extractUsername(token);

						if (encryptedUsername != null) {
							UserDetails userDetails = userDetailsService.loadUserByUsername(encryptedUsername);
							if (jwtUtil.validateToken(token, userDetails)) {

								// Descifrado del nombre de usuario para almacenar un Principal limpio en la
								// sesión del socket
								String cleanUsername = AESUtil.decrypt(encryptedUsername);

								UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
										cleanUsername, null, userDetails.getAuthorities());

								// Registra al usuario en el contexto actual y lo acopla a la cabecera del
								// protocolo STOMP
								SecurityContextHolder.getContext().setAuthentication(auth);
								accessor.setUser(auth);
							}
						}
					}
				}
				return message;
			}
		});
	}
}