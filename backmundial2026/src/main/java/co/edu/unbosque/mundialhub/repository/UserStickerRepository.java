package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.mundialhub.model.UserSticker;

public interface UserStickerRepository extends JpaRepository<UserSticker, Long> {
    
    // Buscar todas las láminas que pertenecen a un usuario específico
    List<UserSticker> findByUserId(Long userId);
    
    // Buscar si un usuario ya tiene una lámina específica (para actualizar cantidad en lugar de crear duplicado)
    Optional<UserSticker> findByUserIdAndStickerId(Long userId, Long stickerId);
}