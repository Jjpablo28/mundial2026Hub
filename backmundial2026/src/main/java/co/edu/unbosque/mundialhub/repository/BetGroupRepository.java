package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.mundialhub.model.BetGroup;

public interface BetGroupRepository extends JpaRepository<BetGroup, Long> {

	// Busca todos los grupos creados por un usuario (owner)
	List<BetGroup> findByOwnerId(Long ownerId);
	
}