package tn.atb.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.atb.backend.entity.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMatricule(String matricule);

    boolean existsByEmail(String email);

    boolean existsByMatricule(String matricule);
}
