package co.edu.unbosque.mundialhub.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.mundialhub.dto.UserActivityLogDTO;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.model.UserActivityLog;
import co.edu.unbosque.mundialhub.repository.UserActivityLogRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;

@Service
public class ActivityLogService {

    @Autowired
    private UserActivityLogRepository logRepo;

    @Autowired
    private UserRepository userRepo;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String eventType, String detail) {
    	User user = userRepo.findById(userId)  // Long directo
    	        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId));

        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setEventType(eventType);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        logRepo.save(log);
    }

    // Lista completa sin paginar (útil para exports o reportes pequeños)
    @Transactional(readOnly = true)
    public List<UserActivityLogDTO> getLogsByUser(Long userId) {
        return logRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserActivityLogDTO> getAllLogs() {
        return logRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Con paginación — por usuario
    @Transactional(readOnly = true)
    public Page<UserActivityLogDTO> getLogsByUserPaged(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return logRepo.findByUserId(userId, pageable)
                .map(this::toDTO);
    }

    // Con paginación — todos (para panel admin)
    @Transactional(readOnly = true)
    public Page<UserActivityLogDTO> getAllLogsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return logRepo.findAll(pageable)
                .map(this::toDTO);
    }

    private UserActivityLogDTO toDTO(UserActivityLog entity) {
        UserActivityLogDTO dto = new UserActivityLogDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId().longValue());

        String rawName = entity.getUser().getName();
        if (rawName == null || rawName.isBlank()) {
            dto.setUserName("Usuario desconocido");
        } else {
            try {
                dto.setUserName(AESUtil.decrypt(rawName));
            } catch (Exception e) {
                dto.setUserName(rawName); // si no se puede descifrar, devuelve tal cual
            }
        }

        dto.setEventType(entity.getEventType());
        dto.setDetail(entity.getDetail());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}