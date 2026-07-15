package tn.atb.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.atb.backend.entity.CreditFile;

import java.util.List;

public interface CreditFileRepository extends MongoRepository<CreditFile, String> {

    List<CreditFile> findByClientId(String clientId);

    List<CreditFile> findByCreatedBy(String createdBy);
}
