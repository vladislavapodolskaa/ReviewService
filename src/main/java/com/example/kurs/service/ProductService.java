package com.example.kurs.service;

import com.example.kurs.entity.Category;
import com.example.kurs.entity.Product;
import com.example.kurs.entity.Review;
import com.example.kurs.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.PanelUI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }
    public Product save(Product product) throws Exception {
        if (categoryService.findById(product.getCategory().getId()).isEmpty()){
            throw new Exception("Такой категории не существует");
        }
        productRepository.save(product);
        return product;
    }
    public Product updateById(int id, Product product) throws Exception {
        if (productRepository.findById(id).isEmpty()){
            throw new Exception("Такого продукта не существует");
        }
        Product existProduct = productRepository.findById(id).get();
        existProduct.setName(product.getName());
        existProduct.setReviews(product.getReviews());
        if (categoryService.findById(product.getCategory().getId()).isEmpty()){
            throw new Exception("Такой категории не существует");
        }
        existProduct.setCategory(product.getCategory());
        existProduct.setDescription(product.getDescription());
        existProduct.setAverageRating(product.getAverageRating());
        return existProduct;
    }
    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findByCategoryIdOrDescendants(int id) {
        List<Category> descendants = categoryService.findAllDescendantsById(id);
        if (descendants.isEmpty()) {
            return List.of();
        }
        List<Integer> categoryIds = descendants.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        return productRepository.findByCategoryIdIn(categoryIds);
    }
    public List<Product> findProductsByNameContaining(String searchName) {
        return productRepository.findByNameContainingIgnoreCase(searchName);
    }
    public List<Product> findProductsByCategoryId(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
}
