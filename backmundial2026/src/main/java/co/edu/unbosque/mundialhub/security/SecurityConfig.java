package co.edu.unbosque.mundialhub.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

	private final JwtAuthenticationFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/auth/**").permitAll();
					auth.requestMatchers("/payment/**").permitAll();
					auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
					auth.requestMatchers("/user/showAllEncrypted").hasAnyRole("USER", "ADMIN");
					auth.requestMatchers("/user/updatePasswordByUserName").hasAnyRole("USER", "ADMIN","CHECKER");
					auth.requestMatchers("/user/updatePasswordById").hasAnyRole("USER", "ADMIN","CHECKER");
					auth.requestMatchers("/user/encryptUser").hasAnyRole("USER", "ADMIN");
					auth.requestMatchers("/user/decrypUser").hasAnyRole("USER", "ADMIN");
					auth.requestMatchers("/user/deleteById").hasAnyRole("USER", "ADMIN");
					auth.requestMatchers("/user/deletebyuser").hasAnyRole("USER", "ADMIN");
					auth.requestMatchers("/user/updateRol").hasAnyRole("ADMIN");
					auth.requestMatchers("/user/getbyuser").hasAnyRole("USER", "ADMIN");
					auth.requestMatchers("/event/showAllByAdminId").hasAnyRole("ADMIN");
					auth.requestMatchers("/event/deleteById").hasAnyRole("ADMIN");
					auth.requestMatchers("/event/create").hasRole("ADMIN");
					auth.requestMatchers("/event/showAll").permitAll();
					auth.requestMatchers("/email/enviarCorreo").hasAnyRole("TEMPORAL", "USER", "ADMIN", "CHECKER");
					auth.anyRequest().authenticated();
				}).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
