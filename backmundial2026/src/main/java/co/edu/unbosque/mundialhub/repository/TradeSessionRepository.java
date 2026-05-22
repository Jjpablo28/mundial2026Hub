package co.edu.unbosque.mundialhub.repository;

import co.edu.unbosque.mundialhub.model.TradeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TradeSessionRepository extends JpaRepository<TradeSession, String> {
    Optional<TradeSession> findByUuid(String uuid);
}