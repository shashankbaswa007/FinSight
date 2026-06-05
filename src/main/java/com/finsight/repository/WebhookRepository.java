package com.finsight.repository;

import com.finsight.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    
    @Query("SELECT w FROM Webhook w WHERE w.user.id = :userId AND w.active = true")
    List<Webhook> findByUserIdAndActive(@Param("userId") Long userId);
    
    @Query("SELECT w FROM Webhook w WHERE w.user.id = :userId")
    List<Webhook> findByUserId(@Param("userId") Long userId);
}
