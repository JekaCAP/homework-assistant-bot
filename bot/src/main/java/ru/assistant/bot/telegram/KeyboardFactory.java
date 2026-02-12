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
 * Фабрика для создания inline-клавиатур Telegram бота.
 * Генерирует клавиатуры для выбора курсов и заданий.
 *
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Component
public class KeyboardFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    /**
     * Создает клавиатуру для выбора курсов.
     *
     * @param courses список доступных курсов
     * @return InlineKeyboardMarkup с кнопками курсов
     */
    public InlineKeyboardMarkup getCoursesKeyboard(List<Course> courses) {
        log.info("Creating keyboard for {} courses", courses.size());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Course course : courses) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            String buttonText = course.getName();

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

    /**
     * Создает клавиатуру для выбора заданий в курсе.
     *
     * @param assignments список заданий курса
     * @return InlineKeyboardMarkup с кнопками заданий
     */
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
                        buttonText += " (до " + deadline.format(DATE_FORMATTER) + ")";
                    } else {
                        buttonText += " (до " + assignment.getDeadline().toString() + ")";
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
                .text("🔙 К курсам")
                .callbackData("back_to_courses")
                .build());
        rows.add(backRow);

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}