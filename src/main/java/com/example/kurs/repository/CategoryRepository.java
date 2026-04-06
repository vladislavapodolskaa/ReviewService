package com.example.kurs.repository;

import com.example.kurs.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByParentId(int parent_id);
    List<Category> findAllByParentIsNull();

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "    SELECT * FROM categories WHERE id = :categoryId " +
            "    UNION ALL " +
            "    SELECT c.* FROM categories c JOIN category_tree ct ON c.parent_id = ct.id" +
            ") SELECT * FROM category_tree",
            nativeQuery = true)
    List<Category> findAllDescendants(@Param("categoryId") int categoryId);
    @Query("SELECT c.id FROM Category c WHERE c.parent.id = :parentId")
    List<Integer> findIdsByParent(@Param("parentId") Integer parentId);
    @Query("SELECT c FROM Category c WHERE NOT EXISTS " +
            "(SELECT sub FROM Category sub WHERE sub.parent = c)")
    List<Category> findCategoriesWithoutChildren();
}