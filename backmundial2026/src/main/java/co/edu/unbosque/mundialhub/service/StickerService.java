package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.mundialhub.dto.StickerDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.Rarity;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.repository.StickerRepository;
import co.edu.unbosque.mundialhub.repository.TeamRepository;

/**
 * Servicio encargado de gestionar el inventario global de cromos (stickers) de los 
 * jugadores, definiendo sus costos, rarezas según posición y asociaciones por equipos.
 * * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
@Service
public class StickerService implements CRUDOperation<StickerDTO, Sticker> {

	@Autowired
	private StickerRepository stickerRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private TeamRepository teamRepo;

	/**
	 * Crea un nuevo cromo (Sticker) de jugador en el sistema.
	 * Asigna dinámicamente la rareza y el costo del cromo con base en la posición del jugador.
	 *
	 * @param data DTO con la información del jugador a crear.
	 * @return El DTO del cromo guardado de manera exitosa en el sistema.
	 * @throws Exceptions.ValidationException Si ocurre un error inesperado al procesar el mapeo o la persistencia.
	 */
	@Override
	@Transactional
	public StickerDTO create(StickerDTO data) {
		try {
			Sticker entity = modelMapper.map(data, Sticker.class);
			entity.setId(null);

			// Corrección de lógica: Se utiliza .equals() en lugar de == para Strings
			if ("Goalkeeper".equalsIgnoreCase(data.getPosition())) {
				entity.setRarity(Rarity.RARE);
				entity.setCost(30);
			} else if ("Defender".equalsIgnoreCase(data.getPosition())) {
				entity.setRarity(Rarity.COMMON);
				entity.setCost(10);
			} else if ("Midfielder".equalsIgnoreCase(data.getPosition())) {
				entity.setRarity(Rarity.EPIC);
				entity.setCost(40);
			} else if ("Attacker".equalsIgnoreCase(data.getPosition())) {
				entity.setRarity(Rarity.LEGENDARY);
				entity.setCost(50);
			}
			
			stickerRepo.save(entity);
			return modelMapper.map(entity, StickerDTO.class);

		} catch (Exception e) {
			throw new Exceptions.ValidationException("Error al crear el jugador: " + e.getMessage());
		}
	}

	/**
	 * Recupera todos los cromos registrados en el sistema.
	 *
	 * @return Lista completa de DTOs de los cromos.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<StickerDTO> findAll() {
		List<Sticker> entityList = stickerRepo.findAll();
		List<StickerDTO> dtoList = new ArrayList<>();
		
		entityList.forEach(entity -> dtoList.add(modelMapper.map(entity, StickerDTO.class)));
		return dtoList;
	}
	
	/**
	 * Recupera todos los cromos de jugadores que pertenecen a un equipo en específico.
	 *
	 * @param idTeam ID único del equipo a consultar.
	 * @return Lista de DTOs correspondientes al equipo; lista vacía si el ID provisto es nulo.
	 */
	@Transactional(readOnly = true)
	public List<StickerDTO> findByTeam(Long idTeam) {
	    if (idTeam == null) return new ArrayList<>();

	    List<Sticker> entityList = stickerRepo.findByTeamId(idTeam);

	    return entityList.stream()
	            .map(entity -> modelMapper.map(entity, StickerDTO.class))
	            .toList();
	}

	/**
	 * Elimina un cromo del sistema mediante su identificador único.
	 *
	 * @param id Identificador único del cromo a dar de baja.
	 * @throws Exceptions.ValidationException      Si el ID suministrado es nulo.
	 * @throws Exceptions.ResourceNotFoundException Si el jugador/cromo con el ID especificado no existe.
	 */
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null) {
			throw new Exceptions.ValidationException("El ID del jugador no puede ser nulo.");
		}
		Sticker teamDelete = stickerRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Jugador no encontrado para eliminar con ID: " + id));
		stickerRepo.delete(teamDelete);
	}

	/**
	 * Busca y obtiene un cromo específico del sistema a través de su ID.
	 *
	 * @param id Identificador del cromo.
	 * @return El DTO con la información del cromo localizado.
	 * @throws Exceptions.ResourceNotFoundException Si no se encuentra un jugador con el ID indicado.
	 */
	@Transactional(readOnly = true)
	public StickerDTO findById(Long id) {
		Sticker entity = stickerRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("Jugador no encontrado con ID: " + id));

		return modelMapper.map(entity, StickerDTO.class);
	}
	
	/**
	 * Actualiza los datos de un cromo existente en el sistema.
	 *
	 * @param id      Identificador único del cromo que se desea modificar.
	 * @param newData DTO cargado con los nuevos valores a actualizar.
	 * @return El DTO con los cambios persistidos.
	 * @throws Exceptions.ValidationException      Si el ID suministrado es nulo.
	 * @throws Exceptions.ResourceNotFoundException Si el cromo o el equipo (TeamId) asignado no se encuentran en la BD.
	 */
	@Override
	@Transactional
	public StickerDTO updateById(Long id, StickerDTO newData) {
		if (id == null) {
			throw new Exceptions.ValidationException("El ID para la actualización no puede ser nulo.");
		}

		Sticker existing = stickerRepo.findById(id)
				.orElseThrow(() -> new Exceptions.ResourceNotFoundException("No se encontró el jugador con ID: " + id));

		if (newData.getPlayerName() != null) {
			existing.setPlayerName(newData.getPlayerName());
		}

		if (newData.getRarity() != null) {
			existing.setRarity(newData.getRarity());
		}

		if (newData.getTeamId() != null) {
			Team team = teamRepo.findById(newData.getTeamId())
			        .orElseThrow(() -> new Exceptions.ResourceNotFoundException("El equipo con ID " + newData.getTeamId() + " no existe."));
			existing.setTeam(team);
		}

		if (newData.getPosition() != null) {
			existing.setPosition(newData.getPosition());
		}

		if (newData.getCost() != null) {
			existing.setCost(newData.getCost());
		}

		if (newData.getImageUrl() != null) {
			existing.setImageUrl(newData.getImageUrl());
		}

		existing.setUpdatedAt(LocalDateTime.now());
		stickerRepo.save(existing);

		return modelMapper.map(existing, StickerDTO.class);
	}

	/**
	 * Obtiene el conteo total de cromos registrados en la base de datos.
	 *
	 * @return Número total de cromos.
	 */
	@Override
	@Transactional(readOnly = true)
	public long count() {
		return stickerRepo.count();
	}

	/**
	 * Comprueba la existencia de un cromo mediante su identificador.
	 *
	 * @param id Identificador único del cromo.
	 * @return true si el cromo existe, de lo contrario false.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean exist(Long id) {
		return stickerRepo.existsById(id);
	}
}