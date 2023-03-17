package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Category;
import com.example.app.repository.CategoryRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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
}
