package com.example.kurs.service;

import com.example.kurs.entity.Category;
import com.example.kurs.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Optional<Category> findById(int id){
        return categoryRepository.findById(id);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    @Transactional
    public Category save(Category categoryFromForm) {
        if (categoryFromForm.getId() != null) {
            Category existingCategory = categoryRepository.findById(categoryFromForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + categoryFromForm.getId()));
            existingCategory.setName(categoryFromForm.getName());
            categoryRepository.save(existingCategory);
            return existingCategory;
        } else {
            if (categoryFromForm.getParent() != null && categoryFromForm.getParent().getId() == null) {
                categoryFromForm.setParent(null);
            }
            categoryRepository.save(categoryFromForm);
            return categoryFromForm;
        }
    }
    @Transactional
    public void deleteById(int id){
        categoryRepository.deleteById(id);
    }
    public Category updateById(int id, Category category) throws Exception {
        if (categoryRepository.findById(id).isEmpty()){
            throw new Exception("Такой категории не существует");
        }
        if (categoryRepository.findById(category.getParent().getId()).isEmpty()){
            throw new Exception("Такой родительской категории не существует");
        }
        Category existCategory = categoryRepository.findById(id).get();
        existCategory.setName(category.getName());
        existCategory.setParent(category.getParent());
        return existCategory;
    }
    public List<Category> findAllDescendantsById(int id){
        return categoryRepository.findAllDescendants(id);
    }
    public List<Category> findAllParentsById(int id){
        return categoryRepository.findByParentId(id);
    }
    public List<Category> findRootCategories(){
        return categoryRepository.findAllByParentIsNull();
    }
    public List<Category> findAllLeaf(){
        return categoryRepository.findCategoriesWithoutChildren();
    }
}
