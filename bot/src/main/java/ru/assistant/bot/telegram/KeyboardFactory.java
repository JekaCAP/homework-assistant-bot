package ru.assistant.bot.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Course;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * KeyboardFactory
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Component
public class KeyboardFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    public InlineKeyboardMarkup getCoursesKeyboard(List<Course> courses) {
        log.info("Creating keyboard for {} courses", courses.size());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Course course : courses) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            String buttonText = course.getIcon() != null ?
                    course.getIcon() + " " + course.getName() :
                    "📚 " + course.getName();

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData("course_" + course.getId())
                    .build();
            row.add(button);
            rows.add(row);
        }

        List<InlineKeyboardButton> cancelRow = new ArrayList<>();
        cancelRow.add(InlineKeyboardButton.builder()
                .text("❌ Отмена")
                .callbackData("cancel")
                .build());
        rows.add(cancelRow);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup getAssignmentsKeyboard(List<Assignment> assignments) {
        log.info("Creating keyboard for {} assignments", assignments.size());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Assignment assignment : assignments) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            String buttonText = assignment.getTitle();

            if (assignment.getDeadline() != null) {
                try {
                    if (assignment.getDeadline() instanceof LocalDateTime) {
                        LocalDateTime deadline = (LocalDateTime) assignment.getDeadline();
                        buttonText += " (📅 " + deadline.format(DATE_FORMATTER) + ")";
                    } else {
                        buttonText += " (📅 " + assignment.getDeadline().toString() + ")";
                    }
                } catch (Exception e) {
                    log.warn("Could not format deadline for assignment {}: {}",
                            assignment.getId(), e.getMessage());
                }
            }

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData("assignment_" + assignment.getId())
                    .build();
            row.add(button);
            rows.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(InlineKeyboardButton.builder()
                .text("⬅️ Назад к курсам")
                .callbackData("back_to_courses")
                .build());
        rows.add(backRow);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup getAdminActionsKeyboard(Long submissionId, String prUrl) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> reviewRow = new ArrayList<>();
        reviewRow.add(InlineKeyboardButton.builder()
                .text("✅ 100")
                .callbackData("review_" + submissionId + "_100")
                .build());
        reviewRow.add(InlineKeyboardButton.builder()
                .text("⚠️ 50")
                .callbackData("review_" + submissionId + "_50")
                .build());
        reviewRow.add(InlineKeyboardButton.builder()
                .text("❌ 0")
                .callbackData("review_" + submissionId + "_0")
                .build());
        rows.add(reviewRow);

        List<InlineKeyboardButton> actionsRow = new ArrayList<>();
        actionsRow.add(InlineKeyboardButton.builder()
                .text("🔗 Открыть PR")
                .url(prUrl)
                .build());
        actionsRow.add(InlineKeyboardButton.builder()
                .text("👤 Профиль студента")
                .callbackData("student_" + submissionId)
                .build());
        rows.add(actionsRow);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup getAdminMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("📋 Непроверенные сдачи")
                .callbackData("admin_unreviewed")
                .build());
        row1.add(InlineKeyboardButton.builder()
                .text("📊 Статистика")
                .callbackData("admin_stats")
                .build());
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("👥 Студенты")
                .callbackData("admin_students")
                .build());
        row2.add(InlineKeyboardButton.builder()
                .text("⚙️ Настройки")
                .callbackData("admin_settings")
                .build());
        rows.add(row2);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}