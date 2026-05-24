package co.edu.unbosque.mundialhub.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.User;

/**
 * Repositorio de persistencia para la gestión de datos de la entidad
 * {@link User}.
 * <p>
 * Extiende {@link JpaRepository} para habilitar operaciones CRUD avanzadas y
 * consultas especializadas sobre la tabla de usuarios. Facilita la recuperación
 * de perfiles mediante criterios únicos (username, email, nombre), la
 * administración de cuentas y procesos de mantenimiento basados en la
 * expiración de tokens de verificación.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see JpaRepository
 * @see User
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Busca un usuario por su nombre de usuario (alias único).
	 * 
	 * @param userName El alias del usuario.
	 * @return Un objeto {@link Optional} con el usuario encontrado, si existe.
	 */
	public Optional<User> findByUserName(String userName);

	/**
	 * Elimina un usuario del registro permanente basándose en su nombre de usuario.
	 * 
	 * @param userName El alias del usuario a remover.
	 */
	public void deleteByUserName(String userName);

	/**
	 * Busca un usuario mediante su dirección de correo electrónico única.
	 * 
	 * @param email El email registrado.
	 * @return Un objeto {@link Optional} con el usuario correspondiente.
	 */
	public Optional<User> findByEmail(String email);

	/**
	 * Busca un usuario por su nombre completo registrado.
	 * 
	 * @param name Nombre y apellidos del usuario.
	 * @return Un objeto {@link Optional} con el usuario hallado.
	 */
	public Optional<User> findByName(String name);

	/**
	 * Busca un usuario utilizando un identificador entero.
	 * 
	 * @param userId ID del usuario.
	 * @return Un objeto {@link Optional} con el usuario encontrado.
	 */
	public Optional<User> findById(Integer userId);

	/**
	 * Recupera una lista de usuarios cuyo tiempo de expiración del código de
	 * verificación coincide con la fecha y hora proporcionada.
	 * 
	 * @param now Marca de tiempo {@link LocalDateTime} para la consulta.
	 * @return Lista de usuarios con tokens próximos a expirar o expirados.
	 */
	public List<User> findByVerificationExpires(LocalDateTime now);

}