package co.edu.unbosque.mundialhub;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal y punto de entrada de la aplicación Mundial2026Hub.
 * <p>
 * Esta clase se encarga de arrancar todo el contexto de Spring Boot, activar la
 * autoconfiguración del framework, el escaneo de componentes y la configuración
 * de beans globales necesarios para la suite del sistema VeltrixPass.
 * </p>
 * * @author Juan Pablo Cuervo Rodriguez
 * 
 * @version 1.0
 */
@SpringBootApplication
public class Mundial2026HubApplication {

	/**
	 * Método de entrada principal (main) que arranca la ejecución de la aplicación.
	 * Delegando el control al ciclo de vida de Spring Boot mediante
	 * {@link SpringApplication#run}.
	 *
	 * @param args Argumentos de la línea de comandos pasados al iniciar la
	 *             aplicación.
	 */
	public static void main(String[] args) {
		SpringApplication.run(Mundial2026HubApplication.class, args);
	}

	/**
	 * Configura y registra un Bean global de {@link ModelMapper} en el contenedor
	 * de Spring.
	 * <p>
	 * Este utilitario es fundamental en las capas de servicios de la arquitectura
	 * de la aplicación, ya que automatiza el mapeo y transformación de datos
	 * bidireccional entre las Entidades de la base de datos (como User, Team,
	 * Sticker) y sus respectivas representaciones públicas (DTOs).
	 * </p>
	 *
	 * @return Una instancia nueva y lista para inyectar de {@link ModelMapper}.
	 */
	@Bean
	ModelMapper getModelMapper() {
		return new ModelMapper();
	}

}