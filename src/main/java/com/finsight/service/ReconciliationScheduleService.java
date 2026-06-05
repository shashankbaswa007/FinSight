package com.finsight.service;

import com.finsight.dto.ReconciliationScheduleSettingsResponse;
import com.finsight.dto.UpdateReconciliationScheduleRequest;
import com.finsight.model.ReconciliationSchedulePreference;
import com.finsight.model.User;
import com.finsight.repository.ReconciliationSchedulePreferenceRepository;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReconciliationScheduleService {

    private final ReconciliationSchedulePreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Value("${finsight.reconciliation.scheduled.cron:0 0 2 * * *}")
    private String cron;

    @Value("${finsight.reconciliation.scheduled.enabled:false}")
    private boolean globalEnabled;

    public ReconciliationScheduleService(
            ReconciliationSchedulePreferenceRepository preferenceRepository,
            UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ReconciliationScheduleSettingsResponse getSettings(@NonNull Long userId) {
        boolean enabled = preferenceRepository.findByUserId(userId)
                .map(ReconciliationSchedulePreference::getEnabled)
                .orElse(false);
        return new ReconciliationScheduleSettingsResponse(globalEnabled, cron, enabled);
    }

    public ReconciliationScheduleSettingsResponse updateSettings(@NonNull Long userId, UpdateReconciliationScheduleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ReconciliationSchedulePreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> new ReconciliationSchedulePreference(user));
        preference.setEnabled(request.getEnabled());
        preferenceRepository.save(preference);

        return new ReconciliationScheduleSettingsResponse(globalEnabled, cron, preference.getEnabled());
    }

    @Transactional(readOnly = true)
    public List<ReconciliationSchedulePreference> getEnabledPreferences() {
        return preferenceRepository.findByEnabledTrue();
    }
}
