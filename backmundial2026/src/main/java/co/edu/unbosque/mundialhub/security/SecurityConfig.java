package co.edu.unbosque.mundialhub.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración central de seguridad HTTP para MundialHub.
 * <p>
 * Define la cadena de filtros JWT sin estado, las reglas de autorización
 * por rol para cada endpoint de la API REST y la política CORS global.
 * </p>
 *
 * <p>Roles reconocidos por el sistema:</p>
 * <ul>
 *   <li>{@code ROLE_ADMIN}   — Administrador con acceso total.</li>
 *   <li>{@code ROLE_SUPPORT} — Agente de soporte operativo.</li>
 *   <li>{@code ROLE_USER}    — Usuario final autenticado.</li>
 * </ul>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Construye la configuración de seguridad inyectando el filtro JWT.
     *
     * @param jwtAuthFilter      Filtro que valida el token Bearer en cada petición.
     * @param userDetailsService Servicio de carga de usuarios requerido por Spring Security.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     * <p>
     * Deshabilita CSRF (API stateless), aplica CORS, configura sesiones sin estado
     * y establece las reglas de acceso por rol para cada grupo de endpoints.
     * </p>
     *
     * @param http         Objeto de configuración HTTP de Spring Security.
     * @param authProvider Proveedor de autenticación basado en {@link UserDetailsService}.
     * @return La cadena de filtros construida y lista para ser registrada.
     * @throws Exception Si ocurre un error durante la construcción de la cadena.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authProvider)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> {

                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                auth.requestMatchers("/ws/**").permitAll();

                // /auth
                auth.requestMatchers(HttpMethod.POST, "/auth/register").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/auth/verify").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/auth/resend-code").permitAll();
                auth.requestMatchers(HttpMethod.PUT,  "/auth/resend").permitAll();
                auth.requestMatchers(HttpMethod.PUT,  "/auth/rememberPassword").permitAll();
                auth.requestMatchers(HttpMethod.GET,  "/auth/email").permitAll();
                auth.requestMatchers(HttpMethod.GET,  "/auth/teams").permitAll();
                auth.requestMatchers(HttpMethod.GET,  "/auth/matches").permitAll();

                // /teams
                auth.requestMatchers(HttpMethod.GET,    "/teams/**").permitAll();
                auth.requestMatchers(HttpMethod.POST,   "/teams/**").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.PUT,    "/teams/**").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/teams/**").hasAuthority("ROLE_ADMIN");

                // /sticker
                auth.requestMatchers(HttpMethod.GET,    "/sticker/**").permitAll();
                auth.requestMatchers(HttpMethod.POST,   "/sticker/**").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.PUT,    "/sticker/**").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/sticker/**").hasAuthority("ROLE_ADMIN");

                // /users
                auth.requestMatchers(HttpMethod.POST,   "/users/create").permitAll();
                auth.requestMatchers(HttpMethod.GET,    "/users/count").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.GET,    "/users").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.GET,    "/users/{id}").authenticated();
                auth.requestMatchers(HttpMethod.PUT,    "/users/{id}/role").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.PUT,    "/users/{id}/password").authenticated();
                auth.requestMatchers(HttpMethod.PUT,    "/users/{id}").authenticated();
                auth.requestMatchers(HttpMethod.DELETE, "/users/{id}").hasAuthority("ROLE_ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/users").hasAuthority("ROLE_ADMIN");

                // /user-stickers
                auth.requestMatchers("/user-stickers/**").authenticated();

                // /trade-session
                auth.requestMatchers("/trade-session/**").authenticated();

                // /support
                auth.requestMatchers(HttpMethod.GET, "/support/tickets/user/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT", "ROLE_USER");
                auth.requestMatchers(HttpMethod.GET, "/support/tickets/agent/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");
                auth.requestMatchers(HttpMethod.GET, "/support/stats")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");
                auth.requestMatchers(HttpMethod.GET, "/support/tickets/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");
                auth.requestMatchers(HttpMethod.GET, "/support/tickets")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");
                auth.requestMatchers(HttpMethod.POST, "/support/tickets")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");
                auth.requestMatchers(HttpMethod.PUT, "/support/tickets/*/resolve")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");
                auth.requestMatchers(HttpMethod.PUT, "/support/tickets/*/reject")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT");

                // /activity-logs
                auth.requestMatchers(HttpMethod.GET, "/activity-logs/user/*/paged")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT", "ROLE_USER");
                auth.requestMatchers(HttpMethod.GET, "/activity-logs/user/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPPORT", "ROLE_USER");
                auth.requestMatchers("/activity-logs/**")
                        .hasAuthority("ROLE_ADMIN");
                // /payments
                auth.requestMatchers("/payments/**").authenticated();

                auth.anyRequest().authenticated();
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura la política CORS global de la aplicación.
     * <p>
     * Permite todos los orígenes, métodos estándar y cabeceras en cada ruta,
     * exponiendo el header {@code Authorization} para que el frontend pueda leerlo.
     * </p>
     *
     * @return La fuente de configuración CORS registrada para todos los endpoints.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Construye el proveedor de autenticación basado en {@link UserDetailsService} y BCrypt.
     *
     * @param userDetailsService Servicio que carga los detalles del usuario desde la base de datos.
     * @return El proveedor de autenticación configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el {@link AuthenticationManager} como bean de Spring para ser inyectado
     * en los controladores o filtros que requieran autenticar credenciales manualmente.
     *
     * @param config Configuración de autenticación provista por Spring Security.
     * @return El administrador de autenticación activo.
     * @throws Exception Si no es posible obtener el administrador desde la configuración.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Registra el codificador de contraseñas BCrypt como bean global de la aplicación.
     *
     * @return Una instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}