package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.model.User;

/**
 * Repositorio de persistencia para la gestión de datos de la entidad
 * {@link Team}.
 * <p>
 * Extiende {@link JpaRepository} para habilitar operaciones CRUD y consultas
 * personalizadas sobre la tabla de selecciones nacionales, permitiendo
 * búsquedas por criterios de identidad, ordenamientos alfabéticos y gestión de
 * bajas en el catálogo.
 * </p>
 *
 * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 * @see JpaRepository
 * @see Team
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

	/**
	 * Busca una selección nacional por su nombre completo oficial.
	 * 
	 * @param name Nombre del equipo.
	 * @return Un objeto {@link Optional} con el equipo encontrado, si existe.
	 */
	public Optional<Team> findByName(String name);

	/**
	 * Busca una selección nacional por su sigla o acrónimo internacional (ej. COL).
	 * 
	 * @param shortName Código abreviado del equipo.
	 * @return Un objeto {@link Optional} con el equipo correspondiente.
	 */
	public Optional<Team> findByShortName(String shortName);

	/**
	 * Elimina una selección del registro basándose en su nombre oficial.
	 * 
	 * @param name Nombre del equipo a remover.
	 */
	public void deleteByName(String name);

	/**
	 * Elimina una selección del registro basándose en su identificador único.
	 * 
	 * @param id ID de base de datos del equipo.
	 */
	public void deleteById(Long id);

	/**
	 * Busca un usuario asociado a un grupo. <i>Nota: Se mantiene la firma original
	 * que retorna {@link User}.</i>
	 * 
	 * @param email Identificador del grupo.
	 * @return {@link Optional} con el usuario encontrado.
	 */
	public Optional<User> findByGroupName(String email);

	/**
	 * Busca un usuario mediante un identificador entero. <i>Nota: Se mantiene la
	 * firma original que retorna {@link User}.</i>
	 * 
	 * @param userId ID del usuario.
	 * @return {@link Optional} con el usuario encontrado.
	 */
	public Optional<User> findById(Integer userId);

	/**
	 * Recupera todos los equipos registrados ordenados alfabéticamente de forma
	 * ascendente.
	 * 
	 * @return Lista de equipos {@link Team} ordenados A-Z.
	 */
	List<Team> findAllByOrderByNameAsc();

	/**
	 * Recupera todos los equipos registrados ordenados alfabéticamente de forma
	 * descendente.
	 * 
	 * @return Lista de equipos {@link Team} ordenados Z-A.
	 */
	List<Team> findAllByOrderByNameDesc();

}