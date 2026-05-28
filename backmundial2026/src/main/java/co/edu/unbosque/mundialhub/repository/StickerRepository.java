package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.Rarity;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.Team;

/**
 * Repositorio de persistencia diseñado para la gestión de datos de la entidad
 * {@link Sticker}.
 * <p>
 * Extiende {@link JpaRepository} para proveer operaciones estándar de
 * persistencia y define métodos de búsqueda especializados que permiten filtrar
 * el catálogo de cromos según criterios de negocio, como la asociación a
 * selecciones nacionales (Team), niveles de exclusividad (Rarity) o identidad
 * del futbolista.
 * </p>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 * @see JpaRepository
 * @see Sticker
 */
public interface StickerRepository extends JpaRepository<Sticker, Long> {

	/**
	 * Recupera la lista de láminas asociadas a una selección nacional específica.
	 * 
	 * @param team Entidad {@link Team} que representa al equipo de fútbol.
	 * @return Lista de objetos {@link Sticker} vinculados al equipo.
	 */
	public List<Sticker> findByTeam(Team team);

	/**
	 * Recupera la lista de láminas pertenecientes a un equipo mediante su
	 * identificador numérico.
	 * 
	 * @param idTeam ID único del equipo {@link Team}.
	 * @return Lista de objetos {@link Sticker} encontrados bajo ese equipo.
	 */
	public List<Sticker> findByTeamId(Long idTeam);

	/**
	 * Elimina un registro de cromo del catálogo maestro basado en su ID único.
	 * 
	 * @param id Identificador numérico del sticker a remover.
	 */
	public void deleteById(Long id);

	/**
	 * Busca una lámina específica dentro del catálogo mediante su identificador
	 * único.
	 * 
	 * @param stickerId ID del cromo solicitado.
	 * @return Un objeto {@link Optional} que contiene el sticker si fue hallado, o
	 *         vacío en caso contrario.
	 */
	public Optional<Sticker> findById(Long stickerId);

	/**
	 * Filtra las láminas del catálogo basándose en su nivel de rareza.
	 * 
	 * @param rarity El grado de exclusividad {@link Rarity} a filtrar.
	 * @return Lista de stickers clasificados con la rareza especificada.
	 */
	public List<Sticker> findByRarity(Rarity rarity);

	/**
	 * Busca una lámina específica a través del nombre completo del futbolista.
	 * 
	 * @param playerName Nombre del jugador registrado en el sistema.
	 * @return Un objeto {@link Optional} con el sticker del jugador, si existe.
	 */
	public Optional<Sticker> findByPlayerName(String playerName);
}