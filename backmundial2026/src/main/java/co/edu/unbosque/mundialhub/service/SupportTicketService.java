package co.edu.unbosque.mundialhub.service;

import co.edu.unbosque.mundialhub.dto.SupportTicketDTO;
import co.edu.unbosque.mundialhub.exception.Exceptions;
import co.edu.unbosque.mundialhub.model.SupportTicket;
import co.edu.unbosque.mundialhub.model.SupportTicket.TicketStatus;
import co.edu.unbosque.mundialhub.model.SupportTicket.TicketType;
import co.edu.unbosque.mundialhub.model.User;
import co.edu.unbosque.mundialhub.repository.SupportTicketRepository;
import co.edu.unbosque.mundialhub.repository.UserRepository;
import co.edu.unbosque.mundialhub.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Servicio central de soporte. Gestiona el ciclo de vida de los tickets y aplica
 * automáticamente las acciones de negocio al momento de resolverlos:
 * <ul>
 *   <li>ADD_PACKETS   → Suma sobres al usuario objetivo</li>
 *   <li>ADD_TOKENS    → Acredita tokens/puntos</li>
 *   <li>REFUND_TOKENS → Devuelve tokens por reembolso</li>
 *   <li>UNLOCK_ACCOUNT  → Habilita la cuenta del usuario</li>
 *   <li>REVOKE_ACCOUNT  → Deshabilita la cuenta del usuario</li>
 *   <li>RESET_VERIFICATION → Limpia el estado de verificación para que el usuario pueda re-verificar</li>
 *   <li>CUSTOM          → Solo registra la nota sin acción automática</li>
 * </ul>
 *
 * @author Juan Pablo Cuervo Rodriguez
 * @version 1.0
 */
@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository ticketRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ActivityLogService activityLogService;
    
	@Autowired
	private EmailService emailService;


    /**
     * Crea un nuevo ticket de soporte en estado OPEN.
     * El agente es identificado por su ID; el usuario destino por targetUserId.
     *
     * @param dto     DTO con los datos del ticket.
     * @param agentId ID del agente autenticado (extraído del JWT en el controlador).
     * @return El DTO del ticket guardado.
     */
    @Transactional
    public SupportTicketDTO createTicket(SupportTicketDTO dto, Long agentId) {
        // Validaciones mínimas
        if (dto.getTargetUserId() == null) {
            throw new Exceptions.ValidationException("El ID del usuario objetivo es obligatorio.");
        }
        if (dto.getTicketType() == null) {
            throw new Exceptions.ValidationException("El tipo de ticket es obligatorio.");
        }

        // Tipos que requieren cantidad
        if ((dto.getTicketType() == TicketType.ADD_PACKETS
                || dto.getTicketType() == TicketType.ADD_TOKENS
                || dto.getTicketType() == TicketType.REFUND_TOKENS)
                && (dto.getQuantity() == null || dto.getQuantity() <= 0)) {
            throw new Exceptions.ValidationException("Debe especificar una cantidad positiva para este tipo de acción.");
        }

        User targetUser = userRepo.findById(dto.getTargetUserId().intValue())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                        "Usuario destino no encontrado con ID: " + dto.getTargetUserId()));

        User agentUser = userRepo.findById(agentId.intValue())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                        "Agente no encontrado con ID: " + agentId));

        SupportTicket ticket = new SupportTicket();
        ticket.setTargetUser(targetUser);
        ticket.setAgentUser(agentUser);
        ticket.setTicketType(dto.getTicketType());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setQuantity(dto.getQuantity());
        ticket.setNotes(dto.getNotes());

        ticketRepo.save(ticket);

        activityLogService.log(agentId, "SUPPORT_TICKET_CREATED",
        	    "Ticket #" + ticket.getId() + " creado. Tipo: " + dto.getTicketType()
        	    + " para usuario ID " + safeDecrypt(targetUser.getUserName()));

        return toDTO(ticket);
    }
    
	/**
	 * Carga el contenido html de una plantilla desde los recursos del sistema.
	 *
	 * @param path Ruta del recurso a cargar.
	 * @return El contenido textual de la plantilla.
	 * @throws IOException Si ocurre una falla en el flujo de entrada/salida.
	 */
	private String loadTemplate(String path) throws IOException {
		var inputStream = getClass().getResourceAsStream(path);
		if (inputStream == null) {
			throw new IOException("Plantilla no encontrada en ruta: " + path);
		}
		return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	}

    /**
     * Resuelve un ticket aplicando automáticamente la acción correspondiente al usuario destino.
     *
     * @param ticketId   ID del ticket a resolver.
     * @param resolution Texto de resolución escrito por el agente.
     * @param agentId    ID del agente que resuelve.
     * @return El DTO actualizado del ticket.
     */
	@Transactional
    public SupportTicketDTO resolveTicket(Long ticketId, String resolution, Long agentId) {
        SupportTicket ticket = findTicketOrThrow(ticketId);

        if (ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new Exceptions.ValidationException("Este ticket ya fue resuelto previamente.");
        }
        if (ticket.getStatus() == TicketStatus.REJECTED) {
            throw new Exceptions.ValidationException("Este ticket fue rechazado y no puede resolverse.");
        }

        User targetUser = ticket.getTargetUser();
        String userNameDecrypted = AESUtil.decrypt(targetUser.getUserName());
        String emailDecrypted    = AESUtil.decrypt(targetUser.getEmail());
        String resolvedAt        = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        switch (ticket.getTicketType()) {

            case ADD_PACKETS -> {
                int cantidad     = ticket.getQuantity() != null ? ticket.getQuantity() : 1;
                int sobresActuales = targetUser.getPackets() != null ? targetUser.getPackets() : 0;
                targetUser.setPackets(sobresActuales + cantidad);
                userRepo.save(targetUser);
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_ADD_PACKETS",
                        "Soporte acreditó " + cantidad + " sobre(s). Nuevo total: " + targetUser.getPackets());

                sendTicketResolvedEmail(emailDecrypted,
                        "¡Tus sobres han sido acreditados!",
                        "/templates/ticket-resolved-add-packets.html",
                        body -> body
                            .replace("{{USER_NAME}}", userNameDecrypted)
                            .replace("{{QUANTITY}}",  String.valueOf(cantidad))
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }

            case ADD_TOKENS -> {
                int tokens       = ticket.getQuantity() != null ? ticket.getQuantity() : 0;
                int tokensActuales = targetUser.getTotalPoints() != null ? targetUser.getTotalPoints() : 0;
                targetUser.setTotalPoints(tokensActuales + tokens);
                userRepo.save(targetUser);
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_ADD_TOKENS",
                        "Soporte acreditó " + tokens + " token(s). Nuevo saldo: " + targetUser.getTotalPoints());

                sendTicketResolvedEmail(emailDecrypted,
                        "¡Tus tokens han sido acreditados!",
                        "/templates/ticket-resolved-tokens.html",
                        body -> body
                            .replace("{{USER_NAME}}",   userNameDecrypted)
                            .replace("{{EMAIL_TITLE}}", "Tokens Acreditados")
                            .replace("{{QUANTITY}}",    String.valueOf(tokens))
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }

            case REFUND_TOKENS -> {
                int tokens       = ticket.getQuantity() != null ? ticket.getQuantity() : 0;
                int tokensActuales = targetUser.getTotalPoints() != null ? targetUser.getTotalPoints() : 0;
                targetUser.setTotalPoints(tokensActuales + tokens);
                userRepo.save(targetUser);
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_REFUND",
                        "Soporte procesó reembolso de " + tokens + " token(s). Saldo: " + targetUser.getTotalPoints());

                sendTicketResolvedEmail(emailDecrypted,
                        "Tu reembolso ha sido procesado",
                        "/templates/ticket-resolved-tokens.html",
                        body -> body
                            .replace("{{USER_NAME}}",   userNameDecrypted)
                            .replace("{{EMAIL_TITLE}}", "Reembolso Procesado")
                            .replace("{{QUANTITY}}",    String.valueOf(tokens))
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }

            case UNLOCK_ACCOUNT -> {
                targetUser.setEnabled(true);
                targetUser.setAccountNonLocked(true);
                targetUser.setFailedLoginAttempts(0);
                targetUser.setLockedAt(null);
                userRepo.save(targetUser);
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_UNLOCK",
                        "Soporte desbloqueó la cuenta del usuario.");

                sendTicketResolvedEmail(emailDecrypted,
                        "Tu cuenta ha sido reactivada",
                        "/templates/ticket-resolved-unlock.html",
                        body -> body
                            .replace("{{USER_NAME}}",   userNameDecrypted)
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }

            case REVOKE_ACCOUNT -> {
                targetUser.setEnabled(false);
                targetUser.setAccountNonLocked(false);
                userRepo.save(targetUser);
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_REVOKE",
                        "Soporte suspendió la cuenta del usuario.");

                sendTicketResolvedEmail(emailDecrypted,
                        "Tu cuenta ha sido suspendida",
                        "/templates/ticket-resolved-revoke.html",
                        body -> body
                            .replace("{{USER_NAME}}",   userNameDecrypted)
                            .replace("{{RESOLUTION}}",  resolution != null ? resolution : "Violación de términos de uso.")
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }

            case RESET_VERIFICATION -> {
                targetUser.setCode(null);
                targetUser.setVerificationExpires(null);
                targetUser.setEnabled(false);
                userRepo.save(targetUser);
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_RESET_VERIFICATION",
                        "Soporte reinició el estado de verificación de la cuenta.");

                sendTicketResolvedEmail(emailDecrypted,
                        "Tu verificación ha sido reiniciada",
                        "/templates/ticket-resolved-reset-verification.html",
                        body -> body
                            .replace("{{USER_NAME}}",   userNameDecrypted)
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }

            case CUSTOM -> {
                activityLogService.log(targetUser.getId().longValue(), "SUPPORT_CUSTOM",
                        "Acción manual de soporte aplicada: " + resolution);

                sendTicketResolvedEmail(emailDecrypted,
                        "Tu solicitud de soporte ha sido resuelta",
                        "/templates/ticket-resolved-custom.html",
                        body -> body
                            .replace("{{USER_NAME}}",   userNameDecrypted)
                            .replace("{{RESOLUTION}}",  resolution != null ? resolution : "Sin detalles adicionales.")
                            .replace("{{RESOLVED_AT}}", resolvedAt));
            }
        }

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolution(resolution);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepo.save(ticket);

        activityLogService.log(agentId, "SUPPORT_TICKET_RESOLVED",
                "Ticket #" + ticketId + " resuelto por agente ID " + agentId);

        return toDTO(ticket);
    }

    /**
     * Carga una plantilla HTML, aplica los reemplazos mediante el mapper recibido
     * y envía el correo. Los errores de correo se silencian para no interrumpir
     * la transacción principal.
     *
     * @param to       Correo destino en texto plano (ya desencriptado).
     * @param subject  Asunto del correo.
     * @param template Ruta del recurso HTML (e.g. "/templates/ticket-resolved-unlock.html").
     * @param mapper   Función que recibe el HTML crudo y devuelve el HTML con placeholders reemplazados.
     */
    private void sendTicketResolvedEmail(String to, String subject, String template,
                                         UnaryOperator<String> mapper) {
        try {
            String html  = loadTemplate(template);
            String body  = mapper.apply(html);
            emailService.sendEmailDefault(to, subject, body);
        } catch (Exception ignored) {
            // El correo no debe interrumpir la resolución del ticket
        }
    }


    /**
     * Rechaza un ticket sin aplicar ninguna acción de negocio.
     *
     * @param ticketId   ID del ticket a rechazar.
     * @param resolution Motivo del rechazo.
     * @param agentId    ID del agente que rechaza.
     * @return El DTO actualizado.
     */
    @Transactional
    public SupportTicketDTO rejectTicket(Long ticketId, String resolution, Long agentId) {
        SupportTicket ticket = findTicketOrThrow(ticketId);

        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.REJECTED) {
            throw new Exceptions.ValidationException("El ticket ya fue procesado anteriormente.");
        }

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setResolution(resolution);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepo.save(ticket);

        activityLogService.log(agentId, "SUPPORT_TICKET_REJECTED",
                "Ticket #" + ticketId + " rechazado. Motivo: " + resolution);

        return toDTO(ticket);
    }



    @Transactional(readOnly = true)
    public List<SupportTicketDTO> findAll() {
        return ticketRepo.findAllByOrderByCreatedAtDesc(Pageable.unpaged())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDTO> findAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepo.findAllByOrderByCreatedAtDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDTO> findByStatusPaged(TicketStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepo.findByStatusOrderByCreatedAtDesc(status, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDTO> findByTargetUser(Long userId) {
        return ticketRepo.findByTargetUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDTO> findByAgent(Long agentId) {
        return ticketRepo.findByAgentUserIdOrderByCreatedAtDesc(agentId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupportTicketDTO findById(Long id) {
        return toDTO(findTicketOrThrow(id));
    }

    @Transactional(readOnly = true)
    public long countByStatus(TicketStatus status) {
        return ticketRepo.countByStatus(status);
    }



    private SupportTicket findTicketOrThrow(Long id) {
        return ticketRepo.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Ticket no encontrado con ID: " + id));
    }

    /**
     * Mapea la entidad al DTO, desencriptando los nombres de usuario para la UI.
     */
    private SupportTicketDTO toDTO(SupportTicket entity) {
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.setId(entity.getId());
        dto.setTicketType(entity.getTicketType());
        dto.setStatus(entity.getStatus());
        dto.setQuantity(entity.getQuantity());
        dto.setNotes(entity.getNotes());
        dto.setResolution(entity.getResolution());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getTargetUser() != null) {
            dto.setTargetUserId(entity.getTargetUser().getId().longValue());
            dto.setTargetUserName(safeDecrypt(entity.getTargetUser().getUserName()));
        }
        if (entity.getAgentUser() != null) {
            dto.setAgentUserId(entity.getAgentUser().getId().longValue());
            dto.setAgentUserName(safeDecrypt(entity.getAgentUser().getUserName()));
        }
        return dto;
    }

    private String safeDecrypt(String value) {
        if (value == null || value.isBlank()) return "Desconocido";
        try {
            return AESUtil.decrypt(value);
        } catch (Exception e) {
            return value;
        }
    }
}