package com.example.kurs.controller;

import com.example.kurs.entity.Category;
import com.example.kurs.entity.Product;
import com.example.kurs.service.CategoryService;
import com.example.kurs.service.ProductService;
import com.example.kurs.service.ReviewAIService;
import com.example.kurs.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping({"/admin/products", "/moderator/products"})
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;


    public ProductController(ProductService productService, CategoryService categoryService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }
    private String getBaseUrl(String currentPath) {
        if (currentPath.startsWith("/admin")) {
            return "/admin/products";
        }
        return "/moderator/products";
    }
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id){
            return productService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/category/{id}")
    public ResponseEntity<List<Product>> findProductsByCategoryDesc(@PathVariable int id){
        List<Product> products = productService.findByCategoryIdOrDescendants(id);
        if (products.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        else{
            return ResponseEntity.ok(products);
        }
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.save(product);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id, @RequestBody Product product){
        try {
            Product updatedProduct = productService.updateById(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("product", new Product());
        List<Category> allCategories = categoryService.findAll();
        model.addAttribute("categories", allCategories);

        return "admin/product_management";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product, RedirectAttributes redirectAttributes) {
        try {
            if (product.getId() != null) {
                Product existingProduct = productService.findById(product.getId()).orElseThrow();
                existingProduct.setName(product.getName());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setCategory(product.getCategory());
                existingProduct.setImageUrl(product.getImageUrl());
                productService.save(existingProduct);
            } else {
                productService.save(product);
            }
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e);
        }
        return "redirect:/admin/products";
    }
    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
            Optional<Product> product = productService.findById(id);
            if (product.isPresent()) {
                model.addAttribute("product", product.get());
                List<Product> allProducts = productService.findAll();
                model.addAttribute("products", allProducts);
                List<Category> allCategories = categoryService.findAllLeaf();
                model.addAttribute("categories", allCategories);
                return "admin/product_management";
            }
        else{
            redirectAttributes.addFlashAttribute("errorMessage", "Продукт не найден");
            return "redirect:/admin/products";
        }
    }
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id) {
        productService.deleteById(id);
        return "redirect:/admin/products";
    }

}
