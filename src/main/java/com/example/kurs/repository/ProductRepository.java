package com.example.kurs.repository;

import com.example.kurs.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategoryId(int category_id);
    List<Product> findByCategoryIdIn(List<Integer> categoryIds);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findTop10ByOrderByAverageRatingDesc();
    List<Product> findByCategoryId(Integer categoryId);
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    List<Product> searchByKeyword(@Param("searchTerm") String searchTerm);
}

