package tn.atb.backend.mapper;

import org.springframework.stereotype.Component;
import tn.atb.backend.dto.audit.AuditLogResponse;
import tn.atb.backend.entity.AuditLog;
import tn.atb.backend.entity.User;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log, User actor) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userFullName(actor != null ? actor.getFirstName() + " " + actor.getLastName() : null)
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
