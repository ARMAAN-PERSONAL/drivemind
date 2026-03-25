package com.drivemind.repository;

import com.drivemind.model.User;
import com.drivemind.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
}