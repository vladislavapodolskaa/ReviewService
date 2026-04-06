package com.example.kurs.controller;

import com.example.kurs.entity.*;
import com.example.kurs.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.kurs.entity.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
public class HomeController {

    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final UserService userService;
    private final ReviewAIService aiService;

    public HomeController(ReviewService reviewService, CategoryService categoryService,
                          ProductService productService, UserService userService, ReviewAIService aiService) {
        this.reviewService = reviewService;
        this.categoryService = categoryService;
        this.productService = productService;
        this.aiService = aiService;
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        model.addAttribute("categories", categoryService.findAll());
        if (search != null && !search.trim().isEmpty()) {
            List<Product> searchResults = productService.findProductsByNameContaining(search.trim());
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchQuery", search);
            return "search_results";
        }
        model.addAttribute("categories", categoryService.findAll());
        return "home";
    }
    @GetMapping("/category/{id}")
    public String listProductsByCategory(@PathVariable("id") Integer categoryId, Model model) {
        List<Product> products = productService.findProductsByCategoryId(categoryId);

        Optional<Category> category = categoryService.findById(categoryId);
        if(category.isEmpty()){
            return "redirect:/";
        }
        model.addAttribute("products", products);
        model.addAttribute("category", category.get());

        return "products_by_category";
    }
    @GetMapping("/product/{id}")
    public String viewProductDetails(@PathVariable("id") Integer productId, Model model) {
        Optional<Product> product = productService.findById(productId);
        if (product.isEmpty()) {
            return "redirect:/";
        }
        List<Review> approvedReviews = reviewService.findApprovedReviewsByProduct(product.get());

        model.addAttribute("product", product.get());
        model.addAttribute("reviews", approvedReviews);
        //List<String> reviewTexts = reviewService.getApprovedReviewTextsForProduct(productId);
//        String summary = null;
//        if (reviewTexts != null && reviewTexts.size() >= 2) {
//            summary = aiService.summarizeReviews(reviewTexts);
//        }

        //model.addAttribute("aiSummary", summary);

        return "product_details";
    }
    @GetMapping("/product/summery/{id}")
    @ResponseBody
    public String viewProductSummery(@PathVariable("id") Integer productId, Model model) {
        Optional<Product> product = productService.findById(productId);
        if (product.isEmpty()) {
            return "redirect:/";
        }
        List<String> reviewTexts = reviewService.getApprovedReviewTextsForProduct(productId);
        String summary = null;
        if (reviewTexts != null && reviewTexts.size() >= 2) {
            summary = aiService.summarizeReviews(reviewTexts);
        }
//
//        model.addAttribute("aiSummary", summary);
//
        return summary;
    }
}