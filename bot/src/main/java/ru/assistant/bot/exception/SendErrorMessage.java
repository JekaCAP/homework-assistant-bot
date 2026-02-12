package ru.assistant.bot.exception;

public class SendErrorMessage extends RuntimeException {
    public SendErrorMessage(String message) {
        super(message);
    }
}
