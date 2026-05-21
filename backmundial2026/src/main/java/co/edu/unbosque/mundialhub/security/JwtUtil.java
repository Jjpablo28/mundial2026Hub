/**
 * Paquete que contiene las clases relacionadas con la seguridad de la
 * aplicación Virus Detected backend.
 */
package co.edu.unbosque.mundialhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para la generación, extracción y validación de tokens JWT (JSON Web
 * Tokens).
 * <p>
 * Esta clase proporciona métodos para crear tokens JWT para usuarios
 * autenticados, extraer información (como el nombre de usuario y la fecha de
 * expiración) de los tokens, y validar si un token es válido para un
 * {@link UserDetails} específico y si no ha expirado. Utiliza la librería JJWT
 * para la manipulación de tokens.
 */
@Component
public class JwtUtil {
	/**
	 * Tiempo de validez del token JWT en milisegundos (actualmente 1 hora).
	 */
	private static final long JWT_TOKEN_VALIDITY = 30 * 60 * 1000; // 1 hour
	/**
	 * Clave secreta para la firma de los tokens JWT. Se obtiene de la configuración
	 * de la aplicación. Si no se proporciona, se utiliza una clave por defecto (que
	 * debería ser reemplazada en un entorno de producción por una clave segura de
	 * al menos 32 caracteres).
	 */
	@Value("${jwt.secret:defaultSecretKeyWhichShouldBeAtLeast32CharactersLong}")
	private String secret;

	/**
	 * Obtiene la clave de firma para el token JWT a partir de la clave secreta.
	 * Utiliza el algoritmo HMAC-SHA para la creación de la clave.
	 *
	 * @return La clave de firma.
	 */
	private Key getSigningKey() {
		byte[] keyBytes = secret.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Extrae el nombre de usuario (el "subject" del token) de un token JWT.
	 *
	 * @param token El token JWT del cual se extraerá el nombre de usuario.
	 * @return El nombre de usuario contenido en el token.
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae la fecha de expiración de un token JWT.
	 *
	 * @param token El token JWT del cual se extraerá la fecha de expiración.
	 * @return La fecha de expiración del token.
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Extrae una reclamación específica de un token JWT utilizando un resolvedor de
	 * reclamaciones.
	 *
	 * @param <T>            El tipo de la reclamación a extraer.
	 * @param token          El token JWT del cual se extraerá la reclamación.
	 * @param claimsResolver Función para resolver la reclamación a partir del
	 *                       objeto {@link Claims}.
	 * @return La reclamación extraída del token.
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Extrae todas las reclamaciones de un token JWT.
	 *
	 * @param token El token JWT del cual se extraerán las reclamaciones.
	 * @return Un objeto {@link Claims} que contiene todas las reclamaciones del
	 *         token.
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}

	/**
	 * Verifica si un token JWT ha expirado.
	 *
	 * @param token El token JWT a verificar.
	 * @return {@code true} si el token ha expirado, {@code false} en caso
	 *         contrario.
	 */
	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Genera un token JWT para un usuario específico. Incluye las autoridades del
	 * usuario y el nombre de usuario desencriptado como reclamaciones adicionales.
	 *
	 * @param userDetails           Los detalles del usuario para el cual se
	 *                              generará el token.
	 * @param usernameDesencriptado El nombre de usuario desencriptado (puede ser
	 *                              diferente del nombre de usuario utilizado para
	 *                              la autenticación).
	 * @return El token JWT generado.
	 */
	public String generateToken(UserDetails userDetails, String usernameDesencriptado,Long userId, String userEmail) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("authorities", userDetails.getAuthorities());
		claims.put("user", usernameDesencriptado);
		claims.put("id", userId);
	    claims.put("email", userEmail);

		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Crea un token JWT con las reclamaciones proporcionadas y el sujeto
	 * (generalmente el nombre de usuario).
	 *
	 * @param claims  Las reclamaciones que se incluirán en el token.
	 * @param subject El sujeto del token (generalmente el nombre de usuario).
	 * @return El token JWT creado.
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	/**
	 * Valida un token JWT para un {@link UserDetails} específico. Verifica si el
	 * nombre de usuario extraído del token coincide con el nombre de usuario del
	 * {@code UserDetails} y si el token no ha expirado.
	 *
	 * @param token       El token JWT a validar.
	 * @param userDetails Los detalles del usuario contra los cuales se validará el
	 *                    token.
	 * @return {@code true} si el token es válido para el usuario y no ha expirado,
	 *         {@code false} en caso contrario.
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}