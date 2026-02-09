package ru.assistant.bot.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.assistant.bot.service.AdminService;

/**
 * AdminController
 * @author agent
 * @since 03.02.2026
 */
@RestController
public class AdminController {

    private final AdminService adminService;
}