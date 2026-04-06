package com.example.kurs.controller;

import com.example.kurs.entity.Role;
import com.example.kurs.entity.User;
import com.example.kurs.service.RecaptchaService;
import com.example.kurs.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class AuthController {
    private final UserService userService;
    private final RecaptchaService recaptchaService;
    public AuthController(UserService userService, RecaptchaService recaptchaService) {
        this.userService = userService;
        this.recaptchaService = recaptchaService;
    }
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               @RequestParam("g-recaptcha-response") String captchaResponse,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        boolean isCaptchaValid = recaptchaService.validateCaptcha(captchaResponse);

        if (!isCaptchaValid) {
            model.addAttribute("captchaError", "Пожалуйста, подтвердите, что вы не робот!");
            return "register";
        }
        try {
            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация успешна! Войдите, используя свой логин и пароль.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/admin/users/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerAdminUser(
            @ModelAttribute("user") User user,
            @RequestParam("role") String roleName,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            Role assignedRole = Role.valueOf(roleName.toUpperCase());
            if (assignedRole == Role.REVIEWER) {
                throw new IllegalArgumentException("Через админ-панель нельзя регистрировать обычных REVIEWER.");
            }
            userService.registerAdminUser(user, assignedRole);
            redirectAttributes.addFlashAttribute("adminSuccess", "Сотрудник с ролью " + assignedRole.name() + " успешно создан.");
            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("adminError", "Ошибка роли: " + e.getMessage());
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", e.getMessage());
            return "redirect:/admin/users";
        }
    }

}
