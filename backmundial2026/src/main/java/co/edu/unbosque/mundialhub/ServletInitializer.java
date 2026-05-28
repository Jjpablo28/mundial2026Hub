package co.edu.unbosque.mundialhub;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador de Servlet para el despliegue de la aplicación en servidores
 * externos.
 * <p>
 * Esta clase extiende de {@link SpringBootServletInitializer} y es fundamental
 * cuando el proyecto se configura para empaquetarse como un archivo WAR en
 * lugar de un JAR autoejecutable. Permite que el ciclo de vida de la aplicación
 * Spring Boot sea orquestado por un contenedor de servlets tradicional de Java
 * EE/Jakarta EE (como Apache Tomcat, WildFly o JBoss) al enlazar la aplicación
 * con el contexto del servidor web externo.
 * </p>
 * * @author Juan Pablo Cuervo Rodriguez
 * 
 * @version 1.0
 * @see SpringBootServletInitializer
 * @see Mundial2026HubApplication
 */
public class ServletInitializer extends SpringBootServletInitializer {

	/**
	 * Configura la aplicación especificando la clase principal de Spring Boot como
	 * la fuente de configuración primordial para el contenedor de servlets externo.
	 * <p>
	 * Este método intercepta la inicialización del servidor web e indica al motor
	 * que debe arrancar el contexto web utilizando los beans y configuraciones
	 * declaradas en {@link Mundial2026HubApplication}.
	 * </p>
	 *
	 * @param application Un constructor configurador dinámico de la aplicación
	 *                    {@link SpringApplicationBuilder}.
	 * @return El configurador {@link SpringApplicationBuilder} provisto con la
	 *         fuente de la aplicación enlazada.
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Mundial2026HubApplication.class);
	}

}