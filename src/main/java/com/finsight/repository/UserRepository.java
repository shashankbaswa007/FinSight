package com.finsight.repository;

import com.finsight.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations.
 * 
 * Supports GDPR compliance queries for data export and deletion management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Find users marked for deletion whose retention period has expired.
     * Used for scheduled hard-delete job (GDPR Article 17).
     * 
     * @param cutoffDate The date before which users should be hard-deleted
     * @return List of users ready for hard-delete
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = true AND u.hardDeleteScheduledAt <= :cutoffDate")
    List<User> findDeletedUsersBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find all soft-deleted users.
     * 
     * @return List of all deleted users
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    List<User> findAllDeleted();
}
