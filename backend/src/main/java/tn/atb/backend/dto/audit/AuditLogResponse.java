package tn.atb.backend.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tn.atb.backend.entity.enums.AuditAction;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AuditLogResponse {

    private String id;
    private String userId;
    private String userFullName;
    private AuditAction action;
    private String entity;
    private String entityId;
    private String description;
    private LocalDateTime createdAt;
}
