package com.Moventia.auth.repository;

import com.Moventia.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);   // ← ADD THIS if not already present

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
