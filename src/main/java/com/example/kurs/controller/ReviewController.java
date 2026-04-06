package com.example.kurs.controller;

import com.example.kurs.entity.Review;
import com.example.kurs.entity.StatusReview;
import com.example.kurs.entity.User;
import com.example.kurs.repository.ReviewRepository;
import com.example.kurs.service.ProductService;
import com.example.kurs.service.RecaptchaService;
import com.example.kurs.service.ReviewService;
import com.example.kurs.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final ProductService productService;
    private final RecaptchaService recaptchaService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, ProductService productService,
                            RecaptchaService recaptchaService, UserService userService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.recaptchaService = recaptchaService;
        this.userService = userService;
    }
    @PostMapping("/upload-batch")
    @PreAuthorize("hasRole('ADMIN')")
    public String uploadBatch(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пожалуйста, выберите файл для загрузки");
            return "redirect:/admin/batch";
        }
        try {
            reviewService.uploadReviewsFromCsv(file);
            redirectAttributes.addFlashAttribute("successMessage", "Пакетная загрузка успешно завершена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обработке CSV: " + e.getMessage());
        }

        return "redirect:/admin/batch";
    }
    @GetMapping("/submit-review")
    public String showReviewForm(Model model) {
        model.addAttribute("review", new Review());
        model.addAttribute("products", productService.findAll());
        return "submit_review";
    }

    @PostMapping("/submit-review")
    public String submitReview(@ModelAttribute("review") Review review,
                               BindingResult bindingResult,
                               @RequestParam(name = "g-recaptcha-response") String captchaResponse,
                               Authentication authentication,
                               Model model) {

        boolean isCaptchaValid = recaptchaService.validateCaptcha(captchaResponse);
        if (!isCaptchaValid) {
            model.addAttribute("captchaError", "Пожалуйста, подтвердите, что вы не робот!");
            model.addAttribute("products", productService.findAll());
            return "submit_review";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findAll());
            return "submit_review";
        }
        String username = authentication.getName();
        User reviewer = userService.findByLogin(username).get();
        reviewService.saveNewReview(review, reviewer);
        return "redirect:/user/dashboard?success";
    }
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getApprovedReviewByProduct(@PathVariable int id){
        List<Review> reviews = reviewService.findApprovedReviewsByProductId(id);
        if (reviews.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        else{
            return ResponseEntity.ok(reviews);
        }
    }
    @PutMapping("/{review_id}/status")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Review> moderateReview(@PathVariable int review_id, @RequestBody StatusReview statusReview){
        try {
            Review review = reviewService.moderateReview(review_id, statusReview);
            return ResponseEntity.ok(review);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/pending")
    public String listPendingReviews(Model model) {
        List<Review> pendingReviews = reviewService.findPendingReviews();
        model.addAttribute("reviews", pendingReviews);
        model.addAttribute("pageTitle", "Отзывы на модерации");

        return "moderator/pending_reviews";
    }

    @GetMapping("/approve/{id}")
    public String approveReview(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.moderateReview(id, StatusReview.APPROVED);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв одобрен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e);
        }
        return "redirect:/reviews/pending";
    }

    @GetMapping("/reject/{id}")
    public String rejectReview(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.moderateReview(id, StatusReview.REJECTED);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв отклонен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e);
        }
        return "redirect:/reviews/pending";
    }
    @GetMapping("/all")
    public String listAllReviews(
            @RequestParam(required = false) StatusReview status,
            Model model) {

        List<Review> allReviews = reviewService.findAllReviews(status);
        model.addAttribute("reviews", allReviews);
        model.addAttribute("currentStatus", status);
        model.addAttribute("allStatuses", StatusReview.values());

        return "moderator/all_reviews";
    }
}
