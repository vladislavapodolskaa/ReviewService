package com.example.kurs.controller;

import com.example.kurs.entity.Product;
import com.example.kurs.entity.Review;
import com.example.kurs.entity.Role;
import com.example.kurs.entity.User;
import com.example.kurs.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ViewController {
    private final UserService userService;
    private final ReviewService reviewService;
    private final RecaptchaService recaptchaService;

    public ViewController(UserService userService, ReviewService reviewService,
                          RecaptchaService recaptchaService) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.recaptchaService = recaptchaService;
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        model.addAttribute("currentUserName", currentUserName);
        return "admin/dashboard";
    }
    @GetMapping("/moderator/dashboard")
    @PreAuthorize("hasRole('MODERATOR')")
    public String moderatorDashboard() {
        return "moderator/dashboard";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("users", userService.findAllUsers());
        return "admin/user_management";
    }
    @GetMapping("/admin/users/change-role/{userId}/{newRole}")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserRole(@PathVariable int userId, @PathVariable String newRole, RedirectAttributes redirectAttributes) {

        try {
             String currentRoleName = userService.findById(userId).orElseThrow().getRole().name();

            if (currentRoleName.equals(newRole)) {
                redirectAttributes.addFlashAttribute("adminError",
                        "У пользователя с ID " + userId + " уже установлена роль " + newRole + ".");
            } else {
                userService.updateUserRole(userId, Role.valueOf(newRole));
                redirectAttributes.addFlashAttribute("adminSuccess",
                    "Роль пользователя с ID " + userId + " успешно изменена на " + newRole + ".");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("adminError", "Ошибка при изменении роли: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
    @GetMapping("user/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByLogin(username);
        if (currentUser.isPresent()) {
            List<Review> userReviews = reviewService.findReviewsByUser(currentUser.get());

            model.addAttribute("username", username);
            model.addAttribute("reviews", userReviews);

            return "user/dashboard";
        }
        else {
            return "redirect:/logout";
        }
    }
    @GetMapping("/admin/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public String showBatchPage() {
        return "admin/batch";
    }
}