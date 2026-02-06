package ru.assistant.bot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * BotInitializer
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BotInitializer {

    private final TelegramBotConfig botConfig;
    private final TelegramUpdateHandler updateHandler;

    @Bean
    public TelegramBotsApi telegramBotsApi(HomeworkBot homeworkBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(homeworkBot);
        log.info("Telegram bot успешно запущен: @{}", botConfig.getUsername());
        return botsApi;
    }

    @Bean
    public HomeworkBot homeworkBot() {
        return new HomeworkBot(
                botConfig.createBotOptions(),
                updateHandler,
                botConfig.getToken(),
                botConfig.getUsername()
        );
    }
}