package com.finsight.scheduler;

import com.finsight.dto.ReconciliationBatchResponse;
import com.finsight.model.Notification;
import com.finsight.model.ReconciliationSchedulePreference;
import com.finsight.model.User;
import com.finsight.repository.ReconciliationBatchRepository;
import com.finsight.service.NotificationService;
import com.finsight.service.ReconciliationScheduleService;
import com.finsight.service.ReconciliationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class ScheduledReconciliationTaskTest {

    @Mock
    private ReconciliationService reconciliationService;

    @Mock
    private ReconciliationScheduleService scheduleService;

    @Mock
    private ReconciliationBatchRepository reconciliationBatchRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ScheduledReconciliationTask scheduledReconciliationTask;

    @Test
    @SuppressWarnings("unused")
    void runDailyReconciliation_runsForEachUser() {
        LocalDate batchDate = LocalDate.now().minusDays(1);
        User user1 = User.builder()
                .id(1L)
                .name("User One")
                .email("user1@test.com")
                .password("pwd")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("User Two")
                .email("user2@test.com")
                .password("pwd")
                .build();

        ReconciliationSchedulePreference pref1 = new ReconciliationSchedulePreference(user1);
        pref1.setEnabled(true);
        ReconciliationSchedulePreference pref2 = new ReconciliationSchedulePreference(user2);
        pref2.setEnabled(true);

        when(scheduleService.getEnabledPreferences()).thenReturn(List.of(pref1, pref2));
        when(reconciliationBatchRepository.findByUserIdAndBatchDate(anyLong(), any(LocalDate.class)))
            .thenReturn(Optional.empty());
        when(reconciliationService.performReconciliation(anyLong(), any(LocalDate.class)))
            .thenReturn(new ReconciliationBatchResponse(
                10L,
                batchDate,
                "COMPLETED",
                3,
                2,
                1,
                java.math.BigDecimal.ONE,
                66.7,
                null,
                null
            ));

        scheduledReconciliationTask.runDailyReconciliation();

        verify(reconciliationService).initializeReconciliationBatch(1L, batchDate);
        verify(reconciliationService).initializeReconciliationBatch(2L, batchDate);
        verify(reconciliationService).performReconciliation(1L, batchDate);
        verify(reconciliationService).performReconciliation(2L, batchDate);
        verify(notificationService, times(2)).createNotification(
            anyLong(),
            org.mockito.ArgumentMatchers.eq(Notification.NotificationType.RECONCILIATION_ALERT),
            anyString(),
            anyString(),
            anyString()
        );
    }
}
