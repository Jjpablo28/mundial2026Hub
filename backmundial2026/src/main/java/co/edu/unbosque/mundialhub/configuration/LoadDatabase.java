package co.edu.unbosque.mundialhub.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración dedicada a orquestar la ejecución automática de tareas
 * durante la fase de arranque de la aplicación de Spring Boot.
 * <p>
 * Sirve como el puente que activa los procesos automáticos de siembra y
 * aprovisionamiento de datos del backend (Data Seeding) antes de que el
 * servidor web empiece a recibir solicitudes HTTP del cliente.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see Configuration
 * @see CommandLineRunner
 * @see DatabaseInitializer
 */
@Configuration
public class LoadDatabase {

	/**
	 * Declara y registra un Bean de tipo {@link CommandLineRunner} en el contenedor
	 * de Spring.
	 * <p>
	 * El callback funcional provisto por esta interfaz se ejecuta inmediatamente
	 * después de que el contexto de la aplicación se ha cargado por completo.
	 * Intercepta el inicio e invoca el método {@code runDataInitialization()} del
	 * inicializador inyectado para cargar los usuarios base, los 48 equipos
	 * mundiales y las colecciones completas de cromos/stickers.
	 * </p>
	 *
	 * @param initializer Componente de servicio {@link DatabaseInitializer}
	 *                    encargado de la lógica pesada de carga de datos,
	 *                    validación y consumo de APIs externas.
	 * @return Una función lambda compatible con la interfaz funcional
	 *         {@link CommandLineRunner}.
	 */
	@Bean
	CommandLineRunner initDatabase(DatabaseInitializer initializer) {
		return args -> {
			initializer.runDataInitialization();
		};
	}
}