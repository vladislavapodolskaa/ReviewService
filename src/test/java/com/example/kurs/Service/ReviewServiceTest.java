package com.example.kurs.Service;

import com.example.kurs.entity.*;
import com.example.kurs.repository.ReviewRepository;
import com.example.kurs.service.ProductService;
import com.example.kurs.service.ReviewAIService;
import com.example.kurs.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ReviewAIService aiService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void testUpdateProductAverageRating() throws Exception {
        // 1. Исходные данные (Given)
        int productId = 1;
        Product product = new Product();
        product.setId(productId);
        product.setAverageRating(0.0);

        // Настраиваем поведение моков
        // Когда репозиторий считает среднее, пусть вернет 4.5
        when(reviewRepository.calculateAverageRating(productId, StatusReview.APPROVED)).thenReturn(4.5);
        // Когда сервис ищет продукт, пусть вернет наш объект
        when(productService.findById(productId)).thenReturn(Optional.of(product));

        // 2. Действие (When)
        reviewService.updateProductAverageRating(productId);

        // 3. Проверка (Then)
        // Проверяем, что в объекте продукта рейтинг изменился на 4.5
        assertEquals(4.5, product.getAverageRating());

        // Проверяем, что productService.save() был вызван, чтобы сохранить изменения в базу
        verify(productService, times(1)).save(product);
    }

    @Test
    void testSaveReviewWithAI() {
        // Given
        Review review = new Review();
        review.setText("Супер!");
        // Имитируем ответ от ИИ
        when(aiService.analyzeSentiment("Супер!")).thenReturn("POSITIVE");

        // When
        reviewService.saveReview(review);

        // Then
        assertEquals(Review.Sentiment.POSITIVE, review.getAi_ton());
        verify(reviewRepository).save(review);
    }
    @Test
    void testUpdateProductAverageRating_ProductNotFound() {
        int productId = 999;
        when(productService.findById(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            reviewService.updateProductAverageRating(productId);
        });

        assertEquals("Продукт не найден", exception.getMessage());
    }
}