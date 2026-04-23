package co.edu.unbosque.mundialhub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.mundialhub.model.Bet;

public interface BetRepository extends JpaRepository<Bet, Long> {
	
	// Busca todas las apuestas hechas por un usuario específico (por su ID)
	List<Bet> findByUserId(Long userId);

	// Busca todas las apuestas que pertenecen a un grupo específico
	List<Bet> findByBetGroupId(Long betGroupId);
	
	// Busca todas las apuestas hechas para un partido específico
	List<Bet> findByMatchId(Long matchId);

}