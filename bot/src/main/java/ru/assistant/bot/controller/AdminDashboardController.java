package ru.assistant.bot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.assistant.bot.service.CourseService;
import ru.assistant.bot.service.StudentService;
import ru.assistant.bot.service.SubmissionService;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final SubmissionService submissionService;
    private final StudentService studentService;
    private final CourseService courseService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {

        // Статистика
        try {
            model.addAttribute("pendingCount", submissionService.getPendingSubmissionsCount());
            model.addAttribute("studentsCount", studentService.getActiveStudentsCount());
            model.addAttribute("activeCoursesCount", courseService.getActiveCoursesCount());
            model.addAttribute("todayAccepted", submissionService.getTodayAcceptedCount());
            model.addAttribute("recentSubmissions", submissionService.getRecentSubmissions(5));
        } catch (Exception e) {
            log.error("Ошибка статистики", e);
            model.addAttribute("pendingCount", 0);
            model.addAttribute("studentsCount", 0);
            model.addAttribute("activeCoursesCount", 0);
            model.addAttribute("todayAccepted", 0);
            model.addAttribute("recentSubmissions", java.util.Collections.emptyList());
        }

        // 👇 ПРОСТО ВОЗВРАЩАЕМ СТРАНИЦУ - БЕЗ ФРАГМЕНТОВ, БЕЗ LAYOUT
        return "admin/dashboard";
    }

    @GetMapping
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
}