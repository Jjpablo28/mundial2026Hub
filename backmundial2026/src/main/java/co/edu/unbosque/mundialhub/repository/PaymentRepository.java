package co.edu.unbosque.mundialhub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.Payments;
import co.edu.unbosque.mundialhub.model.User;

/**
 * Repositorio de persistencia encargado de la gestión de datos para la entidad
 * {@link Payments}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar capacidades completas de
 * operaciones CRUD (Crear, Leer, Actualizar, Borrar) y permite la definición de
 * métodos de consulta personalizados basados en nombres de propiedades para
 * interactuar con la tabla de transacciones financieras.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see JpaRepository
 * @see Payments
 */
public interface PaymentRepository extends JpaRepository<Payments, Long> {

	/**
	 * Busca un registro de pago específico utilizando el identificador de
	 * preferencia generado por la pasarela de Mercado Pago. * @param mpPreferenceId
	 * Firma única de preferencia de pago.
	 * 
	 * @return Un objeto {@link Optional} que contiene la entidad si fue encontrada,
	 *         o vacío en caso contrario.
	 */
	Optional<Payments> findByMpPreferenceId(String mpPreferenceId);

	/**
	 * Busca un registro de pago específico utilizando el ID definitivo de la
	 * transacción confirmado por Mercado Pago. * @param mpPaymentId Identificador
	 * único del pago procesado.
	 * 
	 * @return Un objeto {@link Optional} con la entidad de pago si existe, o vacío.
	 */
	Optional<Payments> findByMpPaymentId(Long mpPaymentId);

	/**
	 * Verifica la existencia de al menos un registro de pago asociado a un usuario
	 * específico. * @param user Entidad del usuario {@link User} a consultar.
	 * 
	 * @return {@code true} si el usuario tiene al menos un pago registrado;
	 *         {@code false} si no posee transacciones.
	 */
	boolean existsByUserId(User user);
}