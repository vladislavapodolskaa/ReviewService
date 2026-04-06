package com.example.kurs.service;

import java.util.List;

public interface ReviewAIService {
    String analyzeSentiment(String reviewText);
    String summarizeReviews(List<String> reviewTexts);

}