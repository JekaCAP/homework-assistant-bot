package ru.assistant.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties
public class HomeworkAssistantBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeworkAssistantBotApplication.class, args);
    }
}
