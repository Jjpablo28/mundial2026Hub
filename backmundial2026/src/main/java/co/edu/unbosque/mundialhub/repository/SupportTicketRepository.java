package co.edu.unbosque.mundialhub.repository;

import co.edu.unbosque.mundialhub.model.SupportTicket;
import co.edu.unbosque.mundialhub.model.SupportTicket.TicketStatus;
import co.edu.unbosque.mundialhub.model.SupportTicket.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

	/** Todos los tickets de un usuario destino */
	List<SupportTicket> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

	/** Todos los tickets gestionados por un agente */
	List<SupportTicket> findByAgentUserIdOrderByCreatedAtDesc(Long agentUserId);

	/** Filtro por estado */
	List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

	/** Filtro por tipo de acción */
	List<SupportTicket> findByTicketTypeOrderByCreatedAtDesc(TicketType ticketType);

	/** Paginación general para panel admin */
	Page<SupportTicket> findAllByOrderByCreatedAtDesc(Pageable pageable);

	/** Paginación filtrada por estado */
	Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

	/** Contador de tickets abiertos — útil para dashboards */
	long countByStatus(TicketStatus status);

	/** Busca tickets de un usuario destino con un estado específico */
	@Query("SELECT t FROM SupportTicket t WHERE t.targetUser.id = :userId AND t.status = :status ORDER BY t.createdAt DESC")
	List<SupportTicket> findByTargetUserIdAndStatus(@Param("userId") Long userId, @Param("status") TicketStatus status);
}