package com.example.kurs;

import com.example.kurs.entity.Product;
import com.example.kurs.entity.Review;
import com.example.kurs.entity.User;
import com.example.kurs.repository.ReviewRepository;
import com.example.kurs.service.ReviewAIService;
import com.example.kurs.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@Transactional // Обеспечивает откат изменений в БД после каждого теста
public class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    // Заглушка для ИИ-сервиса, чтобы он не делал внешний HTTP-вызов
    @MockitoBean
    private ReviewAIService aiService;

    /**
     * Тест полного цикла: сохранение отзыва, включая анализ тональности, и запись в БД.
     */
    @Test
    void saveReview_shouldSaveSentimentFromAIService() {
        // ARRANGE: Создаем заглушки данных для связи (в реальном проекте они берутся из БД)
        User mockUser = new User();
        mockUser.setId(1); // Предполагаем, что ID 1 существует
        Product mockProduct = new Product();
        mockProduct.setId(1); // Предполагаем, что ID 1 существует

        String reviewText = "Это потрясающе, рекомендую всем!";

        // 1. Мокируем ответ ИИ-сервиса: когда его вызовут, он должен вернуть POSITIVE
        when(aiService.analyzeSentiment(any(String.class))).thenReturn(Review.Sentiment.POSITIVE.name());

        // ACT: Создаем и сохраняем новый отзыв
        Review newReview = new Review();
        newReview.setText(reviewText);
        newReview.setRating(5);
        newReview.setReviewer(mockUser);
        newReview.setProduct(mockProduct);

        Review savedReview = reviewService.saveReview(newReview); // Этот метод должен вызвать aiService

        // ASSERT: Проверяем, что результат ИИ был сохранен в БД
        assertNotNull(savedReview.getId(), "Отзыв должен быть сохранен с ID");

        // Читаем отзыв из репозитория для уверенности
        Review foundReview = reviewRepository.findById(savedReview.getId()).orElse(null);

        // 2. Проверяем, что поле sentiment заполнено результатом мока
        assertEquals(Review.Sentiment.POSITIVE, foundReview.getAi_ton(),
                "Поле тональности в БД должно соответствовать результату ИИ.");
    }
}