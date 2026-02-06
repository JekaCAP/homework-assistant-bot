package ru.assistant.bot.telegram;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * TelegramBotConfig
 * @author agent
 * @since 03.02.2026
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotConfig {

    private String username;
    private String token;
    private Long timeout = 30L;
    private Boolean useWebhook = false;
    private AdminConfig admin;

    @Getter
    @Setter
    public static class AdminConfig {
        private String chatId;
        private Boolean notifyOnSubmission;
        private Boolean notifyOnReview;
        private Boolean notifyOnError;
        private List<Long> adminIds = new ArrayList<>();
    }

    public DefaultBotOptions createBotOptions() {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setMaxThreads(10);
        options.setGetUpdatesTimeout((int) (timeout * 1000));
        return options;
    }
}