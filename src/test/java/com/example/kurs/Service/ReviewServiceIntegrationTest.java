package com.example.kurs.Service;

import com.example.kurs.entity.*;
import com.example.kurs.repository.CategoryRepository;
import com.example.kurs.repository.ProductRepository;
import com.example.kurs.repository.ReviewRepository;
import com.example.kurs.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CategoryRepository categoryRepository; // Добавляем репозиторий категорий

    @Test
    void testFullReviewCycleAndRatingUpdate() throws Exception {
        // 1. Сначала создаем категорию (так как продукт не может существовать без неё)
        Category category = new Category();
        category.setName("Электроника");
        category = categoryRepository.save(category);

        // 2. Создаем продукт и привязываем категорию
        Product product = new Product();
        product.setName("Тестовый телефон");
        product.setAverageRating(0.0);
        product.setCategory(category); // ОБЯЗАТЕЛЬНО устанавливаем категорию
        product = productRepository.save(product);

        // 3. Создаем отзыв
        Review review = new Review();
        review.setProduct(product);
        review.setRating(5);
        review.setText("Отличный товар!");
        review.setStatus(StatusReview.APPROVED);
        reviewRepository.save(review);

        // 4. Вызываем метод обновления рейтинга
        reviewService.updateProductAverageRating(product.getId());

        // 5. Проверка
        Product updatedProduct = productRepository.findById(product.getId()).get();
        assertEquals(5.0, updatedProduct.getAverageRating(), "Рейтинг должен обновиться в БД");
    }
}