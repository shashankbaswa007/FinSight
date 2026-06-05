package com.finsight.repository;

import com.finsight.model.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findTop50ByStatusAndNextRetryBeforeOrderByNextRetryAsc(
            WebhookDelivery.Status status,
            LocalDateTime nextRetry
    );

        @Query("select d from WebhookDelivery d where d.webhook.user.id = :userId " +
           "and d.createdAt between :start and :end")
        List<WebhookDelivery> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
        );
}
