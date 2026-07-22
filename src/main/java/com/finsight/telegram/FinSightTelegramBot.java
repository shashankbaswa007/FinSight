package com.finsight.telegram;

import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import com.finsight.service.AiService;
import com.finsight.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Profile("!test")
public class FinSightTelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(FinSightTelegramBot.class);

    private final String botUsername;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final TransactionService transactionService;

    public FinSightTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            UserRepository userRepository,
            AiService aiService,
            TransactionService transactionService) {
        super(botToken);
        this.botUsername = botUsername;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.transactionService = transactionService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChatId();

        // 1. Check if user is trying to link an account with a 6-digit code
        if (messageText.length() == 6 && messageText.matches("\\d{6}")) {
            handleLinkingCode(chatId, messageText);
            return;
        }

        // 2. Lookup existing linked user
        Optional<User> optionalUser = userRepository.findByTelegramChatId(chatId);
        if (optionalUser.isEmpty()) {
            sendMessage(chatId, "Welcome to FinSight! To chat with me, please link your account by generating a 6-digit code in the FinSight web application and pasting it here.");
            return;
        }

        User user = optionalUser.get();

        // 3. Process AI Chat
        if (messageText.equalsIgnoreCase("/start")) {
            sendMessage(chatId, "Hello " + user.getName() + "! I am your FinSight AI advisor. How can I help you today?");
            return;
        }

        sendMessage(chatId, "Thinking...");
        
        try {
            LocalDate now = LocalDate.now();
            String contextData = "Current month: " + now.getMonth() + " " + now.getYear();
            try {
                var summary = transactionService.getMonthlyTransactionSummary(user.getId(), now.getMonthValue(), now.getYear());
                contextData = String.format("Current month: Income $%.2f, Expense $%.2f, Balance $%.2f", 
                    summary.getTotalIncome(), summary.getTotalExpense(), summary.getTotalIncome().subtract(summary.getTotalExpense()));
            } catch (Exception e) {
                log.warn("Failed to get monthly summary for telegram context: {}", e.getMessage());
            }

            String aiResponse = aiService.getFinancialAdvice(user.getId(), messageText, contextData);
            sendMessage(chatId, aiResponse);
        } catch (Exception e) {
            log.error("Error processing AI response", e);
            sendMessage(chatId, "Sorry, I am having trouble connecting to my AI brain right now.");
        }
    }

    private void handleLinkingCode(Long chatId, String code) {
        Optional<User> optionalUser = userRepository.findByTelegramLinkingCode(code);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setTelegramChatId(chatId);
            user.setTelegramLinkingCode(null);
            userRepository.save(user);
            sendMessage(chatId, "✅ Successfully linked to your FinSight account (" + user.getEmail() + ")!\n\nYou can now ask me questions about your finances.");
        } else {
            sendMessage(chatId, "❌ Invalid or expired linking code. Please generate a new one in the FinSight web application.");
        }
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to Telegram", e);
        }
    }
}
