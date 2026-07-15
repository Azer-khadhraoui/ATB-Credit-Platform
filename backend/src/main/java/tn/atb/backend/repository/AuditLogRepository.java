package tn.atb.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.atb.backend.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUserId(String userId);
}
