package ru.assistant.bot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.dto.AssignmentWithCourseDto;
import ru.assistant.bot.model.enums.SubmissionStatus;
import ru.assistant.bot.model.enums.UserState;
import ru.assistant.bot.repository.SubmissionRepository;
import ru.assistant.bot.service.AdminService;
import ru.assistant.bot.service.AssignmentService;
import ru.assistant.bot.service.CourseService;
import ru.assistant.bot.service.StudentService;
import ru.assistant.bot.service.SubmissionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CallbackQueryHandler
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class CallbackQueryHandler {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final StudentService studentService;
    private final SubmissionRepository submissionRepository;
    private final KeyboardFactory keyboardFactory;
    private final AdminService adminService;

    public void handleCallbackQuery(
            Update update,
            AbsSender sender,
            Map<Long, UserState> userStates,
            Map<Long, TelegramUpdateHandler.UserContext> userContexts) {

        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("User {} clicked callback: {}", userId, callbackData);

        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .build();
            sender.execute(answer);

            if (callbackData.startsWith("course_")) {
                handleCourseSelection(userId, chatId, messageId, callbackData, sender, userStates, userContexts);
            } else if (callbackData.startsWith("assignment_")) {
                handleAssignmentSelection(userId, chatId, messageId, callbackData, sender, userStates, userContexts);
            } else if (callbackData.equals("back_to_courses")) {
                handleBackToCourses(userId, chatId, messageId, sender, userStates, userContexts);
            } else if (callbackData.startsWith("review_")) {
                handleReviewAction(userId, chatId, messageId, callbackData, sender);
            } else if (callbackData.equals("cancel")) {
                handleCancel(userId, chatId, messageId, sender, userStates, userContexts);
            } else if (callbackData.startsWith("submission_")) {
                handleSubmissionDetails(userId, chatId, messageId, callbackData, sender);
            }

        } catch (TelegramApiException e) {
            log.error("Error handling callback query", e);
        }
    }

    private void handleCourseSelection(Long userId, Long chatId, Integer messageId, String callbackData,
                                       AbsSender sender, Map<Long, UserState> userStates,
                                       Map<Long, TelegramUpdateHandler.UserContext> userContexts)
            throws TelegramApiException {

        log.info("Handling course selection for user {}, callback: {}", userId, callbackData);

        try {
            Long courseId = Long.parseLong(callbackData.substring("course_".length()));
            Course course = courseService.findById(courseId).orElse(null);

            if (course == null) {
                log.warn("Course not found for id: {}", courseId);
                EditMessageText editMessage = EditMessageText.builder()
                        .chatId(chatId.toString())
                        .messageId(messageId)
                        .text("Курс не найден. Пожалуйста, выберите другой курс.")
                        .build();
                sender.execute(editMessage);
                return;
            }

            log.info("Found course: {} (ID: {})", course.getName(), courseId);

            TelegramUpdateHandler.UserContext context = userContexts.getOrDefault(userId,
                    TelegramUpdateHandler.UserContext.builder().build());
            context.setSelectedCourseId(courseId);
            userContexts.put(userId, context);

            List<Assignment> assignments = assignmentService.getActiveAssignmentsByCourseId(courseId);
            log.info("Found {} assignments for course {}", assignments.size(), courseId);

            if (assignments.isEmpty()) {
                EditMessageText editMessage = EditMessageText.builder()
                        .chatId(chatId.toString())
                        .messageId(messageId)
                        .text(String.format("В курсе *%s* пока нет доступных заданий.", course.getName()))
                        .parseMode("Markdown")
                        .build();
                sender.execute(editMessage);
                return;
            }

            String messageText = String.format("""
                    *Курс:* %s
                    *Доступные задания:*
                    
                    Выберите задание для сдачи:
                    """, course.getName());

            InlineKeyboardMarkup assignmentsKeyboard = keyboardFactory.getAssignmentsKeyboard(assignments);

            if (assignmentsKeyboard == null || assignmentsKeyboard.getKeyboard() == null) {
                log.error("Keyboard is null for assignments: {}", assignments);
                EditMessageText editMessage = EditMessageText.builder()
                        .chatId(chatId.toString())
                        .messageId(messageId)
                        .text("Ошибка при создании клавиатуры. Попробуйте позже.")
                        .build();
                sender.execute(editMessage);
                return;
            }

            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(messageText)
                    .parseMode("Markdown")
                    .replyMarkup(assignmentsKeyboard)
                    .build();

            userStates.put(userId, UserState.WAITING_FOR_ASSIGNMENT_SELECTION);
            sender.execute(editMessage);
            log.info("Successfully sent assignments keyboard to user {}", userId);

        } catch (NumberFormatException e) {
            log.error("Invalid course ID format in callback: {}", callbackData, e);
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text("Ошибка формата данных. Пожалуйста, попробуйте снова.")
                    .build();
            sender.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Telegram API error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in handleCourseSelection: {}", e.getMessage(), e);
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text("Произошла ошибка. Пожалуйста, попробуйте позже.")
                    .build();
            sender.execute(editMessage);
        }
    }

    private void handleAssignmentSelection(Long userId, Long chatId, Integer messageId, String callbackData,
                                           AbsSender sender, Map<Long, UserState> userStates,
                                           Map<Long, TelegramUpdateHandler.UserContext> userContexts)
            throws TelegramApiException {

        Long assignmentId = Long.parseLong(callbackData.substring("assignment_".length()));
        AssignmentWithCourseDto assignmentDto = assignmentService.findByIdWithCourse(assignmentId).orElse(null);

        if (assignmentDto == null || assignmentDto.getCourse() == null) {
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text("Задание не найдено. Пожалуйста, выберите другое задание.")
                    .build();
            sender.execute(editMessage);
            return;
        }

        TelegramUpdateHandler.UserContext context = userContexts.getOrDefault(userId,
                TelegramUpdateHandler.UserContext.builder().build());
        context.setSelectedAssignmentId(assignmentId);
        userContexts.put(userId, context);

        Optional<Submission> existingSubmission = submissionRepository
                .findTopByStudentIdAndAssignmentIdOrderBySubmittedAtDesc(
                        studentService.findByTelegramId(userId).map(Student::getId).orElse(null),
                        assignmentId
                );

        if (existingSubmission.isPresent()) {
            Submission lastSubmission = existingSubmission.get();

            String messageText;
            if (lastSubmission.getStatus() == SubmissionStatus.REJECTED ||
                lastSubmission.getStatus() == SubmissionStatus.NEEDS_REVISION) {

                messageText = String.format("""
                            *У вас есть предыдущая сдача этого задания*
                            
                            *Задание:* %s
                            *Статус:* %s
                            *Оценка:* %s
                            %s
                            
                            *Вы можете пересдать это задание!*
                            
                            Отправьте новую ссылку на PR с исправлениями.
                            """,
                        assignmentDto.getTitle(),
                        lastSubmission.getStatus().getDisplayName(),
                        lastSubmission.getScore() != null ?
                                lastSubmission.getScore() + "/100" : "не оценено",
                        lastSubmission.getReviewerComment() != null ?
                                String.format("*Комментарий:* %s", lastSubmission.getReviewerComment()) : ""
                );
            } else {
                messageText = String.format("""
                            *Вы уже сдавали это задание!*
                            
                            *Задание:* %s
                            *Статус:* %s
                            *Оценка:* %s
                            %s
                            
                            *К сожалению, пересдача пока недоступна.*
                            
                            Если хотите пересдать, обратитесь к преподавателю.
                            """,
                        assignmentDto.getTitle(),
                        lastSubmission.getStatus().getDisplayName(),
                        lastSubmission.getScore() != null ?
                                lastSubmission.getScore() + "/100" : "не оценено",
                        lastSubmission.getReviewerComment() != null ?
                                String.format("*Комментарий:* %s", lastSubmission.getReviewerComment()) : ""
                );
            }

            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(messageText)
                    .parseMode("Markdown")
                    .build();

            if (lastSubmission.getStatus() == SubmissionStatus.REJECTED ||
                lastSubmission.getStatus() == SubmissionStatus.NEEDS_REVISION) {
                userStates.put(userId, UserState.WAITING_FOR_PR_LINK);
            } else {
                userStates.put(userId, UserState.IDLE);
            }

            sender.execute(editMessage);
            return;
        }

        // 🔴 Новая сдача - обычный процесс
        String messageText = String.format("""
                    *Вы выбрали задание:*
                    
                    *Курс:* %s
                    *Задание #%d:* %s
                    %s
                    *Макс. балл:* %d
                    %s
                    
                    *Теперь отправьте ссылку на ваш Pull Request*
                    
                    *Формат ссылки:*
                    ```https://github.com/username/repository/pull/123```
                    
                    *Требования к PR:*
                    • PR должен быть открыт
                    • Автор PR должен совпадать с вашим GitHub
                    • В названии укажите номер задания
                    """,
                assignmentDto.getCourse().getName(),
                assignmentDto.getNumber(),
                assignmentDto.getTitle(),
                assignmentDto.getDescription() != null ?
                        String.format("*Описание:* %s\n", assignmentDto.getDescription()) : "",
                assignmentDto.getMaxScore(),
                assignmentDto.getDeadline() != null ?
                        String.format("*Дедлайн:* %s",
                                assignmentDto.getDeadline().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))) :
                        "*Дедлайн:* не установлен"
        );

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(messageText)
                .parseMode("Markdown")
                .build();

        userStates.put(userId, UserState.WAITING_FOR_PR_LINK);
        sender.execute(editMessage);
    }

    private void handleBackToCourses(Long userId, Long chatId, Integer messageId,
                                     AbsSender sender, Map<Long, UserState> userStates,
                                     Map<Long, TelegramUpdateHandler.UserContext> userContexts)
            throws TelegramApiException {

        TelegramUpdateHandler.UserContext context = userContexts.getOrDefault(userId,
                TelegramUpdateHandler.UserContext.builder().build());
        context.setSelectedAssignmentId(null);
        userContexts.put(userId, context);

        List<Course> activeCourses = courseService.getActiveCourses();
        InlineKeyboardMarkup coursesKeyboard = keyboardFactory.getCoursesKeyboard(activeCourses);

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text("*Выберите курс:*")
                .parseMode("Markdown")
                .replyMarkup(coursesKeyboard)
                .build();

        userStates.put(userId, UserState.WAITING_FOR_COURSE_SELECTION);
        sender.execute(editMessage);
    }

    private void handleReviewAction(Long userId, Long chatId, Integer messageId,
                                    String callbackData, AbsSender sender) {

        String[] parts = callbackData.split("_");
        if (parts.length != 3) {
            log.error("Неверный формат callback data: {}", callbackData);
            return;
        }

        try {
            Long submissionId = Long.parseLong(parts[1]);
            Integer score = Integer.parseInt(parts[2]);

            log.info("Обработка проверки задания ID={}, оценка={}, от пользователя {}",
                    submissionId, score, userId);

            if (!adminService.existsByTelegramId(userId)) {
                log.warn("Пользователь {} не является админом", userId);
                SendMessage message = SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("У вас нет прав для проверки заданий.")
                        .build();
                sender.execute(message);
                return;
            }

            log.info("Пользователь {} является админом, продолжаем проверку", userId);

            String comment = "Проверено через админ-панель";

            Submission updatedSubmission = submissionService.reviewSubmission(
                    submissionId,
                    score,
                    comment
            );

            log.info("Сдача ID={} проверена, оценка={}", submissionId, score);

            String updatedMessage = String.format("""
                        *ЗАДАНИЕ ПРОВЕРЕНО*
                        
                        *ID сдачи:* %d
                        *Оценка:* %d/100
                        *Статус:* %s
                        
                        *Комментарий:* %s
                        *Проверено:* %s
                        """,
                    submissionId,
                    score,
                    getStatusDisplay(score),
                    comment,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );

            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(updatedMessage)
                    .parseMode("Markdown")
                    .replyMarkup(null)
                    .build();

            sender.execute(editMessage);

            log.info("Сообщение обновлено в чате {} (messageId={})", chatId, messageId);

        } catch (NumberFormatException e) {
            log.error("Ошибка парсинга callback data: {}", callbackData, e);
        } catch (TelegramApiException e) {
            log.error("Ошибка Telegram API: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Ошибка обработки review action: {}", e.getMessage(), e);
            try {
                SendMessage errorMessage = SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("Ошибка при проверке задания: " + e.getMessage())
                        .build();
                sender.execute(errorMessage);
            } catch (TelegramApiException ex) {
                log.error("Не удалось отправить сообщение об ошибке", ex);
            }
        }
    }

    private String getStatusDisplay(Integer score) {
        if (score >= 80) return "Принято";
        if (score >= 60) return "Требует доработки";
        return "Отклонено";
    }

    private void handleCancel(Long userId, Long chatId, Integer messageId,
                              AbsSender sender, Map<Long, UserState> userStates,
                              Map<Long, TelegramUpdateHandler.UserContext> userContexts)
            throws TelegramApiException {

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text("Действие отменено.")
                .build();

        userStates.put(userId, UserState.IDLE);
        userContexts.remove(userId);

        sender.execute(editMessage);
    }

    private void handleSubmissionDetails(Long userId, Long chatId, Integer messageId,
                                         String callbackData, AbsSender sender)
            throws TelegramApiException {

        Long submissionId = Long.parseLong(callbackData.substring("submission_".length()));

        Submission submission = submissionService.findByIdWithAllDetails(submissionId).orElse(null);

        if (submission == null) {
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text("Сдача не найдена.")
                    .build();
            sender.execute(editMessage);
            return;
        }

        String studentName = submission.getStudent() != null ?
                submission.getStudent().getFullName() : "Неизвестный студент";

        String courseName = "Неизвестный курс";
        String assignmentTitle = "Неизвестное задание";

        if (submission.getAssignment() != null) {
            assignmentTitle = submission.getAssignment().getTitle() != null ?
                    submission.getAssignment().getTitle() : "Неизвестное задание";

            if (submission.getAssignment().getCourse() != null) {
                courseName = submission.getAssignment().getCourse().getName() != null ?
                        submission.getAssignment().getCourse().getName() : "Неизвестный курс";
            }
        }

        String detailsText = String.format("""
                        *Детали сдачи #%d*
                        
                        *Студент:* %s
                        *Курс:* %s
                        *Задание:* %s
                        
                        *PR:* %s
                        *Статус:* %s
                        %s
                        %s
                        *Сдано:* %s
                        %s
                        """,
                submission.getId(),
                studentName,
                courseName,
                assignmentTitle,
                submission.getPrUrl(),
                submission.getStatus().getDisplayName(),
                submission.getScore() != null ?
                        String.format("*Оценка:* %d/100", submission.getScore()) : "",
                submission.getReviewerComment() != null ?
                        String.format("*Комментарий:* %s", submission.getReviewerComment()) : "",
                submission.getSubmittedAt() != null ?
                        submission.getSubmittedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "Не указано",
                submission.getReviewedAt() != null ?
                        String.format("*Проверено:* %s",
                                submission.getReviewedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))) : ""
        );

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .text(detailsText)
                .parseMode("Markdown")
                .build();

        sender.execute(editMessage);
    }
}