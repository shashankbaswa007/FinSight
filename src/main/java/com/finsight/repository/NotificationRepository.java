package com.finsight.repository;

import com.finsight.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, Boolean read, Pageable pageable);
    
    long countByUserIdAndRead(Long userId, Boolean read);
    
    List<Notification> findByUserIdAndRead(Long userId, Boolean read);

    List<Notification> findByUserIdAndReadFalseAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
