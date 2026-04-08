/**
 * Paquete que contiene las clases relacionadas con la seguridad de la
 * aplicación Virus Detected backend.
 */
package co.edu.unbosque.mundialhub.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Configuración de seguridad para la aplicación.
 * <p>
 * Esta clase configura la autenticación y autorización de las peticiones HTTP,
 * habilita CORS, define las reglas para diferentes endpoints y configura el uso
 * de JWT para la autenticación sin estado.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	/**
	 * Filtro de autenticación JWT que intercepta las peticiones para validar el
	 * token.
	 */
	private final JwtAuthenticationFilter jwtAuthFilter;
	/**
	 * Constructor con dependencias para {@link JwtAuthenticationFilter} y
	 * {@link UserDetailsService}. Spring injecta automáticamente estas
	 * dependencias.
	 *
	 * @param jwtAuthFilter      Filtro de autenticación JWT.
	 * @param userDetailsService Servicio para cargar detalles del usuario.
	 */
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	/**
	 * Configura la cadena de filtros de seguridad para las peticiones HTTP.
	 *
	 * @param http Objeto {@link HttpSecurity} para configurar la seguridad.
	 * @return El {@link SecurityFilterChain} construido.
	 * @throws Exception Si ocurre un error durante la configuración.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
	    http.csrf(csrf -> csrf.disable())
	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	        .authorizeHttpRequests(auth -> {
	            auth.requestMatchers("/auth/**").permitAll();
	            auth.requestMatchers("/teams/**").permitAll();
	            auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
	            auth.anyRequest().authenticated();
	        })
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        // AQUÍ EL CAMBIO: Usamos la variable 'authProvider' que viene por parámetro
	        .authenticationProvider(authProvider) 
	        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

	/**
	 * Configura la fuente de configuración para CORS (Cross-Origin Resource
	 * Sharing).
	 *
	 * @return Un {@link CorsConfigurationSource} que define los orígenes, métodos y
	 *         encabezados permitidos.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
				Arrays.asList("http://localhost:8080", "http://localhost:8081", "http://localhost:8082"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	/**
	 * Define el proveedor de autenticación que se utilizará. En este caso, se
	 * utiliza un {@link DaoAuthenticationProvider} que se basa en un
	 * {@link UserDetailsService} para cargar los usuarios y un
	 * {@link PasswordEncoder} para verificar las contraseñas.
	 *
	 * @return El {@link AuthenticationProvider} configurado.
	 */
	@Bean
	public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
	    // 1. Pasamos el parámetro inyectado por Spring directamente al constructor
	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService); 
	    
	    // 2. Asigna el encriptador de contraseñas (asumiendo que passwordEncoder() sí es un método en esta clase)
	    authProvider.setPasswordEncoder(passwordEncoder());
	    
	    return authProvider;
	}
	/**
	 * Obtiene el {@link AuthenticationManager} a partir de la configuración de
	 * autenticación.
	 *
	 * @param config La configuración de autenticación.
	 * @return El {@link AuthenticationManager}.
	 * @throws Exception Si ocurre un error al obtener el AuthenticationManager.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Define el codificador de contraseñas que se utilizará. Se recomienda usar
	 * {@link BCryptPasswordEncoder} para una codificación segura.
	 *
	 * @return El {@link PasswordEncoder} que se utilizará.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
