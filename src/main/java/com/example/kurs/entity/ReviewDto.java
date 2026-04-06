package com.example.kurs.entity;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.Column;

import java.time.LocalDate;

public class ReviewDto {
    @CsvBindByName(column = "product_id")
    private int productId;

    @CsvBindByName(column = "user_id")
    private int userId;

    @CsvBindByName(column = "text")
    private String text;
    @CsvBindByName(column = "rating")
    private int rating;
    @CsvBindByName(column = "date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate date;
    @CsvBindByName(column = "ai_ton")
    private String ai_ton;

    @CsvBindByName(column = "ai_review")
    private String ai_review;

    public ReviewDto() {
    }

    public ReviewDto(int productId, int userId, String text, int rating, LocalDate date, String ai_ton, String ai_review) {
        this.productId = productId;
        this.userId = userId;
        this.text = text;
        this.rating = rating;
        this.date = date;
        this.ai_ton = ai_ton;
        this.ai_review = ai_review;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getAi_ton() {
        return ai_ton;
    }

    public void setAi_ton(String ai_ton) {
        this.ai_ton = ai_ton;
    }

    public String getAi_review() {
        return ai_review;
    }

    public void setAi_review(String ai_review) {
        this.ai_review = ai_review;
    }
}
