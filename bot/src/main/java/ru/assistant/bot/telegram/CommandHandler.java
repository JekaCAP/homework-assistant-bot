package ru.assistant.bot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.enums.UserState;
import ru.assistant.bot.service.AdminService;
import ru.assistant.bot.service.CourseService;
import ru.assistant.bot.service.StudentService;
import ru.assistant.bot.service.SubmissionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CommandHandler
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final CourseService courseService;
    private final StudentService studentService;
    private final SubmissionService submissionService;
    private final AdminService adminService;
    private final KeyboardFactory keyboardFactory;

    public void handleCommand(
            Update update,
            AbsSender sender,
            Map<Long, UserState> userStates,
            Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        Long userId = update.getMessage().getFrom().getId();
        String command = update.getMessage().getText().split(" ")[0];

        log.info("User {} issued command: {}", userId, command);

        if (command.startsWith("/admin") || command.equals("/stats") || command.equals("/pending")) {
            if (!adminService.existsByTelegramId(userId)) {
                handleNotAdmin(update, sender);
                return;
            }
        }

        switch (command) {
            case "/start":
                handleStartCommand(update, sender, userId, userStates);
                break;

            case "/submit":
                handleSubmitCommand(update, sender, userId, userStates, userContexts);
                break;

            case "/progress":
                handleProgressCommand(update, sender, userId);
                break;

            case "/help":
            case "/помощь":
                handleHelpCommand(update, sender);
                break;

            case "/github":
                handleGithubCommand(update, sender, userId, userStates, userContexts);
                break;

            case "/settings":
                handleSettingsCommand(update, sender, userId);
                break;

            // АДМИНСКИЕ КОМАНДЫ
            case "/admin":
                handleAdminCommand(update, sender, userId);
                break;

            case "/stats":
                handleStatsCommand(update, sender, userId);
                break;

            case "/pending":
                handlePendingCommand(update, sender, userId);
                break;

            case "/review":
                handleReviewCommand(update, sender, userId);
                break;

            case "/students":
                handleStudentsCommand(update, sender, userId);
                break;

            default:
                handleUnknownCommand(update, sender);
        }
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        return getMainMenuKeyboard(null);
    }

    public ReplyKeyboardMarkup getAdminMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("⏳ На проверке"));
        row1.add(new KeyboardButton("✅ Проверить работу"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📊 Статистика"));
        row2.add(new KeyboardButton("👥 Студенты"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("📚 Курсы"));
        row3.add(new KeyboardButton("⚙️ Настройки"));

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("🔙 В главное меню"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void handleAdminPanelButton(Long userId, AbsSender sender) {
        try {
            if (!adminService.existsByTelegramId(userId)) {
                SendMessage message = SendMessage.builder()
                        .chatId(userId.toString())
                        .text("❌ У вас нет прав администратора.")
                        .replyMarkup(getMainMenuKeyboard(userId))
                        .build();
                sender.execute(message);
                return;
            }

            String adminPanel = """
                *👨‍💼 АДМИН-ПАНЕЛЬ*
                
                *Доступные функции:*
                • Просмотр и проверка работ
                • Статистика системы
                • Управление студентами и курсами
                
                *Используйте кнопки ниже или команды:*
                /pending - Непроверенные работы
                /stats - Статистика
                /students - Список студентов""";

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(adminPanel)
                    .parseMode("Markdown")
                    .replyMarkup(getAdminMainKeyboard())
                    .build();

            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending admin panel", e);
        }
    }

    private void handleStartCommand(Update update, AbsSender sender, Long userId,
                                    Map<Long, UserState> userStates) {

        Student student = studentService.registerOrUpdateStudent(update.getMessage().getFrom());

        String welcomeMessage = String.format("""
            *Привет, %s!*
            
            Добро пожаловать в *Homework Assistant Bot*
            
            Я помогу тебе:
            • Сдавать домашние задания через GitHub
            • Отслеживать свой прогресс
            • Получать уведомления о проверке
            • Соревноваться с другими студентами
            
            *Доступные команды:*
            /submit - Сдать домашнее задание
            /progress - Мой прогресс и статистика
            /github - Привязать GitHub аккаунт
            /help - Помощь и инструкции
            """, student.getFullName());

        if (adminService.existsByTelegramId(userId)) {
            welcomeMessage += "\n\n*🔐 Вы являетесь администратором*\nДоступна админ-панель с функциями управления";
        }

        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text(welcomeMessage)
                .parseMode("Markdown")
                .replyMarkup(getMainMenuKeyboard(userId))
                .build();

        userStates.put(userId, UserState.IDLE);

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending start message", e);
        }
    }

    private void handleNotAdmin(Update update, AbsSender sender) {
        SendMessage message = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text("У вас нет прав администратора.")
                .build();

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }

    private void handleSubmitCommand(Update update, AbsSender sender, Long userId,
                                     Map<Long, UserState> userStates,
                                     Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        List<Course> activeCourses = courseService.getActiveCourses();

        if (activeCourses.isEmpty()) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("В данный момент нет доступных курсов.")
                    .build();

            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        Student student = studentService.findByTelegramId(userId).orElse(null);
        if (student == null || student.getGithubUsername() == null) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("*Сначала нужно привязать GitHub аккаунт!*\n\n" +
                          "Используй команду /github чтобы привязать свой GitHub.\n" +
                          "Это необходимо для проверки авторства Pull Request.")
                    .parseMode("Markdown")
                    .build();

            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text("*Выберите курс:*")
                .parseMode("Markdown")
                .replyMarkup(keyboardFactory.getCoursesKeyboard(activeCourses))
                .build();

        userStates.put(userId, UserState.WAITING_FOR_COURSE_SELECTION);

        userContexts.put(userId, TelegramUpdateHandler.UserContext.builder().build());

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending courses keyboard", e);
        }
    }

    private void handleProgressCommand(Update update, AbsSender sender, Long userId) {
        try {
            Student student = studentService.findByTelegramId(userId).orElse(null);

            if (student == null) {
                SendMessage message = SendMessage.builder()
                        .chatId(userId.toString())
                        .text("Вы не зарегистрированы в системе. Используйте /start")
                        .build();

                sender.execute(message);
                return;
            }

            Map<String, Object> progressStats = studentService.getStudentProgressStats(userId);

            StringBuilder progressMessage = new StringBuilder();
            progressMessage.append("*Ваша статистика*\n\n");

            progressMessage.append(String.format("*Студент:* %s\n", student.getFullName()));

            if (student.getGithubUsername() != null) {
                progressMessage.append(String.format("*GitHub:* @%s\n", student.getGithubUsername()));
            }

            progressMessage.append(String.format("*Регистрация:* %s\n\n",
                    student.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))));

            List<Map<String, Object>> coursesProgress = (List<Map<String, Object>>) progressStats.get("coursesProgress");

            if (coursesProgress.isEmpty()) {
                progressMessage.append("Вы еще не сдавали задания.\n");
                progressMessage.append("Используйте /submit чтобы начать!");
            } else {
                progressMessage.append("*Прогресс по курсам:*\n\n");

                for (Map<String, Object> courseProgress : coursesProgress) {
                    String courseName = (String) courseProgress.get("courseName");
                    Long submitted = (Long) courseProgress.get("submitted");
                    Long accepted = (Long) courseProgress.get("accepted");
                    Double averageScore = (Double) courseProgress.get("averageScore");

                    progressMessage.append(String.format("""
                    *%s*
                    Сдано: %d
                    Принято: %d
                    Средний балл: %.1f
                    
                    """, courseName, submitted, accepted, averageScore != null ? averageScore : 0.0));
                }

                Long totalSubmitted = (Long) progressStats.get("totalSubmitted");
                Long totalAccepted = (Long) progressStats.get("totalAccepted");
                Double overallAverage = (Double) progressStats.get("overallAverage");

                Number rankNumber = (Number) progressStats.get("rank");
                int rank = rankNumber != null ? rankNumber.intValue() : 0;

                progressMessage.append(String.format("""
                *Общая статистика:*
                Всего сдано: %d заданий
                Принято: %d заданий
                Средний балл: %.1f/100
                
                *Рейтинг:* %d место
                """, totalSubmitted, totalAccepted, overallAverage != null ? overallAverage : 0.0,
                        rank));
            }

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(progressMessage.toString())
                    .parseMode("Markdown")
                    .replyMarkup(getProgressKeyboard())
                    .build();

            sender.execute(message);

        } catch (Exception e) {
            log.error("Error in handleProgressCommand for user {}", userId, e);

            SendMessage errorMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("Ошибка при загрузке статистики. Пожалуйста, попробуйте позже.\n\n" +
                          "Техническая ошибка: " + e.getMessage())
                    .build();

            try {
                sender.execute(errorMessage);
            } catch (TelegramApiException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    private void handleGithubCommand(Update update, AbsSender sender, Long userId,
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

    private void handleSettingsCommand(Update update, AbsSender sender, Long userId) {
        Student student = studentService.findByTelegramId(userId).orElse(null);

        if (student == null) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("Вы не зарегистрированы в системе. Используйте /start")
                    .build();

            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        String settingsMessage = String.format("""
            *Настройки профиля*
            
            *Имя:* %s
            *Telegram ID:* %d
            
            %s
            
            *Уведомления:*
            О проверке: Включены
            Напоминания: Включены
            
            *Дополнительно:*
            Цель на неделю: 3 задания
            Время активности: дневное
            """,
                student.getFullName(),
                student.getTelegramId(),
                student.getGithubUsername() != null
                        ? String.format("*GitHub:* @%s", student.getGithubUsername())
                        : "*GitHub:* Не привязан"
        );

        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text(settingsMessage)
                .parseMode("Markdown")
                .replyMarkup(getSettingsKeyboard())
                .build();

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending settings message", e);
        }
    }

    private void handleHelpCommand(Update update, AbsSender sender) {
        String helpText = """
            *Помощь по использованию бота*
            
            *Основные команды:*
            /start - Начать работу с ботом
            /submit - Сдать домашнее задание
            /progress - Просмотреть свой прогресс
            /github - Привязать GitHub аккаунт
            /settings - Настройки профиля
            
            *Как сдать задание:*
            1. Привяжите GitHub аккаунт (/github)
            2. Нажмите /submit или кнопку "Сдать домашку"
            3. Выберите курс и задание
            4. Отправьте ссылку на Pull Request
            
            *Формат ссылки на PR:*
            ```https://github.com/username/repository/pull/123```
            
            *Требования к Pull Request:*
            • PR должен быть открыт в публичном репозитории
            • Название PR должно содержать номер задания
            • Автор PR должен совпадать с привязанным GitHub
            
            *Система оценивания:*
            • 0-59 баллов: Отклонено
            • 60-79 баллов: На доработку
            • 80-100 баллов: Принято
            
            *Частые вопросы:*
            Q: Что делать, если PR уже закрыт?
            A: Создайте новый PR и отправьте новую ссылку
            
            Q: Можно ли пересдать задание?
            A: Да, если статус "На доработку"
            
            Q: Сколько времени занимает проверка?
            A: Обычно 1-3 рабочих дня
            
            *Техническая поддержка:*
            По вопросам работы бота обращайтесь к администратору
            """;

        SendMessage message = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(helpText)
                .parseMode("Markdown")
                .build();

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending help message", e);
        }
    }

    private void handleUnknownCommand(Update update, AbsSender sender) {
        SendMessage message = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text("Неизвестная команда. Используйте /help для списка команд.")
                .build();

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending unknown command message", e);
        }
    }

    private void handleAdminCommand(Update update, AbsSender sender, Long userId) {
        String adminPanel = """
                *АДМИН-ПАНЕЛЬ*
                
                *Статистика системы:*
                Всего студентов: 150
                Всего сдач: 450
                На проверке: 12
                
                *Быстрые команды:*
                /pending - Непроверенные сдачи
                /stats - Детальная статистика
                /review - Проверить конкретную сдачу
                /students - Список студентов
                
                *Быстрые действия:*
                • Проверяйте задания прямо из админского чата
                • Используйте кнопки под сообщениями
                • Настройте уведомления в /settings
                
                *Активность за сегодня:*
                • 5 новых сдач
                • 3 проверенных задания
                • 2 новых студента
                """;

        SendMessage message = SendMessage.builder()
                .chatId(userId.toString())
                .text(adminPanel)
                .parseMode("Markdown")
                .replyMarkup(getAdminKeyboard())
                .build();

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending admin panel", e);
        }
    }

    private void handleStatsCommand(Update update, AbsSender sender, Long userId) {
        try {
            Map<String, Object> stats = submissionService.getSubmissionStats();
            Long totalStudents = studentService.getActiveStudentsCount();

            List<Student> topStudents = studentService.getTopActiveStudents(5);

            StringBuilder topStudentsText = new StringBuilder();
            if (!topStudents.isEmpty()) {
                topStudentsText.append("\n* Самые активные студенты:*\n");
                for (int i = 0; i < Math.min(topStudents.size(), 5); i++) {
                    Student student = topStudents.get(i);
                    Long submissionsCount = submissionService.getStudentSubmissionsCount(student.getId());

                    String name = student.getFullName() != null ? student.getFullName() : "Неизвестный";
                    String github = student.getGithubUsername() != null ?
                            String.format("(@%s)", student.getGithubUsername()) : "";

                    topStudentsText.append(String.format("%d. %s %s - %d сдач\n",
                            i + 1, name, github, submissionsCount));
                }
            } else {
                topStudentsText.append("\n* Топ студентов:*\nДанные временно недоступны\n");
            }

            String statsMessage = String.format("""
                * СТАТИСТИКА СИСТЕМЫ*
                
                * Общая статистика:*
                 Всего студентов: %d
                 Всего сдач: %d
                 На проверке: %d
                 Принято: %d
                 Отклонено: %d
                 На доработке: %d
                
                * Процент принятия:*
                %.1f%%
                %s
                
                *️ Дополнительная информация:*
                /pending - работы на проверке
                /students - полный список студентов
                /admin - админ-панель
                """,
                    totalStudents,
                    (Long) stats.getOrDefault("totalSubmissions", 0L),
                    (Long) stats.getOrDefault("pendingSubmissions", 0L),
                    (Long) stats.getOrDefault("acceptedSubmissions", 0L),
                    (Long) stats.getOrDefault("rejectedSubmissions", 0L),
                    (Long) stats.getOrDefault("needsRevisionSubmissions", 0L),
                    (Double) stats.getOrDefault("acceptanceRate", 0.0),
                    topStudentsText.toString()
            );

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(statsMessage)
                    .parseMode("Markdown")
                    .build();

            sender.execute(message);
        } catch (Exception e) {
            log.error("Error in stats command", e);

            String fallbackMessage = """
                *📊 СТАТИСТИКА СИСТЕМЫ*
                
                *Статистика временно недоступна*
                
                *Приносим извинения за неудобства!*
                Попробуйте позже или используйте другие команды:
                
                /pending - работы на проверке
                /students - список студентов
                /admin - админ-панель
                """;

            SendMessage errorMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(fallbackMessage)
                    .parseMode("Markdown")
                    .build();

            try {
                sender.execute(errorMessage);
            } catch (TelegramApiException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    private void handlePendingCommand(Update update, AbsSender sender, Long userId) {
        try {
            List<Submission> pendingSubmissions = submissionService.getPendingSubmissions();

            if (pendingSubmissions.isEmpty()) {
                SendMessage message = SendMessage.builder()
                        .chatId(userId.toString())
                        .text("Все задания проверены! Отличная работа!")
                        .build();
                sender.execute(message);
                return;
            }

            StringBuilder messageText = new StringBuilder();
            messageText.append("*ЗАДАНИЯ НА ПРОВЕРКУ*\n\n");

            for (int i = 0; i < Math.min(pendingSubmissions.size(), 10); i++) {
                Submission submission = pendingSubmissions.get(i);
                messageText.append(String.format("""
                        *#%d* | %s
                        *Студент:* %s
                        *Курс:* %s
                        *Задание:* %s
                        *PR:* %s
                        
                        """,
                        submission.getId(),
                        submission.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                        submission.getStudent().getFullName(),
                        submission.getAssignment().getCourse().getName(),
                        submission.getAssignment().getTitle(),
                        submission.getPrUrl()
                ));
            }

            if (pendingSubmissions.size() > 10) {
                messageText.append(String.format("\n...и еще %d заданий", pendingSubmissions.size() - 10));
            }

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(messageText.toString())
                    .parseMode("Markdown")
                    .replyMarkup(getPendingActionsKeyboard(pendingSubmissions))
                    .build();

            sender.execute(message);

        } catch (Exception e) {
            log.error("Error in pending command", e);
            SendMessage errorMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("Ошибка при загрузке непроверенных заданий.")
                    .build();
            try {
                sender.execute(errorMessage);
            } catch (TelegramApiException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    private void handleReviewCommand(Update update, AbsSender sender, Long userId) {
        String[] parts = update.getMessage().getText().split(" ");

        if (parts.length < 2) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("*Использование:* /review <ID_сдачи>\n\n" +
                          "Пример: `/review 123`\n" +
                          "Или используйте кнопки в админском чате.")
                    .parseMode("Markdown")
                    .build();
            try {
                sender.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message", e);
            }
            return;
        }

        try {
            Long submissionId = Long.parseLong(parts[1]);
            Submission submission = submissionService.findById(submissionId).orElse(null);

            if (submission == null) {
                SendMessage message = SendMessage.builder()
                        .chatId(userId.toString())
                        .text("Сдача с ID " + submissionId + " не найдена.")
                        .build();
                sender.execute(message);
                return;
            }

            String messageText = String.format("""
                    *СДАЧА #%d*
                    
                    *Студент:* %s (@%s)
                    *GitHub:* @%s
                    
                    *Курс:* %s
                    *Задание:* %s
                    
                    *PR:* %s
                    *Сдано:* %s
                    *Статус:* %s
                    
                    Выберите оценку:
                    """,
                    submission.getId(),
                    submission.getStudent().getFullName(),
                    submission.getStudent().getTelegramUsername(),
                    submission.getStudent().getGithubUsername(),
                    submission.getAssignment().getCourse().getName(),
                    submission.getAssignment().getTitle(),
                    submission.getPrUrl(),
                    submission.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    submission.getStatus().getDisplayName()
            );

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(Arrays.asList(
                            Arrays.asList(
                                    InlineKeyboardButton.builder()
                                            .text("✅ 100 баллов")
                                            .callbackData("review_" + submissionId + "_100")
                                            .build(),
                                    InlineKeyboardButton.builder()
                                            .text("⚠️ 50 баллов")
                                            .callbackData("review_" + submissionId + "_50")
                                            .build(),
                                    InlineKeyboardButton.builder()
                                            .text("❌ 0 баллов")
                                            .callbackData("review_" + submissionId + "_0")
                                            .build()
                            ),
                            Arrays.asList(
                                    InlineKeyboardButton.builder()
                                            .text("🔗 Открыть PR")
                                            .url(submission.getPrUrl())
                                            .build(),
                                    InlineKeyboardButton.builder()
                                            .text("👤 Профиль студента")
                                            .callbackData("student_" + submission.getStudent().getId())
                                            .build()
                            )
                    ))
                    .build();

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(messageText)
                    .parseMode("Markdown")
                    .replyMarkup(keyboard)
                    .build();

            sender.execute(message);

        } catch (NumberFormatException e) {
            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("Неверный формат ID. Используйте число.")
                    .build();
            try {
                sender.execute(message);
            } catch (TelegramApiException ex) {
                log.error("Error sending message", ex);
            }
        } catch (Exception e) {
            log.error("Error in review command", e);
            SendMessage errorMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text("Ошибка при загрузке сдачи.")
                    .build();
            try {
                sender.execute(errorMessage);
            } catch (TelegramApiException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    private void handleStudentsCommand(Update update, AbsSender sender, Long userId) {
        try {
            Long totalStudents = studentService.getActiveStudentsCount();
            List<Student> topStudents = studentService.getTopActiveStudents(10);

            StringBuilder messageText = new StringBuilder();
            messageText.append("* СТУДЕНТЫ СИСТЕМЫ*\n\n");
            messageText.append(String.format("*Всего активных студентов:* %d\n\n", totalStudents));

            if (!topStudents.isEmpty()) {
                messageText.append("*Последние активные студенты:*\n");

                for (int i = 0; i < Math.min(topStudents.size(), 10); i++) {
                    Student student = topStudents.get(i);
                    String studentName = student.getFullName() != null ? student.getFullName() : "Неизвестный";
                    String github = student.getGithubUsername() != null ?
                            String.format("(@%s)", student.getGithubUsername()) : "";

                    Long submissionsCount = submissionService.getStudentSubmissionsCount(student.getId());

                    messageText.append(String.format("%d. %s %s - %d сдач\n",
                            i + 1, studentName, github, submissionsCount));
                }
            } else {
                messageText.append("*Список студентов:*\nПока нет данных\n");
            }

            messageText.append("\n*Доступные команды:*\n");
            messageText.append("• /stats - общая статистика\n");
            messageText.append("• /pending - работы на проверке\n");

            SendMessage message = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(messageText.toString())
                    .parseMode("Markdown")
                    .build();

            sender.execute(message);
        } catch (Exception e) {
            log.error("Error in students command", e);

            String fallbackMessage = """
                * СТУДЕНТЫ СИСТЕМЫ*
                
                *Данные временно недоступны*
                
                Попробуйте позже или используйте:
                /stats - общая статистика
                /pending - работы на проверке
                """;

            SendMessage errorMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(fallbackMessage)
                    .parseMode("Markdown")
                    .build();

            try {
                sender.execute(errorMessage);
            } catch (TelegramApiException ex) {
                log.error("Error sending error message", ex);
            }
        }
    }

    private InlineKeyboardMarkup getPendingActionsKeyboard(List<Submission> pendingSubmissions) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = 0; i < Math.min(pendingSubmissions.size(), 5); i++) {
            Submission submission = pendingSubmissions.get(i);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text("📋 #" + submission.getId() + " - " + submission.getStudent().getFullName())
                    .callbackData("review_" + submission.getId() + "_check")
                    .build());
            keyboard.add(row);
        }

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        navRow.add(InlineKeyboardButton.builder()
                .text("🔄 Обновить")
                .callbackData("refresh_pending")
                .build());
        navRow.add(InlineKeyboardButton.builder()
                .text("📊 Статистика")
                .callbackData("admin_stats")
                .build());
        keyboard.add(navRow);

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(Long userId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📤 Сдать домашку"));
        row1.add(new KeyboardButton("📊 Мой прогресс"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("⚙️ Настройки"));
        row2.add(new KeyboardButton("❓ Помощь"));

        keyboard.add(row1);
        keyboard.add(row2);

        if (adminService.existsByTelegramId(userId)) {
            KeyboardRow adminRow = new KeyboardRow();
            adminRow.add(new KeyboardButton("👨‍💼 Админ-панель"));
            keyboard.add(adminRow);
        }

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup getProgressKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📊 По курсам"));
        row1.add(new KeyboardButton("📈 График"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🏆 Рейтинг"));
        row2.add(new KeyboardButton("🔙 Назад"));

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup getSettingsKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🔗 Изменить GitHub"));
        row1.add(new KeyboardButton("🔔 Уведомления"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🎯 Цели"));
        row2.add(new KeyboardButton("🔙 Назад"));

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup getAdminKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("⏳ На проверке"));
        row1.add(new KeyboardButton("📊 Статистика"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("👥 Студенты"));
        row2.add(new KeyboardButton("📚 Курсы"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("⚙️ Настройки"));
        row3.add(new KeyboardButton("🔙 В меню"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}