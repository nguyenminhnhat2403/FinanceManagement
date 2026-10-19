package org.example.financemanagement.auth.email;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailCodeRepository extends JpaRepository<EmailCode, UUID> {

    long countByEmailAndPurposeAndCreatedAtAfter(
            String email, String purpose, Instant after
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM EmailCode c WHERE c.id = :id")
    Optional<EmailCode> findForUpdate(@Param("id") UUID id);
}

