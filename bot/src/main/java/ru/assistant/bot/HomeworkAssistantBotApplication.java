package ru.assistant.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Точка входа приложения Telegram бота для сдачи домашних заданий.
 *
 * <p>Основной класс Spring Boot приложения, который:
 * <ul>
 *   <li>Запускает бота для проверки домашних работ через GitHub</li>
 *   <li>Включает асинхронную обработку сообщений</li>
 *   <li>Автоматически загружает конфигурацию из application.yml</li>
 * </ul>
 *
 * <p>Конфигурация бота включает настройки Telegram, базы данных и GitHub API.
 * Асинхронная обработка используется для параллельной обработки сообщений от студентов
 * и взаимодействия с внешними API.
 *
 * @author agent
 * @since 03.02.2026
 */
@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties
public class HomeworkAssistantBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeworkAssistantBotApplication.class, args);
    }
}
