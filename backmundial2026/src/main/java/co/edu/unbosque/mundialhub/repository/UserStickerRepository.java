package co.edu.unbosque.mundialhub.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.dto.UserStickerDTO;
import co.edu.unbosque.mundialhub.model.UserSticker;

/**
 * Repositorio de persistencia diseñado para la gestión de las relaciones de
 * posesión entre usuarios y láminas (stickers).
 * <p>
 * Extiende {@link JpaRepository} para permitir operaciones CRUD sobre la tabla
 * intermedia que registra qué cromos posee cada usuario. Provee consultas
 * especializadas para recuperar el inventario completo de un coleccionista y
 * validar la existencia previa de un cromo específico para facilitar la
 * actualización de cantidades (incrementar o decrementar stock).
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see JpaRepository
 * @see UserSticker
 */
public interface UserStickerRepository extends JpaRepository<UserSticker, Long> {

	/**
	 * Recupera la lista completa de láminas (registros de posesión) vinculadas a un
	 * usuario específico. * @param userId Identificador único del usuario
	 * propietario.
	 * 
	 * @return Lista de objetos {@link UserSticker} asociados al usuario.
	 */
	List<UserSticker> findByUserId(Long userId);

	/**
	 * Busca un registro de posesión específico de un usuario para una lámina
	 * concreta.
	 * <p>
	 * Este método es fundamental para la lógica de negocio que permite actualizar
	 * la cantidad de cromos repetidos en lugar de generar duplicados en la base de
	 * datos.
	 * </p>
	 * * @param userId Identificador único del usuario.
	 * 
	 * @param stickerId Identificador único del sticker.
	 * @return Un objeto {@link Optional} que contiene la relación si ya existe, o
	 *         vacío si el usuario no posee dicha lámina.
	 */
	Optional<UserSticker> findByUserIdAndStickerId(Long userId, Long stickerId);

	Collection<UserStickerDTO> findByUserIdAndStickerTeamId(Long userId, Long teamId);
}