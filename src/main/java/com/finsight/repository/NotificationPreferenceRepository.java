package com.finsight.repository;

import com.finsight.model.NotificationPreference;
import com.finsight.model.NotificationPreference.AlertFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserId(Long userId);

    List<NotificationPreference> findByAlertEmailTrueAndAlertFrequencyIn(List<AlertFrequency> frequencies);
}
