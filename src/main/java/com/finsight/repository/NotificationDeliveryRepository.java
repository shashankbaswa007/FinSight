package com.finsight.repository;

import com.finsight.model.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    @Query("select d.notification.id from NotificationDelivery d " +
           "where d.notification.id in :notificationIds and d.channel = :channel")
    List<Long> findDeliveredNotificationIds(
            @Param("notificationIds") List<Long> notificationIds,
            @Param("channel") NotificationDelivery.Channel channel
    );

        @Query("select d from NotificationDelivery d where d.notification.user.id = :userId " +
           "and d.createdAt between :start and :end")
        List<NotificationDelivery> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
        );
}
