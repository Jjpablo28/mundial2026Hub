package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.dto.UserStickerDTO;
import co.edu.unbosque.mundialhub.model.Sticker;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.UserSticker;
import co.edu.unbosque.mundialhub.repository.StickerRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.repository.UserStickerRepository;

@Service
public class UserStickerService implements CRUDOperation<UserStickerDTO, UserSticker> {

	@Autowired
	private StickerRepository stickerRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserStickerRepository userStickerRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public UserStickerDTO create(UserStickerDTO dto) {
		UserSticker entity = modelMapper.map(dto, UserSticker.class);
		entity.setId(null);
		entity.setObtainedAt(LocalDateTime.now());

		if (entity.getQuantity() == null)
			entity.setQuantity(1);

		userStickerRepo.save(entity);
		return modelMapper.map(entity, UserStickerDTO.class);
	}

	public List<UserStickerDTO> findByUser(Long userId) {
		return userStickerRepo.findByUserId(userId).stream()
				.map(entity -> modelMapper.map(entity, UserStickerDTO.class)).collect(Collectors.toList());
	}

	public UserStickerDTO updateById(Long id, UserStickerDTO dto) {
		UserSticker existing = userStickerRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Relación Usuario-Sticker no encontrada"));

		if (dto.getQuantity() != null) {
			existing.setQuantity(dto.getQuantity());
		}

		userStickerRepo.save(existing);
		return modelMapper.map(existing, UserStickerDTO.class);
	}

	public void deleteById(Long id) {
		UserSticker entity = userStickerRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("No se encontró el registro para eliminar"));
		userStickerRepo.delete(entity);
	}

	@Override
	public List<UserStickerDTO> findAll() {
		return null;
	}

	@Override
	public long count() {
		return userStickerRepo.count();
	}

	@Override
	public boolean exist(Long id) {
		return userStickerRepo.existsById(id);
	}

	public List<UserStickerDTO> openPack(Long userId) {
		User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		List<Sticker> allStickers = stickerRepo.findAll();
		if (allStickers.isEmpty())
			throw new RuntimeException("No hay stickers configurados en el sistema");

		int userCoins = user.getTotalPoints();
		int totalPackets = user.getPackets();
		if (totalPackets != 0) {
			List<UserStickerDTO> packResult = new ArrayList<>();
			Random random = new Random();

			List<Sticker> selectedStickers = new ArrayList<>();

			while (selectedStickers.size() < 7) {
				int index;

				if (userCoins > 1000 && random.nextDouble() > 0.5) {
					index = random.nextInt(allStickers.size() / 2) + (allStickers.size() / 2);
				} else {
					index = random.nextInt(allStickers.size());
				}

				Sticker candidate = allStickers.get(index);

				if (!selectedStickers.contains(candidate)) {
					selectedStickers.add(candidate);
				}

				for (Sticker sticker : selectedStickers) {
					Optional<UserSticker> existing = userStickerRepo.findByUserIdAndStickerId(userId, sticker.getId());

					UserSticker entity;
					if (existing.isPresent()) {
						entity = existing.get();
						entity.setQuantity(entity.getQuantity() + 1);
					} else {
						entity = new UserSticker();
						entity.setUser(user);
						entity.setSticker(sticker);
						entity.setQuantity(1);
						entity.setObtainedAt(LocalDateTime.now());
					}

					userStickerRepo.save(entity);
					packResult.add(modelMapper.map(entity, UserStickerDTO.class));
				}

				return packResult;
			}
		} else {
			return null;
		}
		return null;

	}

	public void buyPack(Long userId) {
		User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		int totalPackets = user.getPackets();
		if (user.getTotalPoints() != 0) {
			user.setPackets(totalPackets + 1);
			user.setTotalPoints(user.getTotalPoints() - 500);
		} else {
			throw new RuntimeException("No hay monedas suficientes");
		}

	}
}