package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.mundialhub.dto.UserStickerDTO;
import co.edu.unbosque.mundialhub.model.UserSticker;
import co.edu.unbosque.mundialhub.repository.UserStickerRepository;

@Service
public class UserStickerService {

    @Autowired
    private UserStickerRepository userStickerRepo;

    @Autowired
    private ModelMapper modelMapper;

    public UserStickerDTO create(UserStickerDTO dto) {
        UserSticker entity = modelMapper.map(dto, UserSticker.class);
        entity.setObtainedAt(LocalDateTime.now());
        
        if (entity.getQuantity() == null) entity.setQuantity(1);
        
        userStickerRepo.save(entity);
        return modelMapper.map(entity, UserStickerDTO.class);
    }

    public List<UserStickerDTO> findByUser(Long userId) {
        return userStickerRepo.findByUserId(userId).stream()
                .map(entity -> modelMapper.map(entity, UserStickerDTO.class))
                .collect(Collectors.toList());
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
}