package co.edu.unbosque.mundialhub.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;

import jakarta.annotation.PostConstruct;

/**
 * Clase de configuración encargada de inicializar y parametrizar de manera
 * global el SDK oficial de Mercado Pago dentro del ecosistema de la aplicación.
 * <p>
 * Este componente permite establecer la pasarela de pagos seguros para la
 * compra de sobres y elementos premium en la plataforma, abstrayendo las
 * credenciales secretas a través de las propiedades de entorno inyectadas por
 * el framework de Spring.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see Configuration
 * @see MercadoPagoConfig
 */
@Configuration
public class MercadoPagoSdkConfig {

	/**
	 * * Token de acceso privado y exclusivo (Access Token) de la cuenta de Mercado
	 * Pago.
	 * <p>
	 * Este valor se inyecta dinámicamente desde el archivo de propiedades del
	 * entorno (normalmente {@code application.properties} o
	 * {@code application.yml}) garantizando que las credenciales de producción o
	 * sandbox permanezcan desacopladas del código fuente.
	 * </p>
	 */
	@Value("${mercadopago.access-token}")
	private String accessToken;

	/**
	 * Método de ciclo de vida que inicializa la configuración estática de Mercado
	 * Pago.
	 * <p>
	 * Al estar anotado con {@link PostConstruct}, Spring Boot garantiza que este
	 * método se invocará una única vez inmediatamente después de que el bean haya
	 * sido instanciado y su dependencia {@code accessToken} haya sido inyectada
	 * correctamente. Registra el token en {@link MercadoPagoConfig} para que todas
	 * las peticiones de transacciones financieras subsiguientes de la suite estén
	 * autorizadas de forma automática.
	 * </p>
	 */
	@PostConstruct
	public void init() {
		MercadoPagoConfig.setAccessToken(accessToken);
	}

}