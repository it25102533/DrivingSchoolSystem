package com.drivingschool.system.backend.manager;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    Optional<Manager> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Manager> findByEmailIgnoreCase(String email);
}
