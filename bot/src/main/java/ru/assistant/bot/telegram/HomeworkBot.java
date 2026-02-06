package ru.assistant.bot.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

/**
 * HomeworkBot
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
public class HomeworkBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final TelegramUpdateHandler updateHandler;

    public HomeworkBot(
            DefaultBotOptions options,
            TelegramUpdateHandler updateHandler,
            String botToken,
            String botUsername) {
        super(options, botToken);
        this.botUsername = botUsername;
        this.updateHandler = updateHandler;
        log.info("HomeworkBot created with username: {}", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Received update: {}", update);
        updateHandler.handleUpdate(update, this);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(this::onUpdateReceived);
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message: {}", message.getText(), e);
        }
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T executeMethod(Method method) {
        try {
            return execute(method);
        } catch (TelegramApiException e) {
            log.error("Error executing method: {}", method, e);
            return null;
        }
    }
}