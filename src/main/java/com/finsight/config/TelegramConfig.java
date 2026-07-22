package com.finsight.config;

import com.finsight.telegram.FinSightTelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(FinSightTelegramBot finSightTelegramBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(finSightTelegramBot);
        return api;
    }
}
