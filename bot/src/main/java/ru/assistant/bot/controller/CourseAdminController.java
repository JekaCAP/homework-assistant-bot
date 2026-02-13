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
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.dto.request.CourseCreateRequest;
import ru.assistant.bot.model.dto.request.CourseUpdateRequest;
import ru.assistant.bot.model.dto.response.CourseResponse;
import ru.assistant.bot.model.dto.response.CourseStats;
import ru.assistant.bot.model.enums.DifficultyLevel;
import ru.assistant.bot.service.CourseService;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CourseService courseService;

    /**
     * Список всех курсов
     */
    @GetMapping
    public String listCourses(Model model) {
        List<CourseResponse> courses = courseService.getAllCoursesWithStats();

        model.addAttribute("courses", courses);
        model.addAttribute("title", "Управление курсами");

        return "admin/courses/list";
    }

    /**
     * Форма создания нового курса
     */
    @GetMapping("/new")
    public String newCourseForm(Model model) {
        model.addAttribute("course", new CourseCreateRequest());
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("formAction", "/admin/courses/create");
        model.addAttribute("title", "Создание курса");

        return "admin/courses/form";
    }

    /**
     * Создание курса
     */
    @PostMapping("/create")
    public String createCourse(@Valid @ModelAttribute("course") CourseCreateRequest request,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (result.hasErrors()) {
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("formAction", "/admin/courses/create");
            model.addAttribute("title", "Создание курса");
            return "admin/courses/form";
        }

        try {
            Course course = courseService.createCourse(request);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Курс '%s' успешно создан", course.getName()));
            return "redirect:/admin/courses";

        } catch (Exception e) {
            log.error("Ошибка при создании курса", e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании курса: " + e.getMessage());
            return "redirect:/admin/courses/new";
        }
    }

    /**
     * Форма редактирования курса
     */
    @GetMapping("/{id}/edit")
    public String editCourseForm(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Курс не найден"));

            CourseUpdateRequest updateRequest = new CourseUpdateRequest();
            updateRequest.setName(course.getName());
            updateRequest.setDescription(course.getDescription());
            updateRequest.setIcon(course.getIcon());
            updateRequest.setDifficultyLevel(course.getDifficultyLevel() != null ?
                    course.getDifficultyLevel().name() : null);
            updateRequest.setSortOrder(course.getSortOrder());
            updateRequest.setIsActive(course.getIsActive());

            model.addAttribute("course", updateRequest);
            model.addAttribute("courseId", id);
            model.addAttribute("courseCode", course.getCode());
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("formAction", "/admin/courses/" + id + "/update");
            model.addAttribute("title", "Редактирование курса");

            return "admin/courses/form";

        } catch (Exception e) {
            log.error("Ошибка при загрузке курса для редактирования", e);
            redirectAttributes.addFlashAttribute("error", "Курс не найден");
            return "redirect:/admin/courses";
        }
    }

    /**
     * Обновление курса
     */
    @PostMapping("/{id}/update")
    public String updateCourse(@PathVariable Long id,
                               @Valid @ModelAttribute("course") CourseUpdateRequest request,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (result.hasErrors()) {
            model.addAttribute("courseId", id);
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("formAction", "/admin/courses/" + id + "/update");
            model.addAttribute("title", "Редактирование курса");

            try {
                Course course = courseService.findById(id).orElse(null);
                if (course != null) {
                    model.addAttribute("courseCode", course.getCode());
                }
            } catch (Exception ignored) {}

            return "admin/courses/form";
        }

        try {
            Course course = courseService.updateCourse(id, request);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Курс '%s' успешно обновлен", course.getName()));
            return "redirect:/admin/courses";

        } catch (Exception e) {
            log.error("Ошибка при обновлении курса", e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при обновлении курса: " + e.getMessage());
            return "redirect:/admin/courses/" + id + "/edit";
        }
    }

    /**
     * Переключение статуса курса (AJAX)
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleCourseActive(@PathVariable Long id) {
        try {
            boolean newStatus = courseService.toggleCourseActive(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isActive", newStatus
            ));
        } catch (Exception e) {
            log.error("Ошибка при изменении статуса курса", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Удаление курса (AJAX)
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Курс успешно удален"
            ));
        } catch (Exception e) {
            log.error("Ошибка при удалении курса", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Просмотр детальной информации о курсе
     */
    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            CourseStats courseStats = courseService.getCourseStats(id);

            model.addAttribute("course", courseStats);
            model.addAttribute("title", "Просмотр курса");

            return "admin/courses/view";

        } catch (Exception e) {
            log.error("Ошибка при загрузке курса", e);
            redirectAttributes.addFlashAttribute("error", "Курс не найден");
            return "redirect:/admin/courses";
        }
    }
}