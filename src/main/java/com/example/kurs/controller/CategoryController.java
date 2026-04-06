package com.example.kurs.controller;

import com.example.kurs.entity.Category;
import com.example.kurs.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.processing.Generated;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping
    public String listCategories(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("currentUserName", principal.getName());
        }
        model.addAttribute("categories", categoryService.findAll());
        if (!model.containsAttribute("newCategory")) {
            model.addAttribute("newCategory", new Category());
        }

        return "admin/category_management";
    }
    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories(){
        List<Category> roots = categoryService.findRootCategories();
        return ResponseEntity.ok(roots);
    }
    @GetMapping("/{id}/descendants")
    public ResponseEntity<List<Category>> getAllDescendants(@PathVariable int id){
        List<Category> desc = categoryService.findAllDescendantsById(id);
        if (desc.isEmpty()) {
            if (categoryService.findById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok(desc);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable int id){
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category){
        try{
            Category createdCategory = categoryService.save(category);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category){
        try{
            Category updatedCategory = categoryService.updateById(id, category);
            return ResponseEntity.ok(updatedCategory);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Категория с ID " + id + " успешно удалена.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении категории: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("newCategory") Category category,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.save(category);
            String action = (category.getId() == null) ? "сохранена" : "обновлена";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Категория '" + category.getName() + "' успешно " + action + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newCategory", category);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении категории: " + e.getMessage());
            redirectAttributes.addFlashAttribute("newCategory", category);
        }
        return "redirect:/admin/categories";
    }
    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMessage", "Категория не найдена");
        }
        else {
            model.addAttribute("newCategory", category.get());
            model.addAttribute("categories", categoryService.findAll());
        }
        return "admin/category_management";
    }
    @GetMapping("/add-child/{parentId}")
    public String addChild(@PathVariable Integer parentId, Model model, RedirectAttributes redirectAttributes) {
            Optional<Category> parent = categoryService.findById(parentId);
            if (parent.isPresent()) {
                Category child = new Category();
                child.setParent(parent.get());
                model.addAttribute("newCategory", child);
                model.addAttribute("categories", categoryService.findAll());
                return "admin/category_management";
            }
            else {
                redirectAttributes.addFlashAttribute("errorMessage", "Категория не найдена");
                return "admin/category_management";
            }
    }
}

