package ru.assistant.bot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.repository.StudentRepository;
import ru.assistant.bot.model.enums.UserState;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private final CommandHandler commandHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final StudentRepository studentRepository;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, UserContext> userContexts = new ConcurrentHashMap<>();

    public void handleUpdate(Update update, AbsSender sender) {
        try {
            Long userId = getUserId(update);

            if (userId == null) {
                log.warn("No user ID in update: {}", update);
                return;
            }

            if (update.hasMessage()) {
                registerOrUpdateStudent(update.getMessage().getFrom());
            } else if (update.hasCallbackQuery()) {
                registerOrUpdateStudent(update.getCallbackQuery().getFrom());
            }

            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();

                if (text.startsWith("/")) {
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                } else {
                    messageHandler.handleMessage(update, sender, userStates, userContexts);
                }

            } else if (update.hasCallbackQuery()) {
                callbackQueryHandler.handleCallbackQuery(update, sender, userStates, userContexts);
            }

        } catch (Exception e) {
            log.error("Error handling update", e);
            handleError(update, sender, e);
        }
    }

    private Student registerOrUpdateStudent(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Long telegramId = telegramUser.getId();
        String username = telegramUser.getUserName();
        String fullName = getFullName(telegramUser);

        return studentRepository.findByTelegramId(telegramId)
                .map(existingStudent -> updateStudent(existingStudent, username, fullName))
                .orElseGet(() -> createStudent(telegramId, username, fullName));
    }

    private Student createStudent(Long telegramId, String username, String fullName) {
        Student student = Student.builder()
                .telegramId(telegramId)
                .telegramUsername(username)
                .fullName(fullName)
                .registrationDate(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .isActive(true)
                .build();

        Student saved = studentRepository.save(student);
        log.info("Создан новый студент: {} (telegramId: {})", fullName, telegramId);
        return saved;
    }

    private Student updateStudent(Student student, String username, String fullName) {
        student.setTelegramUsername(username);
        student.setFullName(fullName);
        student.setLastActivity(LocalDateTime.now());

        Student updated = studentRepository.save(student);
        log.debug("Обновлен студент: {} (telegramId: {})", fullName, student.getTelegramId());
        return updated;
    }

    private String getFullName(org.telegram.telegrambots.meta.api.objects.User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? " " + user.getLastName() : "";
        return (firstName + lastName).trim();
    }

    private Long getUserId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        }
        return null;
    }

    private void handleError(Update update, AbsSender sender, Exception e) {
        try {
            String chatId = update.hasMessage()
                    ? update.getMessage().getChatId().toString()
                    : update.getCallbackQuery().getMessage().getChatId().toString();

            SendMessage errorMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text("Произошла ошибка. Пожалуйста, попробуйте позже или обратитесь к администратору.")
                    .build();

            sender.execute(errorMessage);
        } catch (Exception ex) {
            log.error("Error sending error message", ex);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class UserContext {
        private Long selectedCourseId;
        private Long selectedAssignmentId;
        private Long currentSubmissionId;
        private String githubUsername;
        private Map<String, Object> metadata;
    }
}