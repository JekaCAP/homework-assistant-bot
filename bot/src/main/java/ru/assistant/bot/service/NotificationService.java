package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.enums.SubmissionStatus;
import ru.assistant.bot.telegram.HomeworkBot;
import ru.assistant.bot.telegram.event.SubmissionCreatedEvent;
import ru.assistant.bot.telegram.event.SubmissionReviewedEvent;
import ru.assistant.bot.util.MessageFormatter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${telegram.bot.admin.chat-id}")
    private String adminChatId;

    @Value("${telegram.bot.admin.notify-on-submission:true}")
    private boolean notifyOnSubmission;

    private final HomeworkBot homeworkBot;
    private final SubmissionService submissionService;
    private final MessageFormatter messageFormatter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleSubmissionCreated(SubmissionCreatedEvent event) {
        log.info("[AFTER_COMMIT] Получено событие о новой сдаче ID={}", event.getSubmissionId());

        try {
            Submission submission = submissionService
                    .findByIdWithAllDetails(event.getSubmissionId())
                    .orElseThrow();

            log.info("Сдача ID={} загружена после коммита. Студент: {}, Задание: {}",
                    submission.getId(),
                    submission.getStudent() != null ? submission.getStudent().getFullName() : "N/A",
                    submission.getAssignment() != null ? submission.getAssignment().getTitle() : "N/A");

            notifyStudentAboutSubmissionSuccess(submission);

            if (notifyOnSubmission && adminChatId != null && !adminChatId.isBlank() && !adminChatId.equals("-1")) {
                notifyAdminsAboutNewSubmission(submission);
            } else {
                log.warn("Уведомления админам отключены или chat-id не настроен");
            }

        } catch (Exception e) {
            log.error("Ошибка обработки события SubmissionCreatedEvent для ID={}: {}",
                    event.getSubmissionId(), e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleSubmissionReviewed(SubmissionReviewedEvent event) {
        log.info("[AFTER_COMMIT] Получено событие о проверке сдачи ID={}", event.getSubmissionId());

        try {
            Submission submission = submissionService
                    .findByIdWithAllDetails(event.getSubmissionId())
                    .orElseThrow(() -> {
                        log.error("Сдача не найдена ID={} после проверки", event.getSubmissionId());
                        return new RuntimeException("Сдача не найдена ID=" + event.getSubmissionId());
                    });

            log.info("Данные сдачи после коммита ID={}: score={}, status={}, reviewedAt={}",
                    submission.getId(),
                    submission.getScore(),
                    submission.getStatus(),
                    submission.getReviewedAt());

            if (submission.getScore() == null) {
                log.error("ОШИБКА: Событие о проверке получено, но оценка НЕ установлена для сдачи ID={}. " +
                          "Это может быть ошибкой в логике.", submission.getId());
                return;
            }

            if (submission.getReviewedAt() == null) {
                log.error("ОШИБКА: Событие о проверке получено, но время проверки не установлено для сдачи ID={}",
                        submission.getId());
                return;
            }

            log.info("Отправляю уведомление студенту о проверке сдачи ID={}", submission.getId());

            notifyStudentAboutReview(submission);

            log.info("Уведомление о проверке успешно обработано для сдачи ID={}", submission.getId());

        } catch (Exception e) {
            log.error("Ошибка обработки события SubmissionReviewedEvent для ID={}: {}",
                    event.getSubmissionId(), e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyAdminsAboutNewSubmission(Submission submission) {
        if (adminChatId == null || adminChatId.isBlank() || adminChatId.equals("-1")) {
            log.error("Chat ID админской группы не настроен: {}", adminChatId);
            return;
        }

        try {
            String telegramUsername = submission.getStudent().getTelegramUsername() != null ?
                    "@" + submission.getStudent().getTelegramUsername() : "не указан";

            String githubUsername = submission.getStudent().getGithubUsername() != null ?
                    "@" + submission.getStudent().getGithubUsername() : "не привязан";

            MessageFormatter.FormattedMessage formattedMessage = messageFormatter.formatPlainText(
                    """
                            НОВАЯ СДАЧА ЗАДАНИЯ
                            
                            Студент: %s
                            Telegram: %s
                            GitHub: %s
                            
                            Курс: %s
                            Задание #%d: %s
                            
                            Pull Request: %s
                            Время сдачи: %s
                            
                            ID сдачи: %d
                            """,
                    submission.getStudent().getFullName(),
                    telegramUsername,
                    githubUsername,
                    submission.getAssignment().getCourse().getName(),
                    submission.getAssignment().getNumber(),
                    submission.getAssignment().getTitle(),
                    submission.getPrUrl(),
                    submission.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    submission.getId()
            );

            InlineKeyboardMarkup keyboard = createGroupActionsKeyboard(submission.getId(), submission.getPrUrl());

            log.info("Отправляю уведомление в админскую группу {}...", adminChatId);

            SendMessage groupMessage = SendMessage.builder()
                    .chatId(adminChatId)
                    .text(formattedMessage.getText())
                    .parseMode(formattedMessage.getParseMode())
                    .replyMarkup(keyboard)
                    .build();

            homeworkBot.execute(groupMessage);
            log.info("Уведомление отправлено в админскую группу для сдачи ID={}",
                    submission.getId());

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки в админскую группу {}: {}", adminChatId, e.getMessage());

            try {
                String simpleMessage = String.format(
                        "НОВАЯ СДАЧА ЗАДАНИЯ\n\nСтудент: %s\nЗадание: %s\nPR: %s\nID: %d",
                        submission.getStudent().getFullName(),
                        submission.getAssignment().getTitle(),
                        submission.getPrUrl(),
                        submission.getId()
                );

                SendMessage simpleGroupMessage = SendMessage.builder()
                        .chatId(adminChatId)
                        .text(simpleMessage)
                        .build();

                homeworkBot.execute(simpleGroupMessage);
                log.info("Отправлено простое сообщение в админскую группу");

            } catch (Exception ex) {
                log.error("Не удалось отправить даже простое сообщение: {}", ex.getMessage());
            }

        } catch (Exception e) {
            log.error("Неожиданная ошибка при отправке уведомления: {}", e.getMessage(), e);
        }
    }

    private void notifyAdminsAboutReview(Submission submission) {
        if (adminChatId == null || adminChatId.isBlank()) return;

        try {
            if (submission.getScore() == null) {
                log.warn("Пропускаем уведомление админам: оценка не установлена для сдачи ID={}",
                        submission.getId());
                return;
            }

            MessageFormatter.FormattedMessage formattedMessage = messageFormatter.formatPlainText(
                    """
                            ЗАДАНИЕ ПРОВЕРЕНО
                            
                            Студент: %s
                            Курс: %s
                            Задание: %s
                            Оценка: %d/100
                            Статус: %s
                            PR: %s
                            """,
                    submission.getStudent().getFullName(),
                    submission.getAssignment().getCourse().getName(),
                    submission.getAssignment().getTitle(),
                    submission.getScore(),
                    submission.getStatus().getDisplayName(),
                    submission.getPrUrl()
            );

            SendMessage groupMessage = SendMessage.builder()
                    .chatId(adminChatId)
                    .text(formattedMessage.getText())
                    .parseMode(formattedMessage.getParseMode())
                    .build();

            homeworkBot.execute(groupMessage);
            log.info("Уведомление о проверке отправлено в админскую группу для сдачи ID={}",
                    submission.getId());

        } catch (Exception e) {
            log.error("Ошибка отправки уведомления о проверке в группу", e);
        }
    }

    public void notifyStudentAboutSubmissionSuccess(Submission submission) {
        try {
            MessageFormatter.FormattedMessage formattedMessage = messageFormatter.formatPlainText(
                    """
                            ЗАДАНИЕ УСПЕШНО СДАНО!
                            
                            Курс: %s
                            Задание: %s
                            PR: %s
                            Статус: На проверке
                            Время сдачи: %s
                            
                            Что дальше?
                            • Отслеживайте статус через /progress
                            • Проверка обычно занимает 1-3 дня
                            • Вы получите уведомление о результате
                            
                            Можете сдать следующее задание!
                            """,
                    submission.getAssignment().getCourse().getName(),
                    submission.getAssignment().getTitle(),
                    submission.getPrUrl(),
                    submission.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            );

            SendMessage studentMessage = SendMessage.builder()
                    .chatId(submission.getStudent().getTelegramId().toString())
                    .text(formattedMessage.getText())
                    .parseMode(formattedMessage.getParseMode())
                    .build();

            homeworkBot.execute(studentMessage);
            log.info("Подтверждение отправлено студенту {} для сдачи ID={}",
                    submission.getStudent().getTelegramId(), submission.getId());

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки подтверждения студенту {}: {}",
                    submission.getStudent().getTelegramId(), e.getMessage());
        }
    }

    public void notifyStudentAboutReview(Submission submission) {
        if (submission.getScore() == null) {
            log.error("ОТКАЗЫВАЕМСЯ отправлять уведомление студенту: оценка null для сдачи ID={}",
                    submission.getId());
            return;
        }

        if (submission.getReviewedAt() == null) {
            log.warn("Пропускаем уведомление: время проверки не установлено для сдачи ID={}",
                    submission.getId());
            return;
        }

        try {
            String courseName = submission.getAssignment() != null &&
                                submission.getAssignment().getCourse() != null ?
                    submission.getAssignment().getCourse().getName() : "Неизвестный курс";

            String assignmentTitle = submission.getAssignment() != null ?
                    submission.getAssignment().getTitle() : "Неизвестное задание";

            String comment = submission.getReviewerComment() != null &&
                             !submission.getReviewerComment().isEmpty() ?
                    submission.getReviewerComment() : "Без комментария";

            MessageFormatter.FormattedMessage formattedMessage = messageFormatter.formatPlainText(
                    """
                            ВАШЕ ЗАДАНИЕ ПРОВЕРЕНО!
                            
                            Курс: %s
                            Задание: %s
                            Оценка: %d/100
                            Статус: %s
                            
                            Комментарий преподавателя:
                            %s
                            
                            Ссылка на PR: %s
                            Время проверки: %s
                            
                            Следующие шаги:
                            %s
                            """,
                    courseName,
                    assignmentTitle,
                    submission.getScore(),
                    submission.getStatus().getDisplayName(),
                    comment,
                    submission.getPrUrl(),
                    submission.getReviewedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    getNextSteps(submission.getStatus())
            );

            SendMessage studentMessage = SendMessage.builder()
                    .chatId(submission.getStudent().getTelegramId().toString())
                    .text(formattedMessage.getText())
                    .parseMode(formattedMessage.getParseMode())
                    .build();

            homeworkBot.execute(studentMessage);
            log.info("Результат проверки отправлен студенту {} для сдачи ID={} (оценка: {}, статус: {})",
                    submission.getStudent().getTelegramId(), submission.getId(),
                    submission.getScore(), submission.getStatus());

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки результата студенту {}: {}",
                    submission.getStudent().getTelegramId(), e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при отправке уведомления студенту {}: {}",
                    submission.getStudent().getTelegramId(), e.getMessage(), e);
        }
    }

    private String getNextSteps(SubmissionStatus status) {
        return switch (status) {
            case ACCEPTED -> "• Отличная работа! Можете приступать к следующему заданию\n" +
                             "• Продолжайте в том же духе!";
            case NEEDS_REVISION -> "• Исправьте замечания в том же PR\n" +
                                   "• Или создайте новый PR и отправьте ссылку через /submit";
            case REJECTED -> "• Пересмотрите материал задания\n" +
                             "• Обратитесь за помощи к преподавателю";
            default -> "• Ожидайте проверки\n" +
                       "• Обычно проверка занимает 1-3 дня";
        };
    }

    private InlineKeyboardMarkup createGroupActionsKeyboard(Long submissionId, String prUrl) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> reviewRow = new ArrayList<>();
        reviewRow.add(InlineKeyboardButton.builder()
                .text("✅ 100 баллов")
                .callbackData("review_" + submissionId + "_100")
                .build());
        reviewRow.add(InlineKeyboardButton.builder()
                .text("⚠️ 70 баллов")
                .callbackData("review_" + submissionId + "_70")
                .build());
        reviewRow.add(InlineKeyboardButton.builder()
                .text("❌ 0 баллов")
                .callbackData("review_" + submissionId + "_0")
                .build());
        rows.add(reviewRow);

        List<InlineKeyboardButton> actionsRow = new ArrayList<>();
        actionsRow.add(InlineKeyboardButton.builder()
                .text("🔗 Открыть PR")
                .url(prUrl)
                .build());
        actionsRow.add(InlineKeyboardButton.builder()
                .text("📋 Подробнее")
                .callbackData("details_" + submissionId)
                .build());
        rows.add(actionsRow);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}