package co.edu.unbosque.mundialhub.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
/**
 * Filtro de autenticación JWT que se ejecuta una vez por cada petición.
 * <p>
 * Este filtro intercepta las peticiones entrantes, extrae el token JWT del
 * encabezado de autorización, valida el token y, si es válido, autentica al
 * usuario configurando el {@code SecurityContextHolder} con la información
 * del usuario autenticado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	/**
	 * Utilidad para la manipulación y validación de tokens JWT.
	 */
    private final JwtUtil jwtUtil;
    /**
	 * Servicio para cargar los detalles del usuario a partir del nombre de
	 * usuario (generalmente desde la base de datos).
	 */
    private final UserDetailsService userDetailsService;
    /**
	 * Constructor con dependencias para {@link JwtUtil} y
	 * {@link UserDetailsService}. Spring injecta automáticamente estas
	 * dependencias.
	 *
	 * @param jwtUtil          Utilidad para la gestión de JWT.
	 * @param userDetailsService Servicio para cargar los detalles del usuario.
	 */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    /**
	 * Método principal del filtro que se ejecuta para cada petición.
	 *
	 * @param request     La petición HTTP entrante.
	 * @param response    La respuesta HTTP saliente.
	 * @param filterChain Cadena de filtros para continuar el procesamiento de la
	 * petición.
	 * @throws ServletException Si ocurre un error a nivel de servlet.
	 * @throws IOException      Si ocurre un error de entrada/salida.
	 */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error extracting username from token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
