package co.edu.unbosque.mundialhub.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.mundialhub.model.UserActivityLog;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    List<UserActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserActivityLog> findAllByOrderByCreatedAtDesc();
    Page<UserActivityLog> findByUserId(Long userId, Pageable pageable);
    Page<UserActivityLog> findAll(Pageable pageable);
}