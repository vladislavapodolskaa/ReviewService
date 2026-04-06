package com.example.kurs.repository;

import com.example.kurs.entity.Product;
import com.example.kurs.entity.Review;
import com.example.kurs.entity.StatusReview;
import com.example.kurs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByStatus(StatusReview status);
    List<Review> findByProductId(Integer productId);
    List<Review> findByReviewerLogin(String login);
    List<Review> findByRatingGreaterThanEqual(int rating);
    List<Review> findByProduct_CategoryId(Integer categoryId);
    List<Review> findByProduct_CategoryIdIn(List<Integer> categoryIds);
    List<Review> findByProductIdAndStatus(Integer productId, StatusReview status);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = :status")
    Double calculateAverageRating(@Param("productId") int productId, @Param("status") StatusReview status);
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reviewer WHERE r.status = :status")
    List<Review> findByStatusWithUser(StatusReview status);
    long countByStatus(StatusReview status);
    @Query("SELECT r.text FROM Review r WHERE r.product.id = :productId")
    List<String> findReviewTextsByProductId(@Param("productId") Integer productId);
    @Query("SELECT r.text FROM Review r WHERE r.product.id = :productId AND r.status = :status")
    List<String> findApprovedReviewTextsByProductId(
            @Param("productId") Integer productId,
            @Param("status") StatusReview status
    );
    List<Review> findAllByStatus(StatusReview status);
    List<Review> findAllByOrderByDateDesc();
    List<Review> findAllByStatusOrderByDateDesc(StatusReview status);
    List<Review> findAllByReviewerOrderByDateDesc(User reviewer);
    List<Review> findAllByProductAndStatusOrderByDateDesc(Product product, StatusReview status);
}