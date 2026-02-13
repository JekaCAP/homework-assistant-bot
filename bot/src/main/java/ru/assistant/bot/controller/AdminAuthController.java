package ru.assistant.bot.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.assistant.bot.config.AdminWebAuthService;
import ru.assistant.bot.model.Admin;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminWebAuthService adminWebAuthService;

    @GetMapping("/login")
    public String login(@RequestParam String token,
                        HttpServletRequest request) {

        log.info("Попытка входа с токеном: {}", token);

        Optional<Admin> adminOpt = adminWebAuthService.validateAndConsumeToken(token);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            authenticateUser(admin, request);
            log.info("✅ Успешный вход: {}", admin.getTelegramUsername());
            return "redirect:/admin/dashboard";
        } else {
            log.warn("❌ Недействительный токен: {}", token);
            return "redirect:/login-error";
        }
    }

    @GetMapping("/login-error")
    public String loginError() {
        return "error/login-error";
    }

    private void authenticateUser(Admin admin, HttpServletRequest request) {
        AdminUserDetails userDetails = new AdminUserDetails(admin);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
        session.setAttribute("lastLoginDate", LocalDateTime.now());

        log.info("✅ Аутентификация успешна для: {}", admin.getTelegramUsername());
        log.info("🔑 Authorities: {}", userDetails.getAuthorities());
        log.info("📋 Session ID: {}", session.getId());
    }
}