package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.mundialhub.dto.TeamDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.GroupName;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.repository.TeamRepository;

/**
 * Servicio encargado de gestionar las selecciones o equipos de fútbol del sistema VeltrixPass,
 * controlando la asignación de grupos de la Copa del Mundo y validando que no existan duplicados
 * en nombres o siglas oficiales.
 * * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
@Service
public class TeamService implements CRUDOperation<TeamDTO, Team> {

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private TeamRepository teamRepo;

	/**
	 * Registra un nuevo equipo (Team) en la base de datos.
	 * Asigna automáticamente el Enum correspondiente a su grupo y su marca temporal de creación.
	 *
	 * @param data DTO con la información del equipo que se desea guardar.
	 * @return El DTO mapeado con la información persistida e ID generado.
	 * @throws Exceptions.ValidationException Si el nombre del grupo no coincide con el formato del Enum
	 * o ante cualquier error imprevisto en la persistencia.
	 */
	@Override
	@Transactional
	public TeamDTO create(TeamDTO data) {
		try {
			Team entity = modelMapper.map(data, Team.class);
			entity.setId(null);
			
			if (data.getGroupName() != null) {
				String group = data.getGroupName().toString().trim().toUpperCase();
				try {
					// Optimización: Reemplaza los 12 bloques if-else mapeando directo al Enum por su valor de String
					entity.setGroupName(GroupName.valueOf(group));
				} catch (IllegalArgumentException e) {
					throw new Exceptions.ValidationException("El grupo especificado '" + group + "' no pertenece a un grupo válido.");
				}
			}

			entity.setCreatedAt(LocalDateTime.now());
			entity.setUpdatedAt(null);

			teamRepo.save(entity);
			return modelMapper.map(entity, TeamDTO.class);

		} catch (Exception e) {
			if (e instanceof Exceptions.ValidationException) {
				throw e;
			}
			throw new Exceptions.ValidationException("Error al crear el equipo: " + e.getMessage());
		}
	}

	/**
	 * Recupera todos los equipos del sistema sin un orden en particular.
	 *
	 * @return Lista completa de DTOs representativos de los equipos.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<TeamDTO> findAll() {
		List<Team> entityList = teamRepo.findAll();
		List<TeamDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> dtoList.add(modelMapper.map(entity, TeamDTO.class)));
		return dtoList;
	}

	/**
	 * Recupera todas las entidades de equipos ordenadas alfabéticamente por su nombre en orden ascendente.
	 *
	 * @return Lista de entidades de tipo {@link Team}.
	 */
	@Transactional(readOnly = true)
	public List<Team> findAllASC() {
	    return teamRepo.findAllByOrderByNameAsc();
	}

	/**
	 * Recupera todas las entidades de equipos ordenadas alfabéticamente por su nombre en orden descendente.
	 *
	 * @return Lista de entidades de tipo {@link Team}.
	 */
	@Transactional(readOnly = true)
	public List<Team> findAllDESC() {
		return teamRepo.findAllByOrderByNameDesc();
	}

	/**
	 * Busca y obtiene un equipo específico a través de su identificador único.
	 *
	 * @param id Identificador único del equipo.
	 * @return El DTO con los detalles del equipo localizado.
	 * @throws Exceptions.ResourceNotFoundException Si no se encuentra un equipo con el ID provisto.
	 */
	@Transactional(readOnly = true)
	public TeamDTO findById(Long id) {
		Team entity = teamRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Equipo no encontrado con ID: " + id));

		return modelMapper.map(entity, TeamDTO.class);
	}

	/**
	 * Elimina un equipo de la base de datos por su ID único.
	 *
	 * @param id Identificador único del equipo que se va a eliminar.
	 * @throws Exceptions.ValidationException       Si el ID provisto es nulo.
	 * @throws Exceptions.ResourceNotFoundException Si el equipo a eliminar no existe en el sistema.
	 */
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null) {
			throw new Exceptions.ValidationException("El ID del equipo no puede ser nulo.");
		}
		Team teamDelete = teamRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Equipo no encontrado para eliminar con ID: " + id));
		teamRepo.delete(teamDelete);
	}

	/**
	 * Actualiza los campos modificados de un equipo registrado.
	 * Evalúa que los datos nuevos no entren en conflicto de unicidad con otros equipos del sistema.
	 *
	 * @param id      Identificador único del equipo a mutar.
	 * @param newData DTO cargado con los campos actualizados.
	 * @return El DTO con la información ya persistida en base de datos.
	 * @throws Exceptions.ValidationException       Si el ID es nulo o si hay un conflicto con nombres/siglas de otro equipo.
	 * @throws Exceptions.ResourceNotFoundException Si el equipo que se desea actualizar no existe.
	 */
	@Override
	@Transactional
	public TeamDTO updateById(Long id, TeamDTO newData) {
		if (id == null) {
			throw new Exceptions.ValidationException("El ID del equipo no puede ser nulo.");
		}

		Team existing = teamRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Equipo no encontrado con ID: " + id));

		if (isAnyNewDataConflict(newData, existing)) {
			throw new Exceptions.ValidationException("Conflicto de información: El nombre o siglas ya están registrados en otro equipo.");
		}
		
		if (newData.getName() != null) {
			existing.setName(newData.getName());
		}

		if (newData.getShortName() != null) {
			existing.setShortName(newData.getShortName());
		}

		if (newData.getCountry() != null) {
			existing.setCountry(newData.getCountry());
		}

		if (newData.getCrest() != null) {
			existing.setCrest(newData.getCrest());
		}

		if (newData.getFifaRanking() != null) {
			existing.setFifaRanking(newData.getFifaRanking());
		}

		if (newData.getGroupName() != null) {
			existing.setGroupName(newData.getGroupName());
		}

		if (newData.getPoints() != null) {
			existing.setPoints(newData.getPoints());
		}

		existing.setUpdatedAt(LocalDateTime.now());
		teamRepo.save(existing);

		return modelMapper.map(existing, TeamDTO.class);
	}

	/**
	 * Valida si los datos de actualización del nombre o nombre corto (siglas) entran en conflicto
	 * con registros únicos que pertenezcan a otros equipos del sistema.
	 *
	 * @param newData      DTO con los datos que se proponen insertar.
	 * @param existingTeam Entidad del equipo actual antes de la edición.
	 * @return true si existe un conflicto de duplicados con otro registro, de lo contrario false.
	 */
	private boolean isAnyNewDataConflict(TeamDTO newData, Team existingTeam) {
		if (newData.getName() != null && !newData.getName().equalsIgnoreCase(existingTeam.getName())) {
			Optional<Team> team = teamRepo.findByName(newData.getName());
			if (team.isPresent() && !team.get().getId().equals(existingTeam.getId())) {
				return true;
			}
		}

		if (newData.getShortName() != null && !newData.getShortName().equalsIgnoreCase(existingTeam.getShortName())) {
			Optional<Team> team = teamRepo.findByShortName(newData.getShortName());
			if (team.isPresent() && !team.get().getId().equals(existingTeam.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Obtiene el conteo total de equipos registrados.
	 *
	 * @return Número de filas de equipos.
	 */
	@Override
	@Transactional(readOnly = true)
	public long count() {
		return teamRepo.count();
	}

	/**
	 * Comprueba la existencia de un equipo mediante su identificador.
	 *
	 * @param id Identificador único del equipo.
	 * @return true si el equipo existe, de lo contrario false.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean exist(Long id) {
		return teamRepo.existsById(id);
	}
}