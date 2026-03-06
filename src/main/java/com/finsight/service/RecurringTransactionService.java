package com.finsight.service;

import com.finsight.dto.RecurringTransactionRequest;
import com.finsight.dto.RecurringTransactionResponse;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.*;
import com.finsight.repository.RecurringTransactionRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionService {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionService.class);

    private final RecurringTransactionRepository recurringRepo;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public RecurringTransactionService(RecurringTransactionRepository recurringRepo,
                                       TransactionRepository transactionRepository,
                                       UserRepository userRepository,
                                       CategoryService categoryService) {
        this.recurringRepo = recurringRepo;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public RecurringTransactionResponse create(Long userId, RecurringTransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Category category = categoryService.findById(request.getCategoryId());

        RecurringTransaction rt = new RecurringTransaction();
        rt.setUser(user);
        rt.setAmount(request.getAmount());
        rt.setType(request.getType());
        rt.setCategory(category);
        rt.setDescription(request.getDescription());
        rt.setFrequency(request.getFrequency());
        rt.setStartDate(request.getStartDate());
        rt.setEndDate(request.getEndDate());
        rt.setNextOccurrence(request.getStartDate());
        rt.setActive(true);

        rt = recurringRepo.save(rt);
        return mapToResponse(rt);
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getByUser(Long userId) {
        return recurringRepo.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(Long userId, Long id) {
        RecurringTransaction rt = recurringRepo.findById(id)
                .filter(r -> r.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", "id", id));
        rt.setActive(false);
        recurringRepo.save(rt);
    }

    /** Runs daily at midnight to generate transactions from recurring schedules. */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processRecurring() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringRepo.findByActiveAndNextOccurrenceLessThanEqual(true, today);

        for (RecurringTransaction rt : due) {
            // Check if end date passed
            if (rt.getEndDate() != null && today.isAfter(rt.getEndDate())) {
                rt.setActive(false);
                recurringRepo.save(rt);
                continue;
            }

            // Create transaction
            Transaction tx = Transaction.builder()
                    .user(rt.getUser())
                    .amount(rt.getAmount())
                    .type(rt.getType())
                    .category(rt.getCategory())
                    .description(rt.getDescription())
                    .date(rt.getNextOccurrence())
                    .build();
            transactionRepository.save(tx);

            // Advance next occurrence
            rt.setNextOccurrence(calculateNext(rt.getNextOccurrence(), rt.getFrequency()));
            recurringRepo.save(rt);

            log.info("Created recurring transaction for user={} amount={} category={}",
                    rt.getUser().getId(), rt.getAmount(), rt.getCategory().getName());
        }
    }

    private LocalDate calculateNext(LocalDate current, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }

    private RecurringTransactionResponse mapToResponse(RecurringTransaction rt) {
        return RecurringTransactionResponse.builder()
                .id(rt.getId())
                .amount(rt.getAmount())
                .type(rt.getType())
                .categoryId(rt.getCategory().getId())
                .categoryName(rt.getCategory().getName())
                .description(rt.getDescription())
                .frequency(rt.getFrequency())
                .startDate(rt.getStartDate())
                .endDate(rt.getEndDate())
                .nextOccurrence(rt.getNextOccurrence())
                .active(rt.isActive())
                .build();
    }
}
