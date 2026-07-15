package tn.atb.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.atb.backend.entity.Client;

import java.util.Optional;

public interface ClientRepository extends MongoRepository<Client, String> {

    Optional<Client> findByCin(String cin);

    boolean existsByCin(String cin);
}
