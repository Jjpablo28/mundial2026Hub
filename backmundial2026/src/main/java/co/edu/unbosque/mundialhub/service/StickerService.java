package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.dto.StickerDTO;
import co.edu.unbosque.mundialhub.model.Rarity;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.Team;
import co.edu.unbosque.mundialhub.repository.StickerRepository;
import co.edu.unbosque.mundialhub.repository.TeamRepository;

@Service
public class StickerService implements CRUDOperation<StickerDTO, Sticker> {

	@Autowired
	private StickerRepository stickerRepo;

	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private TeamRepository teamRepo;

	@Override
	public StickerDTO create(StickerDTO data) {

		try {
			Sticker entity = modelMapper.map(data, Sticker.class);
			entity.setId(null);

			if (data.getPosition() == "Goalkeeper") {

			} else if (data.getPosition() == "Goalkeeper") {
				entity.setRarity(Rarity.RARE);
				entity.setCost(30);

			} else if (data.getPosition() == "Defender") {
				entity.setRarity(Rarity.COMMON);
				entity.setCost(10);

			} else if (data.getPosition() == "Midfielder") {
				entity.setRarity(Rarity.EPIC);
				entity.setCost(40);

			} else if (data.getPosition() == "Attacker") {
				entity.setRarity(Rarity.LEGENDARY);
				entity.setCost(50);
			}
			
			stickerRepo.save(entity);
			StickerDTO createdDTO=modelMapper.map(entity, StickerDTO.class);
			
			return createdDTO;

		} catch (Exception e) {
			throw new IllegalArgumentException("Error al crear el jugador: " + e.getMessage());

		}

	}

	@Override
	public List<StickerDTO> findAll() {
		List<Sticker>entityList =stickerRepo.findAll();
		List<StickerDTO>dtoList=new ArrayList<StickerDTO>();
		
		entityList.forEach(entity -> {
			StickerDTO dto = modelMapper.map(entity, StickerDTO.class);
			dtoList.add(dto);
		});
		
		return dtoList;
	}
	
	public List<StickerDTO> findByTeam(Long idTeam) {
	    if (idTeam == null) return new ArrayList<>();

	    List<Sticker> entityList = stickerRepo.findByTeamId(idTeam);

	    return entityList.stream()
	            .map(entity -> modelMapper.map(entity, StickerDTO.class))
	            .toList();
	}

	@Override
	public void deleteById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Player ID cannot be null");
		}
		Sticker teamDelete = stickerRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado para eliminar con ID: " + id));
		stickerRepo.delete(teamDelete);
	}

	public StickerDTO findById(Long id) {

		Sticker entity = stickerRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado con ID: " + id));

		StickerDTO dto = modelMapper.map(entity, StickerDTO.class);
	
		return dto;
	}
	
	@Override
	public StickerDTO updateById(Long id, StickerDTO newData) {
		if (id == null) {
			throw new IllegalArgumentException("ID cannot be null");
		}

		Sticker existing = stickerRepo.findById(id).orElseThrow(() -> new RuntimeException("Entity not found"));

		if (newData.getPlayerName() != null) {
			existing.setPlayerName(newData.getPlayerName());
		}

		if (newData.getRarity() != null) {
			existing.setRarity(newData.getRarity());
		}

		Team team = teamRepo.findById(newData.getTeamId())
		        .orElseThrow(() -> new RuntimeException("Team not found"));
		    existing.setTeam(team);

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

		StickerDTO updatedDTO = modelMapper.map(existing, StickerDTO.class);

		return updatedDTO;
	}

	@Override
	public long count() {
		return stickerRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return stickerRepo.existsById(id);
	}

}
