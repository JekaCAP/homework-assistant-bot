package ru.assistant.bot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.assistant.bot.model.dto.AssignmentStatsDto;
import ru.assistant.bot.model.dto.request.AssignmentCreateRequest;
import ru.assistant.bot.model.dto.request.AssignmentUpdateRequest;
import ru.assistant.bot.model.dto.response.AssignmentResponse;
import ru.assistant.bot.model.dto.AssignmentWithCourseDto;
import ru.assistant.bot.service.AssignmentService;
import ru.assistant.bot.service.CourseService;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/assignments")
@RequiredArgsConstructor
public class AssignmentAdminController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;

    // ========== СПИСОК ЗАДАНИЙ ==========

    /**
     * Список всех заданий (с опциональной фильтрацией по курсу)
     */
    @GetMapping
    public String listAssignments(
            @RequestParam(required = false) Long courseId,
            Model model) {

        List<AssignmentResponse> assignments;
        String title;

        if (courseId != null) {
            // Задания конкретного курса
            assignments = assignmentService.getByCourse(courseId);
            var course = courseService.getCourseResponse(courseId);
            model.addAttribute("course", course);
            title = "Задания курса " + course.getName();
        } else {
            // Все задания
            assignments = assignmentService.getAll();
            title = "Все задания";
        }

        model.addAttribute("assignments", assignments);
        model.addAttribute("title", title);

        return "admin/assignments/list";
    }


    /**
     * Детальный просмотр задания
     */
    @GetMapping("/{id}")
    public String viewAssignment(@PathVariable Long id, Model model) {
        try {
            AssignmentWithCourseDto assignment = assignmentService.getByIdWithCourse(id);
            AssignmentStatsDto stats = assignmentService.getStats(id);

            model.addAttribute("assignment", assignment);
            model.addAttribute("stats", stats);
            model.addAttribute("title", "Задание #" + assignment.getNumber() + ": " + assignment.getTitle());

            return "admin/assignments/view";

        } catch (Exception e) {
            log.error("Ошибка при загрузке задания {}", id, e);
            return "redirect:/admin/assignments?error=notfound";
        }
    }

    // ========== СОЗДАНИЕ ЗАДАНИЯ ==========

    /**
     * Форма создания нового задания
     */
    @GetMapping("/new")
    public String newAssignmentForm(
            @RequestParam(required = false) Long courseId,
            Model model) {

        AssignmentCreateRequest request = new AssignmentCreateRequest();
        if (courseId != null) {
            request.setCourseId(courseId);
        }

        model.addAttribute("assignment", request);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("formAction", "/admin/assignments/create");
        model.addAttribute("title", "Создание задания");

        return "admin/assignments/form";
    }

    /**
     * Создание нового задания
     */
    @PostMapping("/create")
    public String createAssignment(
            @Valid @ModelAttribute("assignment") AssignmentCreateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("courses", courseService.getAllCourses());
            model.addAttribute("formAction", "/admin/assignments/create");
            model.addAttribute("title", "Создание задания");
            return "admin/assignments/form";
        }

        try {
            AssignmentResponse assignment = assignmentService.create(request);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Задание '%s' успешно создано", assignment.getTitle()));

            // Редирект на страницу курса
            return "redirect:/admin/courses/" + request.getCourseId();

        } catch (Exception e) {
            log.error("Ошибка при создании задания", e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании задания: " + e.getMessage());

            // Возвращаем на форму с сохранением courseId
            return "redirect:/admin/assignments/new?courseId=" + request.getCourseId();
        }
    }

    // ========== РЕДАКТИРОВАНИЕ ЗАДАНИЯ ==========

    /**
     * Форма редактирования задания
     */
    @GetMapping("/{id}/edit")
    public String editAssignmentForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            AssignmentWithCourseDto assignment = assignmentService.getByIdWithCourse(id);

            // Создаем request объект для формы
            AssignmentUpdateRequest updateRequest = new AssignmentUpdateRequest();
            updateRequest.setTitle(assignment.getTitle());
            updateRequest.setDescription(assignment.getDescription());
            updateRequest.setNumber(assignment.getNumber());
            updateRequest.setMaxScore(assignment.getMaxScore());
            updateRequest.setDeadline(assignment.getDeadline());
            // Остальные поля...

            model.addAttribute("assignment", updateRequest);
            model.addAttribute("assignmentId", id);
            model.addAttribute("assignmentNumber", assignment.getNumber());
            model.addAttribute("course", assignment.getCourse());
            model.addAttribute("formAction", "/admin/assignments/" + id + "/update");
            model.addAttribute("title", "Редактирование задания");

            return "admin/assignments/form";

        } catch (Exception e) {
            log.error("Ошибка при загрузке задания для редактирования", e);
            redirectAttributes.addFlashAttribute("error", "Задание не найдено");
            return "redirect:/admin/assignments";
        }
    }

    /**
     * Обновление задания
     */
    @PostMapping("/{id}/update")
    public String updateAssignment(
            @PathVariable Long id,
            @Valid @ModelAttribute("assignment") AssignmentUpdateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("assignmentId", id);
            model.addAttribute("formAction", "/admin/assignments/" + id + "/update");
            model.addAttribute("title", "Редактирование задания");

            // Пытаемся получить информацию о курсе для отображения
            try {
                AssignmentWithCourseDto assignment = assignmentService.getByIdWithCourse(id);
                model.addAttribute("course", assignment.getCourse());
                model.addAttribute("assignmentNumber", assignment.getNumber());
            } catch (Exception ignored) {}

            return "admin/assignments/form";
        }

        try {
            AssignmentResponse assignment = assignmentService.update(id, request);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Задание '%s' успешно обновлено", assignment.getTitle()));

            return "redirect:/admin/assignments/" + id;

        } catch (Exception e) {
            log.error("Ошибка при обновлении задания", e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при обновлении задания: " + e.getMessage());
            return "redirect:/admin/assignments/" + id + "/edit";
        }
    }

    // ========== УПРАВЛЕНИЕ СТАТУСОМ ==========

    /**
     * Переключение активности задания (AJAX)
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        try {
            boolean newStatus = assignmentService.toggleActive(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isActive", newStatus
            ));
        } catch (Exception e) {
            log.error("Ошибка при изменении статуса задания", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // ========== УДАЛЕНИЕ ==========

    /**
     * Удаление задания
     */
    @PostMapping("/{id}/delete")
    public String deleteAssignment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            // Получаем courseId до удаления
            Long courseId = assignmentService.getCourseId(id);
            String title = assignmentService.getById(id).getTitle();

            assignmentService.delete(id);

            redirectAttributes.addFlashAttribute("success",
                    String.format("Задание '%s' успешно удалено", title));

            return "redirect:/admin/courses/" + courseId;

        } catch (Exception e) {
            log.error("Ошибка при удалении задания", e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при удалении задания: " + e.getMessage());
            return "redirect:/admin/assignments";
        }
    }

    // ========== ДУБЛИРОВАНИЕ ЗАДАНИЯ ==========

    /**
     * Дублирование задания (создание копии)
     */
    @PostMapping("/{id}/duplicate")
    public String duplicateAssignment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            AssignmentWithCourseDto original = assignmentService.getByIdWithCourse(id);

            AssignmentCreateRequest request = new AssignmentCreateRequest();
            request.setCourseId(original.getCourse().getId());
            request.setTitle(original.getTitle() + " (копия)");
            request.setDescription(original.getDescription());
            request.setNumber(original.getNumber() + 1);
            request.setMaxScore(original.getMaxScore());
            request.setDeadline(original.getDeadline());

            AssignmentResponse newAssignment = assignmentService.create(request);

            redirectAttributes.addFlashAttribute("success",
                    String.format("Задание скопировано. Создано новое задание: '%s'",
                            newAssignment.getTitle()));

            return "redirect:/admin/assignments/" + newAssignment.getId() + "/edit";

        } catch (Exception e) {
            log.error("Ошибка при дублировании задания", e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при дублировании задания: " + e.getMessage());
            return "redirect:/admin/assignments";
        }
    }

    /**
     * Экспорт заданий курса (для отладки)
     */
    @GetMapping("/export")
    @ResponseBody
    public List<AssignmentResponse> exportAssignments(
            @RequestParam(required = false) Long courseId) {

        if (courseId != null) {
            return assignmentService.getByCourse(courseId);
        }
        return assignmentService.getAll();
    }
}