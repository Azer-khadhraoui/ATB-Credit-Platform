package tn.atb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.atb.backend.dto.audit.AuditLogResponse;
import tn.atb.backend.entity.AuditLog;
import tn.atb.backend.entity.User;
import tn.atb.backend.entity.enums.AuditAction;
import tn.atb.backend.mapper.AuditLogMapper;
import tn.atb.backend.repository.AuditLogRepository;
import tn.atb.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * Logs an action performed by the currently authenticated user (matricule read from the security context).
     */
    public void log(AuditAction action, String entity, String entityId, String description) {
        log(currentMatriculeOrNull(), action, entity, entityId, description);
    }

    /**
     * Logs an action with an explicit actor matricule, for flows where the security context
     * isn't populated yet (e.g. the login request itself, or self-registration).
     */
    public void log(String actorMatricule, AuditAction action, String entity, String entityId, String description) {
        AuditLog logEntry = AuditLog.builder()
                .userId(actorMatricule)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(logEntry);
    }

    public List<AuditLogResponse> getAllLogs() {
        List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        Map<String, User> usersByMatricule = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getMatricule, Function.identity(), (a, b) -> a));

        return logs.stream()
                .map(logEntry -> auditLogMapper.toResponse(logEntry, usersByMatricule.get(logEntry.getUserId())))
                .toList();
    }

    private String currentMatriculeOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
