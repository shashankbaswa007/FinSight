package com.finsight.repository;

import com.finsight.model.ReconciliationSchedulePreference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationSchedulePreferenceRepository extends JpaRepository<ReconciliationSchedulePreference, Long> {

    Optional<ReconciliationSchedulePreference> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    List<ReconciliationSchedulePreference> findByEnabledTrue();
}
