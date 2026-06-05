package com.finsight.controller;

import com.finsight.dto.*;
import com.finsight.service.ReconciliationScheduleService;
import com.finsight.service.ReconciliationService;
import com.finsight.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController extends BaseController {
    
    private final ReconciliationService reconciliationService;
    private final ReconciliationScheduleService scheduleService;
    private final SecurityUtil securityUtil;
    
    public ReconciliationController(ReconciliationService reconciliationService,
                                    ReconciliationScheduleService scheduleService,
                                    SecurityUtil securityUtil) {
        this.reconciliationService = reconciliationService;
        this.scheduleService = scheduleService;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Initialize a new reconciliation batch for a specific date
     */
    @PostMapping("/batches")
    public ResponseEntity<ReconciliationBatchResponse> initializeBatch(
            @RequestParam LocalDate batchDate) {
        Long userId = getUserId();
        ReconciliationBatchResponse batch = reconciliationService.initializeReconciliationBatch(userId, batchDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }
    
    /**
     * Import external transactions from bank or CSV
     */
    @PostMapping("/import-transactions")
    public ResponseEntity<ImportResponse> importExternalTransactions(
            @Valid @RequestBody ImportExternalTransactionsRequest request) {
        Long userId = getUserId();
        int importedCount = reconciliationService.importExternalTransactions(userId, request).size();
        return ResponseEntity.ok(new ImportResponse(importedCount, "Transactions imported successfully"));
    }
    
    /**
     * Perform reconciliation matching for a batch
     */
    @PostMapping("/batches/{batchId}/reconcile")
    public ResponseEntity<ReconciliationBatchResponse> performReconciliation(
            @PathVariable @NonNull Long batchId,
            @RequestParam LocalDate batchDate) {
        Long userId = getUserId();
        ReconciliationBatchResponse result = reconciliationService.performReconciliation(userId, batchDate);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get reconciliation batch details
     */
    @GetMapping("/batches/{batchId}")
    public ResponseEntity<ReconciliationBatchResponse> getBatch(
            @PathVariable @NonNull Long batchId) {
        Long userId = getUserId();
        ReconciliationBatchResponse batch = reconciliationService.getReconciliationBatch(userId, batchId);
        return ResponseEntity.ok(batch);
    }
    
    /**
     * Get all reconciliation batches for current user (paginated)
     */
    @GetMapping("/batches")
    public ResponseEntity<Page<ReconciliationBatchResponse>> getBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ReconciliationBatchResponse> batches = reconciliationService.getReconciliationBatches(userId, pageable);
        return ResponseEntity.ok(batches);
    }
    
    /**
     * Get transaction matches for a batch
     */
    @GetMapping("/batches/{batchId}/matches")
    public ResponseEntity<List<TransactionMatchResponse>> getMatches(
            @PathVariable @NonNull Long batchId) {
        Long userId = getUserId();
        List<TransactionMatchResponse> matches = reconciliationService.getTransactionMatches(userId, batchId);
        return ResponseEntity.ok(matches);
    }

    /**
     * Export reconciliation batch matches as CSV (or JSON in future)
     */
    @GetMapping("/batches/{batchId}/export")
    public ResponseEntity<byte[]> exportBatch(
            @PathVariable @NonNull Long batchId,
            @RequestParam(defaultValue = "csv") String format) {
        Long userId = getUserId();

        if (!"csv".equalsIgnoreCase(format)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String csv = reconciliationService.exportBatchCsv(userId, batchId);
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                .filename("reconciliation-batch-" + batchId + ".csv").build());

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * Get scheduled reconciliation settings for the current user
     */
    @GetMapping("/schedule")
    public ResponseEntity<ReconciliationScheduleSettingsResponse> getScheduleSettings() {
        Long userId = getUserId();
        return ResponseEntity.ok(scheduleService.getSettings(userId));
    }

    /**
     * Update scheduled reconciliation settings for the current user
     */
    @PutMapping("/schedule")
    public ResponseEntity<ReconciliationScheduleSettingsResponse> updateScheduleSettings(
            @Valid @RequestBody UpdateReconciliationScheduleRequest request) {
        Long userId = getUserId();
        return ResponseEntity.ok(scheduleService.updateSettings(userId, request));
    }

    private @NonNull Long getUserId() {
        return Objects.requireNonNull(securityUtil.getCurrentUserId(), "userId");
    }
}
