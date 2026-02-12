package ru.assistant.bot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.dto.StudentRatingDto;
import ru.assistant.bot.model.enums.UserState;
import ru.assistant.bot.service.CourseService;
import ru.assistant.bot.service.RatingService;
import ru.assistant.bot.service.StudentService;
import ru.assistant.bot.service.SubmissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * MessageHandler
 *
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final StudentService studentService;
    private final SubmissionService submissionService;
    private final CommandHandler commandHandler;
    private final RatingService ratingService;
    private final CourseService courseService;

    private static final Pattern GITHUB_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}$");
    private static final Pattern PR_URL_PATTERN = Pattern.compile("^https://github\\.com/[^/]+/[^/]+/pull/\\d+$");

    public void handleMessage(
            Update update,
            AbsSender sender,
            Map<Long, UserState> userStates,
            Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        Long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

        UserState currentState = userStates.getOrDefault(userId, UserState.IDLE);

        log.info("User {} in state {} sent message: {}", userId, currentState, text);

        boolean isMenuCommand = text.equals("📤 Сдать домашку") ||
                                text.equals("📊 Мой прогресс") ||
                                text.equals("📊 По курсам") ||
                                text.equals("📈 График") ||
                                text.equals("🏆 Рейтинг") ||
                                text.equals("⚙️ Настройки") ||
                                text.equals("🔗 Изменить GitHub") ||
                                text.equals("🔔 Уведомления") ||
                                text.equals("🎯 Цели") ||
                                text.equals("❓ Помощь") ||
                                text.equals("🔙 Назад") ||
                                text.equals("🔙 В меню") ||
                                text.equals("👨‍💼 Админ-панель") ||

                                text.equals("⏳ На проверке") ||
                                text.equals("✅ Проверить работу") ||
                                text.equals("📊 Статистика") ||
                                text.equals("👥 Студенты") ||
                                text.equals("📚 Курсы");

        if (isMenuCommand) {
            userStates.put(userId, UserState.IDLE);
            userContexts.remove(userId);
            handleTextCommand(userId, text, sender, update, userStates, userContexts);
            return;
        }

        if (text.startsWith("/")) {
            userStates.put(userId, UserState.IDLE);
            userContexts.remove(userId);
            handleTextCommand(userId, text, sender, update, userStates, userContexts);
            return;
        }

        switch (currentState) {
            case WAITING_FOR_GITHUB_USERNAME:
                handleGithubUsername(userId, text, sender, userStates, userContexts);
                break;

            case WAITING_FOR_PR_LINK:
                handlePrLink(userId, text, sender, userStates, userContexts);
                break;

            case IDLE:
                handleTextCommand(userId, text, sender, update, userStates, userContexts);
                break;

            default:
                handleStateExceptionMessage(userId, currentState, text, sender);
        }
    }

    private void handleStateExceptionMessage(Long userId, UserState state, String text, AbsSender sender) {
        String message;

        switch (state) {
            case WAITING_FOR_COURSE_SELECTION:
                message = "Пожалуйста, выберите курс из списка выше.";
                break;

            case WAITING_FOR_ASSIGNMENT_SELECTION:
                message = "Пожалуйста, выберите задание из списка выше или нажмите 'Назад к курсам'.";
                break;

            default:
                message = "Я не совсем понял ваше сообщение.\n\n" +
                          "Используйте кнопки меню или команды:\n" +
                          "• /submit - Сдать задание\n" +
                          "• /progress - Ваш прогресс\n" +
                          "• /help - Помощь";
        }

        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId.toString())
                .text(message)
                .parseMode("Markdown")
                .replyMarkup(commandHandler.getMainMenuKeyboard(userId))
                .build();

        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }

    private void handleGithubUsername(Long userId, String text, AbsSender sender,
                                      Map<Long, UserState> userStates,
                                      Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        if (!GITHUB_USERNAME_PATTERN.matcher(text).matches()) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("*Неверный формат GitHub username!*\n\n" +
                          "Пожалуйста, отправьте только логин (например: `ivanov` или `johndoe`).\n" +
                          "Логин должен содержать только латинские буквы, цифры и дефисы.")
                    .parseMode("Markdown")
                    .build();

            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        Student student = studentService.updateGithubUsername(userId, text);

        TelegramUpdateHandler.UserContext context = userContexts.getOrDefault(userId,
                TelegramUpdateHandler.UserContext.builder().build());
        context.setGithubUsername(text);
        userContexts.put(userId, context);

        String responseText = String.format("""
                *GitHub аккаунт успешно привязан!*
                
                Теперь вы можете сдавать задания.
                Ваш GitHub: @%s
                
                *Что дальше?*
                1. Нажмите /submit чтобы начать сдачу
                2. Выберите курс и задание
                3. Отправьте ссылку на Pull Request
                """, text);

        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text(responseText)
                .parseMode("Markdown")
                .replyMarkup(commandHandler.getMainMenuKeyboard(userId))
                .build();

        userStates.put(userId, UserState.IDLE);

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }

    private void handlePrLink(Long userId, String text, AbsSender sender,
                              Map<Long, UserState> userStates,
                              Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        if (!PR_URL_PATTERN.matcher(text).matches()) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("*Неверный формат ссылки!*\n\n" +
                          "Пожалуйста, отправьте ссылку в формате:\n" +
                          "```https://github.com/username/repository/pull/123```\n\n" +
                          "Убедитесь, что:\n" +
                          "• PR открыт\n" +
                          "• Автор PR должен совпадать с вашим GitHub\n" +
                          "• В названии указан номер задания")
                    .parseMode("Markdown")
                    .build();

            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        TelegramUpdateHandler.UserContext context = userContexts.get(userId);
        if (context == null || context.getSelectedAssignmentId() == null) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("Ошибка: задание не выбрано. Пожалуйста, начните снова.")
                    .build();

            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        try {
            Submission submission = submissionService.createSubmission(
                    userId,
                    context.getSelectedAssignmentId(),
                    text
            );

            userStates.put(userId, UserState.IDLE);
            userContexts.remove(userId);

            log.info("Сдача создана ID={}. Студенту {} не отправлено подтверждение - " +
                     "дожидаемся NotificationService", submission.getId(), userId);

        } catch (Exception e) {
            log.error("Error creating submission", e);

            String errorMessage = "*Ошибка при создании сдачи!*\n\n";

            if (e.getMessage().contains("already submitted")) {
                errorMessage += "Вы уже сдавали это задание.\n";
                errorMessage += "Проверьте статус в /progress\n";
            } else if (e.getMessage().contains("GitHub validation")) {
                errorMessage += "Ошибка валидации GitHub:\n";
                errorMessage += "• Убедитесь, что PR открыт\n";
                errorMessage += "• Автор PR должен совпадать с вашим GitHub\n";
                errorMessage += "• Проверьте правильность ссылки\n";
            } else {
                errorMessage += "Пожалуйста, попробуйте позже или обратитесь к администратору.\n";
            }

            SendMessage errorResponse = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(errorMessage)
                    .parseMode("Markdown")
                    .build();

            try {
                sender.execute(errorResponse);
            } catch (TelegramApiException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    private void handleTextCommand(Long userId, String text, AbsSender sender, Update update,
                                   Map<Long, UserState> userStates,
                                   Map<Long, TelegramUpdateHandler.UserContext> userContexts) {
        userStates.put(userId, UserState.IDLE);
        userContexts.remove(userId);

        String originalText = update.getMessage().getText();

        try {
            switch (text) {
                case "📤 Сдать домашку":
                    update.getMessage().setText("/submit");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "📊 Мой прогресс":
                case "📊 По курсам":
                    update.getMessage().setText("/progress");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "⚙️ Настройки":
                    update.getMessage().setText("/settings");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "❓ Помощь":
                case "Помощь":
                    update.getMessage().setText("/help");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "👨‍💼 Админ-панель":
                    handleAdminPanelButton(userId, sender);
                    break;

                case "⏳ На проверке":
                    update.getMessage().setText("/pending");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "✅ Проверить работу":
                    handleReviewWorkButton(userId, sender);
                    break;

                case "📊 Статистика":
                    update.getMessage().setText("/stats");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "👥 Студенты":
                    update.getMessage().setText("/students");
                    commandHandler.handleCommand(update, sender, userStates, userContexts);
                    break;

                case "📚 Курсы":
                    handleCoursesCommand(userId, sender);
                    break;

                case "📈 График":
                    handleGraphCommand(userId, sender);
                    break;

                case "🏆 Рейтинг":
                    handleRatingCommand(userId, sender);
                    break;

                case "🔗 Изменить GitHub":
                    handleChangeGithubCommand(userId, sender, userStates, userContexts);
                    break;

                case "🔔 Уведомления":
                    handleNotificationsCommand(userId, sender);
                    break;

                case "🎯 Цели":
                    handleGoalsCommand(userId, sender);
                    break;

                case "🔙 Назад":
                case "🔙 В меню":
                case "🔙 В главное меню":
                    handleBackToMainMenu(userId, sender);
                    break;

                default:
                    if (text.startsWith("/")) {
                        commandHandler.handleCommand(update, sender, userStates, userContexts);
                    } else {
                        handleDefaultMessage(userId, text, sender);
                    }
            }
        } finally {
            update.getMessage().setText(originalText);
        }
    }

    private void handleAdminPanelButton(Long userId, AbsSender sender) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("* АДМИН-ПАНЕЛЬ*\n\nЗагружаем админские функции...")
                    .parseMode("Markdown")
                    .replyMarkup(commandHandler.getAdminMainKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending admin panel", e);
        }
    }

    private void handleReviewWorkButton(Long userId, AbsSender sender) {
        try {
            String messageText = """
                    *ПРОВЕРИТЬ РАБОТУ*
                    
                    Для проверки работы используйте:
                    
                    *Команды:*
                    /review [ID] - проверить конкретную работу
                    /pending - список работ на проверку
                    
                    *Или выберите работу:*
                    (список будет загружен отдельно)
                    """;

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(messageText)
                    .parseMode("Markdown")
                    .replyMarkup(commandHandler.getAdminMainKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending review work message", e);
        }
    }

    private void handleGraphCommand(Long userId, AbsSender sender) {
        try {
            String graphText = """
                    *График вашего прогресса*
                    
                    Здесь будет график вашей активности по дням.
                    
                    *Функция в разработке:*
                    • Анализ активности за последние 30 дней
                    • График сдачи заданий
                    • Прогноз продуктивности
                    
                    *Временно используйте:*
                    /progress - для просмотра статистики
                    """;

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(graphText)
                    .parseMode("Markdown")
                    .replyMarkup(commandHandler.getProgressKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending graph message", e);
        }
    }

    private void handleRatingCommand(Long userId, AbsSender sender) {
        try {
            // Получаем рейтинг
            List<StudentRatingDto> topStudents = ratingService.getTopStudentsByAverageScore(10);

            // Форматируем таблицу
            String ratingText = formatRatingTable(topStudents, "🏆 ТОП-10 ПО СРЕДНЕМУ БАЛЛУ");

            // Добавляем информацию о позиции текущего студента
            Optional<Student> currentStudent = studentService.findByTelegramId(userId);
            if (currentStudent.isPresent()) {
                int studentRank = ratingService.getStudentRank(currentStudent.get().getId());
                double avgScore = studentService.calculateAverageScore(currentStudent.get().getId());
                int acceptedCount = studentService.countAcceptedSubmissions(currentStudent.get().getId());

                ratingText += "\n\n" + String.format("""
                    👤 *Ваша позиция:* #%d
                    📊 *Ваш средний балл:* %.1f
                    📝 *Принято работ:* %d
                    """, studentRank, avgScore, acceptedCount);
            }

            // Получаем клавиатуру рейтинга
            InlineKeyboardMarkup ratingKeyboard = getRatingInlineKeyboard();

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(ratingText)
                    .parseMode("Markdown")
                    .replyMarkup(ratingKeyboard)
                    .build();

            sender.execute(message);

        } catch (Exception e) {
            log.error("Error handling rating command", e);
            sendErrorMessage(userId, "Ошибка при получении рейтинга", sender);
        }
    }

    private String formatRatingTable(List<StudentRatingDto> topStudents, String title) {
        if (topStudents.isEmpty()) {
            return "🏆 *Рейтинг студентов*\n\n" +
                   "Пока нет данных для рейтинга.\n" +
                   "Сдайте первое задание, чтобы попасть в таблицу!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(title).append("*\n\n");

        sb.append("```\n");
        sb.append(String.format("%-3s %-15s %-6s %-8s %-7s\n",
                "#", "Имя", "Сред.", "Принято", "Прогр."));
        sb.append("─".repeat(45)).append("\n");

        for (StudentRatingDto student : topStudents) {
            sb.append(String.format("%-3s %-15s %-6s %-8d %-7s\n",
                    student.getFormattedRank(),
                    student.getShortName(),
                    student.getFormattedAverageScore(),
                    student.getAssignmentsAccepted(),
                    student.getCompletionRate()
            ));
        }
        sb.append("```\n\n");

        sb.append("*Легенда:*\n");
        sb.append("• # - позиция в рейтинге\n");
        sb.append("• Сред. - средний балл за задания\n");
        sb.append("• Принято - количество принятых работ\n");
        sb.append("• Прогр. - процент принятых от сданных\n");

        return sb.toString();
    }

    private InlineKeyboardMarkup getRatingInlineKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("🏆 По среднему баллу")
                .callbackData("rating:by_score")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("📊 По принятым работам")
                .callbackData("rating:by_submissions")
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("📚 По курсам")
                .callbackData("rating:by_courses")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("🔄 Обновить")
                .callbackData("rating:refresh")
                .build());

        rows.add(row1);
        rows.add(row2);
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    private String formatStudentPosition(Student student, int rank) {
        return String.format("👤 *Ваша позиция:* #%d\n" +
                             "📊 *Ваш средний балл:* %.1f\n" +
                             "📝 *Принято работ:* %d",
                rank,
                studentService.calculateAverageScore(student.getId()),
                studentService.countAcceptedSubmissions(student.getId()));
    }

    private void sendErrorMessage(Long userId, String message, AbsSender sender) {
        try {
            ReplyKeyboardMarkup keyboard = commandHandler.getMainMenuKeyboard(userId);

            SendMessage errorMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("❌ *Ошибка:* " + message + "\n\nПожалуйста, попробуйте позже.")
                    .parseMode("Markdown")
                    .replyMarkup(keyboard)
                    .build();

            sender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending error message to user {}", userId, e);
        }
    }

    private void handleChangeGithubCommand(Long userId, AbsSender sender,
                                           Map<Long, UserState> userStates,
                                           Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text("*Привязка GitHub аккаунта*\n\n" +
                      "Пожалуйста, отправьте ваш *GitHub username* (только логин, без @).\n\n" +
                      "Пример: `ivanov` или `johndoe`\n\n" +
                      "Это необходимо для проверки авторства ваших Pull Request.")
                .parseMode("Markdown")
                .build();

        userStates.put(userId, UserState.WAITING_FOR_GITHUB_USERNAME);

        userContexts.put(userId, TelegramUpdateHandler.UserContext.builder().build());

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending github message", e);
        }
    }

    private void handleNotificationsCommand(Long userId, AbsSender sender) {
        try {
            String notificationsText = """
                    *Настройки уведомлений*
                    
                    Здесь можно настроить получение уведомлений.
                    
                    *Функция в разработке:*
                    • Уведомления о проверке заданий
                    • Напоминания о дедлайнах
                    • Еженедельные отчеты
                    
                    *Текущие настройки:*
                    🔔 Уведомления о проверке: Включены
                    📅 Напоминания: Включены
                    """;

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(notificationsText)
                    .parseMode("Markdown")
                    .replyMarkup(commandHandler.getSettingsKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending notifications message", e);
        }
    }

    private void handleGoalsCommand(Long userId, AbsSender sender) {
        try {
            String goalsText = """
                    *Цели на неделю*
                    
                    Здесь можно установить цели по заданиям.
                    
                    *Функция в разработке:*
                    • Установка целей на неделю
                    • Отслеживание прогресса
                    • Мотивационные напоминания
                    
                    *Текущая цель:*
                    3 задания в неделю
                    """;

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(goalsText)
                    .parseMode("Markdown")
                    .replyMarkup(commandHandler.getSettingsKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending goals message", e);
        }
    }

    private void handleCoursesCommand(Long userId, AbsSender sender) {
        try {
            String coursesText = """
                    *Управление курсами*
                    
                    Здесь администраторы могут управлять курсами.
                    
                    *Функция в разработке:*
                    • Добавление новых курсов
                    • Редактирование существующих
                    • Назначение преподавателей
                    
                    *Временно используйте:*
                    /admin - для админ-панели
                    """;

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(coursesText)
                    .parseMode("Markdown")
                    .replyMarkup(commandHandler.getAdminKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending courses message", e);
        }
    }


    private void handleBackToMainMenu(Long userId, AbsSender sender) {
        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text("Возвращаемся в главное меню")
                .replyMarkup(commandHandler.getMainMenuKeyboard(userId))
                .build();
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }


    private void handleDefaultMessage(Long userId, String text, AbsSender sender) {
        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text("*Я не совсем понял ваше сообщение*\n\n" +
                      "Пожалуйста, используйте кнопки меню или команды:\n" +
                      "• /submit - Сдать задание\n" +
                      "• /progress - Ваш прогресс\n" +
                      "• /help - Помощь")
                .parseMode("Markdown")
                .replyMarkup(commandHandler.getMainMenuKeyboard(userId))
                .build();

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}