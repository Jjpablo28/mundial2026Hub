package co.edu.unbosque.mundialhub.repository;

import co.edu.unbosque.mundialhub.model.TradeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para TradeSession.
 * Añade búsqueda por shortCode y por email del Usuario A (sesión más reciente).
 */
@Repository
public interface TradeSessionRepository extends JpaRepository<TradeSession, String> {

    /** Búsqueda por UUID (ya existía implícitamente via findById, pero explícita por claridad). */
    Optional<TradeSession> findByUuid(String uuid);

    /** Búsqueda por código alfanumérico corto (ej: "AB3X7Q"). */
    Optional<TradeSession> findByShortCode(String shortCode);

    /**
     * Busca la sesión PENDING más reciente asociada al email del Usuario A.
     * Usada cuando el Usuario B se une mediante el correo del anfitrión.
     */
    Optional<TradeSession> findTopByUserAEmailAndStatusOrderByCreatedAtDesc(String userAEmail, String status);
}