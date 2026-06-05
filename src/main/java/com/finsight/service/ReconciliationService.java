package com.finsight.service;

import com.finsight.dto.*;
import com.finsight.model.*;
import com.finsight.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReconciliationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReconciliationService.class);
    private static final BigDecimal EXACT_MATCH_THRESHOLD = new BigDecimal("0.01");
    private static final BigDecimal PARTIAL_MATCH_THRESHOLD = new BigDecimal("1.00");
    
    private final ReconciliationBatchRepository reconciliationBatchRepository;
    private final TransactionMatchRepository transactionMatchRepository;
    private final ExternalTransactionRepository externalTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    
    public ReconciliationService(ReconciliationBatchRepository reconciliationBatchRepository,
                                TransactionMatchRepository transactionMatchRepository,
                                ExternalTransactionRepository externalTransactionRepository,
                                TransactionRepository transactionRepository,
                                UserRepository userRepository) {
        this.reconciliationBatchRepository = reconciliationBatchRepository;
        this.transactionMatchRepository = transactionMatchRepository;
        this.externalTransactionRepository = externalTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a reconciliation batch for a specific date
     */
    public ReconciliationBatchResponse initializeReconciliationBatch(@NonNull Long userId, LocalDate batchDate) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReconciliationBatch batch = new ReconciliationBatch(user, batchDate);
        batch.setUpdatedAt(LocalDateTime.now());
        
        ReconciliationBatch saved = reconciliationBatchRepository.save(batch);
        logger.info("Initialized reconciliation batch for user {} on date {}", userId, batchDate);
        return ReconciliationBatchResponse.fromEntity(saved);
    }
    
    /**
     * Import external transactions (from bank CSV, API, etc.)
     */
    public List<ExternalTransaction> importExternalTransactions(@NonNull Long userId, ImportExternalTransactionsRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ExternalTransaction> imported = new ArrayList<>();
        
        for (ImportExternalTransactionsRequest.ExternalTransactionItem item : request.getTransactions()) {
            // Check if already exists
            Optional<ExternalTransaction> existing = externalTransactionRepository
                .findByExternalIdAndSourceAndUserId(item.getExternalId(), request.getSource(), userId);
            
            if (existing.isEmpty()) {
                ExternalTransaction transaction = new ExternalTransaction(
                    user,
                    item.getExternalId(),
                    request.getSource(),
                    item.getAmount(),
                    item.getTransactionDate()
                );
                transaction.setDescription(item.getDescription());
                
                imported.add(externalTransactionRepository.save(transaction));
            }
        }
        
        logger.info("Imported {} external transactions for user {}", imported.size(), userId);
        return imported;
    }
    
    /**
     * Perform reconciliation matching
     */
    public ReconciliationBatchResponse performReconciliation(@NonNull Long userId, LocalDate batchDate) {
        ReconciliationBatch batch = reconciliationBatchRepository
            .findByUserIdAndBatchDate(userId, batchDate)
            .orElseThrow(() -> new RuntimeException("Reconciliation batch not found"));
        
        batch.setStatus(ReconciliationBatch.ReconciliationStatus.IN_PROGRESS);
        batch.setUpdatedAt(LocalDateTime.now());
        
        // Get internal transactions for the date
        List<Transaction> internalTransactions = transactionRepository
            .findByUserIdAndDate(userId, batchDate);
        
        // Get external transactions for the date
        Page<ExternalTransaction> externalPage = externalTransactionRepository
            .findByUserIdAndTransactionDateOrderByTransactionDateDesc(userId, batchDate, Pageable.unpaged());
        List<ExternalTransaction> externalTransactions = externalPage.getContent();
        
        batch.setTotalTransactions(internalTransactions.size());
        
        // Perform matching algorithm
        Set<Long> matchedExternalIds = new HashSet<>();
        BigDecimal totalDiscrepancy = BigDecimal.ZERO;
        int matchedCount = 0;
        
        for (Transaction internal : internalTransactions) {
            Optional<ExternalTransaction> bestMatch = findBestMatch(internal, externalTransactions, matchedExternalIds);
            
            if (bestMatch.isPresent()) {
                ExternalTransaction external = bestMatch.get();
                BigDecimal variance = internal.getAmount().subtract(external.getAmount()).abs();
                
                TransactionMatch.MatchStatus matchStatus;
                if (variance.compareTo(EXACT_MATCH_THRESHOLD) <= 0) {
                    matchStatus = TransactionMatch.MatchStatus.EXACT_MATCH;
                } else if (variance.compareTo(PARTIAL_MATCH_THRESHOLD) <= 0) {
                    matchStatus = TransactionMatch.MatchStatus.PARTIAL_MATCH;
                } else {
                    matchStatus = TransactionMatch.MatchStatus.NO_MATCH;
                }
                
                BigDecimal confidence = calculateConfidence(internal, external);
                
                TransactionMatch match = new TransactionMatch(batch, internal);
                match.setExternalTransactionId(external.getExternalId());
                match.setExternalAmount(external.getAmount());
                match.setInternalAmount(internal.getAmount());
                match.setMatchConfidence(confidence);
                match.setMatchStatus(matchStatus);
                match.setVarianceAmount(variance);
                match.setMatchedAt(LocalDateTime.now());
                
                transactionMatchRepository.save(match);
                matchedExternalIds.add(external.getId());
                
                if (matchStatus != TransactionMatch.MatchStatus.NO_MATCH) {
                    matchedCount++;
                }
                totalDiscrepancy = totalDiscrepancy.add(variance);
            }
        }
        
        // Update batch statistics
        batch.setMatchedTransactions(matchedCount);
        batch.setUnmatchedTransactions(internalTransactions.size() - matchedCount);
        batch.setDiscrepancyAmount(totalDiscrepancy);
        batch.setStatus(ReconciliationBatch.ReconciliationStatus.COMPLETED);
        batch.setUpdatedAt(LocalDateTime.now());
        
        ReconciliationBatch saved = reconciliationBatchRepository.save(batch);
        logger.info("Completed reconciliation for user {} on date {}. Matched: {}/{}", 
                userId, batchDate, matchedCount, internalTransactions.size());
        
        return ReconciliationBatchResponse.fromEntity(saved);
    }
    
    /**
     * Find best matching external transaction for an internal transaction
     */
    private Optional<ExternalTransaction> findBestMatch(
            Transaction internal,
            List<ExternalTransaction> externals,
            Set<Long> alreadyMatched) {
        
        return externals.stream()
            .filter(e -> !alreadyMatched.contains(e.getId()))
            .filter(e -> e.getTransactionDate().isEqual(internal.getDate()))
            .min(Comparator.comparingDouble(e -> {
                BigDecimal diff = internal.getAmount().subtract(e.getAmount()).abs();
                return diff.doubleValue();
            }));
    }
    
    /**
     * Calculate match confidence score (0-100)
     */
    private BigDecimal calculateConfidence(Transaction internal, ExternalTransaction external) {
        BigDecimal baseScore = new BigDecimal("100");
        BigDecimal variance = internal.getAmount().subtract(external.getAmount()).abs();
        
        if (variance.compareTo(BigDecimal.ZERO) == 0) {
            return baseScore;
        }
        
        // Reduce score based on variance percentage
        BigDecimal variancePercent = variance.divide(internal.getAmount(), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
        
        BigDecimal penalty = variancePercent.multiply(new BigDecimal("0.5")); // 0.5% penalty per 1% variance
        return baseScore.subtract(penalty).max(new BigDecimal("0"));
    }
    
    /**
     * Get reconciliation batch details
     */
    @Transactional(readOnly = true)
    public ReconciliationBatchResponse getReconciliationBatch(@NonNull Long userId, @NonNull Long batchId) {
        ReconciliationBatch batch = reconciliationBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found"));
        
        if (!batch.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        return ReconciliationBatchResponse.fromEntity(batch);
    }
    
    /**
     * Get all reconciliation batches for a user
     */
    @Transactional(readOnly = true)
    public Page<ReconciliationBatchResponse> getReconciliationBatches(@NonNull Long userId, Pageable pageable) {
        return reconciliationBatchRepository.findByUserIdOrderByBatchDateDesc(userId, pageable)
            .map(ReconciliationBatchResponse::fromEntity);
    }
    
    /**
     * Get transaction matches for a batch
     */
    @Transactional(readOnly = true)
    public List<TransactionMatchResponse> getTransactionMatches(@NonNull Long userId, @NonNull Long batchId) {
        ReconciliationBatch batch = reconciliationBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found"));
        
        if (!batch.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        return transactionMatchRepository.findByReconciliationBatchId(batchId)
            .stream()
            .map(TransactionMatchResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Export a reconciliation batch's matches as CSV
     */
    @Transactional(readOnly = true)
    public String exportBatchCsv(@NonNull Long userId, @NonNull Long batchId) {
        ReconciliationBatch batch = reconciliationBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (!batch.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<TransactionMatch> matches = transactionMatchRepository.findByReconciliationBatchId(batchId);

        StringBuilder sb = new StringBuilder();
        sb.append("internalTransactionId,externalTransactionId,internalAmount,externalAmount,matchStatus,matchConfidence,varianceAmount,variancePercentage,matchedAt,notes\n");

        for (TransactionMatch m : matches) {
            sb.append(m.getInternalTransaction().getId()).append(',');
            sb.append('"').append(m.getExternalTransactionId() != null ? m.getExternalTransactionId().replace("\"", "\"\"") : "").append('"').append(',');
            sb.append(m.getInternalAmount() != null ? m.getInternalAmount().toPlainString() : "").append(',');
            sb.append(m.getExternalAmount() != null ? m.getExternalAmount().toPlainString() : "").append(',');
            sb.append(m.getMatchStatus() != null ? m.getMatchStatus().name() : "").append(',');
            sb.append(m.getMatchConfidence() != null ? m.getMatchConfidence().toPlainString() : "").append(',');
            sb.append(m.getVarianceAmount() != null ? m.getVarianceAmount().toPlainString() : "").append(',');
            sb.append(m.getVariancePercentage() != null ? m.getVariancePercentage().toPlainString() : "").append(',');
            sb.append(m.getMatchedAt() != null ? m.getMatchedAt().toString() : "").append(',');
            sb.append('"').append(m.getNotes() != null ? m.getNotes().replace("\"", "\"\"") : "").append('"');
            sb.append('\n');
        }

        return sb.toString();
    }
}
