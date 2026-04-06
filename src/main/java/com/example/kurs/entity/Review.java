package com.example.kurs.entity;
import jakarta.persistence.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User reviewer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    @Column
    private int rating;
    @Column
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    @Column
    private StatusReview status;
    @Column
    private Sentiment ai_ton;
    @Column(columnDefinition = "TEXT")
    private String ai_review;
    public enum Sentiment {
        POSITIVE,
        NEGATIVE,
        NEUTRAL,
        UNKNOWN
    }
    public Review() {
    }

    public Review(int id, Product product, User reviewer, User moderator, String text, int rating, LocalDate date,
                  StatusReview status, Sentiment ai_ton, String ai_review) {
        this.id = id;
        this.product = product;
        this.reviewer = reviewer;
        this.moderator = moderator;
        this.text = text;
        this.rating = rating;
        this.date = date;
        this.status = status;
        this.ai_ton = ai_ton;
        this.ai_review = ai_review;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
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

    public StatusReview getStatus() {
        return status;
    }

    public void setStatus(StatusReview status) {
        this.status = status;
    }

    public Sentiment getAi_ton() {
        return ai_ton;
    }

    public void setAi_ton(Sentiment ai_ton) {
        this.ai_ton = ai_ton;
    }

    public String getAi_review() {
        return ai_review;
    }

    public void setAi_review(String ai_review) {
        this.ai_review = ai_review;
    }
}
