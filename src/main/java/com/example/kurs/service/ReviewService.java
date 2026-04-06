package com.example.kurs.service;

import com.example.kurs.entity.*;
import com.example.kurs.repository.ReviewRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final UserService userService;
    private final ReviewAIService aiService;

    public ReviewService(ReviewRepository reviewRepository, ProductService productService,
                         UserService userService, ReviewAIService aiService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.userService = userService;
        this.aiService = aiService;
    }
    public Review saveReview(Review review) {
        String sentimentResult = aiService.analyzeSentiment(review.getText());
        try {
            Review.Sentiment sentiment = Review.Sentiment.valueOf(sentimentResult.toUpperCase());
            review.setAi_ton(sentiment);
        } catch (IllegalArgumentException e) {
            review.setAi_ton(Review.Sentiment.UNKNOWN);
        }
        return reviewRepository.save(review);
    }
    public List<String> getReviewTextsForProduct(Integer productId) {
        return reviewRepository.findReviewTextsByProductId(productId);
    }
    public List<String> getApprovedReviewTextsForProduct(Integer productId) {
        return reviewRepository.findApprovedReviewTextsByProductId(productId, StatusReview.APPROVED);
    }

    public void updateProductAverageRating(int product_id) throws Exception {
        Double averageRating = reviewRepository.calculateAverageRating(product_id, StatusReview.APPROVED);
        Optional<Product> trying = productService.findById(product_id);
        if (trying.isEmpty()) {
            throw new Exception("Продукт не найден");
        }
        Product product = trying.get();
        product.setAverageRating(averageRating != null ? averageRating : 0.0);
        productService.save(product);
    }
    @Transactional
    public void uploadReviewsFromCsv(MultipartFile file) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<ReviewDto> csvToBean = new CsvToBeanBuilder<ReviewDto>(reader)
                    .withType(ReviewDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ReviewDto> dtos = csvToBean.parse();
            List<Review> reviewsToSave = new ArrayList<>();

            for (ReviewDto dto : dtos) {
                Review review = new Review();
                review.setText(dto.getText());
                review.setRating(dto.getRating());
                review.setAi_review(dto.getAi_review());
                review.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
                if (dto.getAi_ton() != null) {
                    try {
                        review.setAi_ton(Review.Sentiment.valueOf(dto.getAi_ton().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        review.setAi_ton(Review.Sentiment.UNKNOWN);
                    }
                }
                productService.findById(dto.getProductId()).ifPresent(review::setProduct);
                userService.findById(dto.getUserId()).ifPresent(review::setReviewer);
                review.setStatus(StatusReview.APPROVED);
                reviewsToSave.add(review);
            }
            reviewRepository.saveAll(reviewsToSave);
            Set<Integer> productIds = reviewsToSave.stream()
                    .map(r -> r.getProduct().getId())
                    .collect(Collectors.toSet());

            for (Integer productId : productIds) {
                updateProductAverageRating(productId);
            }
        }
    }

    @Transactional
    public Review submitNewReview(Review review, String login) throws Exception {
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new Exception("Пользователь не найден"));
        review.setStatus(StatusReview.PENDING);
        Review savedReview = saveReview(review);
        return savedReview;
    }

    @Transactional
    public Review moderateReview(Integer reviewId, StatusReview newStatus) throws Exception {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв с ID " + reviewId + " не найден."));
        StatusReview oldStatus = review.getStatus();
        review.setStatus(newStatus);
        Review updatedReview = reviewRepository.save(review);
        Integer productId = review.getProduct().getId();

        if (newStatus.equals(StatusReview.APPROVED) || oldStatus.equals(StatusReview.APPROVED)) {
            updateProductAverageRating(productId);
        }
        return updatedReview;
    }

    public List<Review> findApprovedReviewsByProductId(Integer product_id) {
        return reviewRepository.findByProductIdAndStatus(product_id, StatusReview.APPROVED);
    }

    public List<Review> findPendingReviews() {
        return reviewRepository.findByStatusWithUser(StatusReview.PENDING);
    }

    public List<Review> findAllReviews(StatusReview status) {
        if (Objects.isNull(status)) {
            return reviewRepository.findAllByOrderByDateDesc();
        } else {
            return reviewRepository.findAllByStatusOrderByDateDesc(status);
        }
    }
    public Map<Product, List<Review>> getApprovedReviewsGroupedByProduct() {
        List<Review> approvedReviews = reviewRepository.findAllByStatusOrderByDateDesc(StatusReview.APPROVED);
        return approvedReviews.stream()
                .collect(Collectors.groupingBy(Review::getProduct));
    }
    public List<Review> findReviewsByUser(User user) {
        return reviewRepository.findAllByReviewerOrderByDateDesc(user);
    }
    @Transactional
    public void saveNewReview(Review review, User reviewer) {
        review.setReviewer(reviewer);
        review.setStatus(StatusReview.PENDING);
        review.setDate(LocalDate.now());
        saveReview(review);
    }
    public List<Review> findApprovedReviewsByProduct(Product product) {
        return reviewRepository.findAllByProductAndStatusOrderByDateDesc(product, StatusReview.APPROVED);
    }
}