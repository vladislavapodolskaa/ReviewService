package com.example.kurs.controller;

import com.example.kurs.service.ReviewAIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    private final ReviewAIService reviewAIService;

    // Автоматическое внедрение вашего сервиса OpenRouterReviewAIService
    public TestController(ReviewAIService reviewAIService) {
        this.reviewAIService = reviewAIService;
    }

    // --- ТЕСТ 1: АНАЛИЗ ТОНАЛЬНОСТИ (УЖЕ ПРОВЕРЕНО) ---
    @GetMapping("/test/ai-sentiment")
    public String testSentimentAnalysis(@RequestParam String text) {
        System.out.println("-> Отправка текста на анализ: " + text);
        try {
            // Вызываем метод, который обращается к OpenRouter
            String sentiment = reviewAIService.analyzeSentiment(text);

            System.out.println("<- Полученный ответ: " + sentiment);

            return "Анализ тональности завершен. Результат ИИ: " + sentiment;

        } catch (Exception e) {
            e.printStackTrace();
            return "ОШИБКА: Не удалось выполнить запрос к AI. Проверьте: " + e.getMessage();
        }
    }

    // --- ТЕСТ 2: СУММАРИЗАЦИЯ (НОВАЯ ПРОВЕРКА) ---
    @GetMapping("/test/ai-summary")
    public String testSummaryAnalysis() {
        // Создаем пример данных, имитируя отзывы из базы данных
        List<String> mockReviews = List.of(
                "Этот продукт просто великолепно! Качество сборки на высоте, и он очень быстро работает. Лучшая покупка в этом году.",
                "Товар пришел с небольшой задержкой, но в целом соответствует описанию. Цена немного завышена, но функции выполняет.",
                "Ужасный дизайн и очень сложный интерфейс. Я не рекомендую его никому. Единственный плюс — быстрая доставка и поддержка."
        );

        System.out.println("-> Отправка " + mockReviews.size() + " отзывов на суммаризацию...");

        try {
            // Вызываем ваш новый метод
            String summary = reviewAIService.summarizeReviews(mockReviews);

            System.out.println("<- Полученный обзор: " + summary);

            return "<h2>✅ Обзор ИИ успешно сгенерирован</h2>"
                    + "<p><strong>Входные данные:</strong> " + mockReviews.size() + " тестовых отзыва.</p>"
                    + "<h3>Результат:</h3>"
                    + "<blockquote style='border-left: 5px solid #007bff; padding-left: 15px;'>"
                    + summary
                    + "</blockquote>";

        } catch (Exception e) {
            e.printStackTrace();
            return "ОШИБКА СУММАРИЗАЦИИ: Не удалось выполнить запрос к AI. Проверьте: " + e.getMessage();
        }
    }
}