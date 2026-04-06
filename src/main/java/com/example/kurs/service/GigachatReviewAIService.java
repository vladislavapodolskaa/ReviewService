package com.example.kurs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GigachatReviewAIService implements ReviewAIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.token.uri}")
    private String tokenUri;
    @Value("${ai.scope}")
    private String scope;
    @Value("${ai.model}")
    private String model;
    private String accessToken;
    private long tokenExpiresAt;

    public GigachatReviewAIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        try {
            // Создаем контекст, который разрешает любые сертификаты
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

            this.webClient = webClientBuilder
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка настройки SSL", e);
        }
        this.objectMapper = objectMapper;
    }

    private void getAccessToken(){
        String response = webClient.post()
                .uri(tokenUri)
                .header("Authorization", "Basic " + apiKey)
                .header("RqUID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("scope="+scope)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            JsonNode json = objectMapper.readTree(response);
            tokenExpiresAt = json.path("expires_at").asLong();
            accessToken = json.path("access_token").asText();
        }
        catch (Exception e){
            System.err.println("Ошибка получения токена: " + e.getMessage());
        }
    }

    @Override
    public String analyzeSentiment(String reviewText) {
        String systemPrompt = "Ты — эксперт по анализу тональности. Твой ответ должен содержать только ОДНО слово: POSITIVE, NEGATIVE или NEUTRAL.";
        String userPrompt = "Определи тональность этого отзыва: \"" + reviewText + "\"";

        return callAiApi(systemPrompt, userPrompt);
    }

    private String callAiApi(String systemPrompt, String userPrompt) {
        Object requestBody = buildRequestBody(systemPrompt, userPrompt);
        if (accessToken == null || System.currentTimeMillis() > (tokenExpiresAt - 60000)) {
            getAccessToken();
        }

        String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .bodyValue(buildRequestBody(systemPrompt, userPrompt))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return parseResponse(response);
    }

    private Object buildRequestBody(String systemPrompt, String userPrompt) {
        return new Object() {
            public final String model = GigachatReviewAIService.this.model;
            public final Object[] messages = new Object[]{
                    new Object() {
                        public final String role = "system";
                        public final String content = systemPrompt;
                    },
                    new Object() {
                        public final String role = "user";
                        public final String content = userPrompt;
                    }
            };
        };
    }

    private String parseResponse(String rawJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode choicesNode = rootNode.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.path("message");
                return messageNode.path("content").asText();
            }
            System.err.println("Ошибка парсинга JSON: не найдено поле content.");
            return "UNKNOWN_ERROR_PARSING_RESPONSE";

        } catch (IOException e) {
            System.err.println("Ошибка чтения JSON-ответа: " + e.getMessage());
            return "UNKNOWN_ERROR_READING_JSON";
        }
    }
    @Override
    public String summarizeReviews(List<String> reviewTexts) {
        if (reviewTexts == null || reviewTexts.isEmpty()) {
            return "Недостаточно отзывов для создания обзора.";
        }
        String combinedText = reviewTexts.stream()
                .collect(Collectors.joining("\n---\n"));
        String systemPrompt = "Ты — профессиональный аналитик. Создай краткий, объективный обзор длиной не более 100 слов на основе представленных отзывов. Обзор должен быть нейтральным и содержать ключевые моменты как положительных, так и отрицательных сторон, если они присутствуют. Не используй вводные фразы вроде 'В обзорах говорится' или 'Обзор следующий:'.";

        String userPrompt = "Суммируй следующие отзывы:\n" + combinedText;
        try {
            return callAiApi(systemPrompt, userPrompt);
        } catch (Exception e) {
            System.err.println("Ошибка при вызове API для суммаризации: " + e.getMessage());
            return "Ошибка генерации обзора ИИ.";
        }
    }
}