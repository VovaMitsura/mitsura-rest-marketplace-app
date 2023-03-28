package com.example.app.service;

import com.example.app.controller.dto.CategoryDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.Category;
import com.example.app.repository.CategoryRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Autowired
  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public Category findCategoryByName(String categoryName) {
    Optional<Category> byName = categoryRepository.findByName(categoryName);

    return byName.orElseThrow(
        () -> new NotFoundException(ApplicationExceptionHandler.CATEGORY_NOT_FOUND,
            String.format("category with name: %s not found", categoryName)));
  }

  public Category addCategory(CategoryDTO categoryDTO) {
    Category category = Category.builder()
        .name(categoryDTO.getName())
        .description(categoryDTO.getDescription())
        .build();

    try {
      return categoryRepository.save(category);
    }catch (DataIntegrityViolationException e){
      throw new ResourceConflictException(ApplicationExceptionHandler.DUPLICATE_ENTRY,
          String.format("Category [%s] already exists", categoryDTO.getName()));
    }
  }
}
