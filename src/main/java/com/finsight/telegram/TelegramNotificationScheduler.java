package com.finsight.telegram;

import com.finsight.dto.MonthlyTransactionSummary;
import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import com.finsight.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TelegramNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationScheduler.class);

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final FinSightTelegramBot telegramBot;

    public TelegramNotificationScheduler(UserRepository userRepository, TransactionService transactionService, FinSightTelegramBot telegramBot) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.telegramBot = telegramBot;
    }

    // Run every Sunday at 10 AM
    @Scheduled(cron = "0 0 10 * * SUN")
    public void sendWeeklySummaries() {
        log.info("Starting weekly Telegram summary notifications...");
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getTelegramChatId() != null)
                .toList();

        LocalDate now = LocalDate.now();

        for (User user : users) {
            try {
                MonthlyTransactionSummary summary = transactionService.getMonthlyTransactionSummary(user.getId(), now.getMonthValue(), now.getYear());
                String message = String.format(
                        "📊 *Weekly FinSight Update*\n\n" +
                        "Here is your current standing for %s %d:\n\n" +
                        "🟢 *Income:* $%.2f\n" +
                        "🔴 *Expense:* $%.2f\n" +
                        "💰 *Balance:* $%.2f\n\n" +
                        "Keep up the good work! Feel free to ask me any questions about your budget or spending.",
                        now.getMonth(), now.getYear(),
                        summary.getTotalIncome(), summary.getTotalExpense(), summary.getTotalIncome().subtract(summary.getTotalExpense())
                );
                telegramBot.sendMessage(user.getTelegramChatId(), message);
            } catch (Exception e) {
                log.error("Failed to send weekly summary to user " + user.getId(), e);
            }
        }
        log.info("Finished weekly Telegram summary notifications.");
    }
}
