package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.dto.TeamDTO;
import co.edu.unbosque.mundialhub.dto.UserDTO;
import co.edu.unbosque.mundialhub.model.GroupName;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.repository.TeamRepository;

@Service
public class TeamService implements CRUDOperation<TeamDTO, Team> {

	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private TeamRepository teamRepo;

	@Override
	public TeamDTO create(TeamDTO data) {

		try {
			Team entity = modelMapper.map(data, Team.class);
			entity.setId(null);
			
			String group=data.getGroupName().toString();

			if (group.equals("A")) {
				entity.setGroupName(GroupName.A);
			} else if (group.equals("B")) {
				entity.setGroupName(GroupName.B);
			} else if (group.equals("C")) {
				entity.setGroupName(GroupName.C);
			} else if (group.equals("D")) {
				entity.setGroupName(GroupName.D);
			} else if (group.equals("E")) {
				entity.setGroupName(GroupName.E);
			} else if (group.equals("F")) {
				entity.setGroupName(GroupName.F);
			} else if (group.equals("G")) {
				entity.setGroupName(GroupName.G);
			} else if (group.equals("H")) {
				entity.setGroupName(GroupName.H);
			} else if (group.equals("I")) {
				entity.setGroupName(GroupName.I);
			} else if (group.equals("J")) {
				entity.setGroupName(GroupName.J);
			} else if (group.equals("K")) {
				entity.setGroupName(GroupName.K);
			} else if (group.equals("L")) {
				entity.setGroupName(GroupName.L);
			}

			entity.setCreatedAt(LocalDateTime.now());
			entity.setUpdatedAt(	null);

			teamRepo.save(entity);

			TeamDTO createdDto = modelMapper.map(entity, TeamDTO.class);

			return createdDto;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error al crear el equipo: " + e.getMessage());

		}
	}

	@Override
	public List<TeamDTO> findAll() {
		List<Team> entityList = teamRepo.findAll();
		List<TeamDTO> dtoList = new ArrayList<>();

		entityList.forEach(entity -> {
			TeamDTO dto = modelMapper.map(entity, TeamDTO.class);
			dtoList.add(dto);
		});
		return dtoList;
	}
	public List<Team> findAllASC() {
	    return teamRepo.findAllByOrderByNameAsc();
	}
	public List<Team> findAllDESC() {
		return teamRepo.findAllByOrderByNameDesc();
	}
	public UserDTO findById(Long id) {

		Team entity = teamRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado con ID: " + id));

		UserDTO dto = modelMapper.map(entity, UserDTO.class);
	
		return dto;
	}

	@Override
	public void deleteById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Team ID cannot be null");
		}
		Team teamDelete = teamRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado para eliminar con ID: " + id));
		teamRepo.delete(teamDelete);
	}

	@Override
	public TeamDTO updateById(Long id, TeamDTO newData) {
		if (id == null) {
			throw new IllegalArgumentException("Team ID cannot be null");
		}

		Team existing = teamRepo.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));

		if (isAnyNewDataConflict(newData, existing)) {
			throw new IllegalArgumentException("Conflict with existing data");
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

		TeamDTO updatedDTO = modelMapper.map(existing, TeamDTO.class);

		return updatedDTO;
	}

	private boolean isAnyNewDataConflict(TeamDTO newData, Team existingTeam) {

		if (newData.getName() != null && !newData.getName().equals(existingTeam.getName())) {
			Optional<Team> team = teamRepo.findByName(newData.getName());
			if (team.isPresent() && !team.get().getId().equals(existingTeam.getId())) {
				return true;
			}
		}

		if (newData.getShortName() != null && !newData.getShortName().equals(existingTeam.getShortName())) {
			Optional<Team> team = teamRepo.findByShortName(newData.getShortName());
			if (team.isPresent() && !team.get().getId().equals(existingTeam.getId())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long count() {

		return teamRepo.count();
	}

	@Override
	public boolean exist(Long id) {

		return teamRepo.existsById(id);
	}

}
