/**
 * Paquete que contiene las clases de configuración para la aplicación backend Virus Detected.
 */
package co.edu.unbosque.mundialhub.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Clase de configuración para la generación de la documentación de la API
 * utilizando OpenAPI 3.
 * <p>
 * Esta clase define un bean {@code customOpenAPI} que personaliza la
 * configuración de OpenAPI, incluyendo información general de la API,
 * esquemas de seguridad y respuestas globales. Esto permite generar una
 * documentación interactiva y detallada de los endpoints de la API.
 */
@Configuration
public class OpenApiConfig {
	/**
	 * Define y configura el objeto {@link OpenAPI} para la documentación de la API.
	 * <p>
	 * Este bean personaliza la información general de la API (título, versión,
	 * descripción, contacto y licencia), define el esquema de seguridad JWT
	 * ("bearerAuth") y configura respuestas globales como "UnauthorizedError",
	 * "ForbiddenError" y "NotFoundError" para proporcionar mensajes de error
	 * estandarizados en la documentación.
	 *
	 * @return Un objeto {@link OpenAPI} configurado con la información de la API
	 * y las definiciones de seguridad y respuestas globales.
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		String mainDescription = "<h2>Mundial 2026 Hub API Guide</h2><p>This API provides services for the"
				+ " Mundial 2026 Hub platform. It allows the management of users, authentication,"
				+ " predictions, matches, digital album participation and other platform features"
				+ " related to the FIFA World Cup 2026.</p><h3>Core concepts:</h3><ul>"
				+ " <li><strong>JWT (JSON Web Token)</strong>: A standard used to generate secure"
				+ " access tokens. After logging in, the API returns a token that must be included"
				+ " in the authorization header of subsequent requests.</li>"
				+ " <li><strong>Authentication</strong>: The process of verifying the identity of"
				+ " a user using credentials such as username and password.</li>"
				+ " <li><strong>Authorization</strong>: Determines what actions an authenticated"
				+ " user is allowed to perform based on their assigned role.</li>"
				+ "</ul><h3>Basic usage flow:</h3><ol>"
				+ " <li>Register a new user using <code>/auth/register</code></li>"
				+ " <li>Log in with <code>/auth/login</code> to obtain a JWT token</li>"
				+ " <li>Include the token in the request header:"
				+ " <code>Authorization: Bearer your_jwt_token</code></li>"
				+ " <li>Use the available endpoints to interact with the platform"
				+ " features such as matches, predictions, album and user data</li>"
				+ "</ol><h3>User roles:</h3><ul>"
				+ " <li><strong>USER</strong>: Standard user with access to platform features</li>"
				+ " <li><strong>ADMIN</strong>: Administrator with permissions to manage users"
				+ " and system resources</li></ul>"
				+ "<h3>Common HTTP status codes:</h3><ul>"
				+ " <li><strong>200/201</strong>: Successful operation</li>"
				+ " <li><strong>400</strong>: Bad request (invalid or missing data)</li>"
				+ " <li><strong>401</strong>: Unauthorized (invalid or expired token)</li>"
				+ " <li><strong>403</strong>: Forbidden (insufficient permissions)</li>"
				+ " <li><strong>404</strong>: Resource not found</li>"
				+ " <li><strong>409</strong>: Conflict (for example duplicated data)</li>"
				+ "</ul>";

		String securityDescription = "Authentication using JWT (JSON Web Token)."
				+ "<p>To authenticate requests, follow these steps:</p>" + "<ol>"
				+ "    <li>Obtain a JWT token using the endpoint <code>/auth/login</code></li>"
				+ "    <li>Copy the token returned in the response</li>"
				+ "    <li>Click the \"Authorize\" button at the top of this page</li>"
				+ "    <li>In the \"Value\" field write: <code>Bearer your_jwt_token</code></li>"
				+ "    <li>Click \"Authorize\" and then \"Close\"</li>" + "</ol>"
				+ "<p>After this, you will be able to access the protected endpoints.</p>";

		io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info()
				.title("Mundial 2026 Hub API").version("1.0").description(mainDescription)
				.contact(new io.swagger.v3.oas.models.info.Contact().name("Mundial 2026 Hub Development Team")
						.email("support@mundialhub.com")
						.url("https://github.com/your-organization/mundial2026-hub-backend"))
				.license(new io.swagger.v3.oas.models.info.License().name("MIT License")
						.url("https://opensource.org/licenses/MIT"));

		io.swagger.v3.oas.models.security.SecurityScheme securityScheme = new io.swagger.v3.oas.models.security.SecurityScheme()
				.type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
				.description(securityDescription);

		return new OpenAPI().info(info).components(new Components().addSecuritySchemes("bearerAuth", securityScheme)
				.addResponses("UnauthorizedError",
						new ApiResponse().description("Unauthorized - Invalid or expired JWT token")
								.content(new Content().addMediaType("application/json",
										new MediaType().addExamples("error",
												new Example().value("{\"error\": \"Unauthorized\", \"message\":"
														+ " \"Invalid or expired token\"}")))))
				.addResponses("ForbiddenError",
						new ApiResponse().description("Forbidden - You do not have sufficient permissions")
								.content(new Content().addMediaType("application/json",
										new MediaType().addExamples("error",
												new Example().value("{\"error\": \"Forbidden\", \"message\":"
														+ " \"You do not have permission to perform this"
														+ " operation\"}")))))
				.addResponses("NotFoundError",
						new ApiResponse().description("Resource not found")
								.content(new Content().addMediaType("application/json",
										new MediaType().addExamples("error",
												new Example().value("{\"error\": \"Not found\", \"message\":"
														+ " \"The requested resource does not exist\"}"))))));
	}
}