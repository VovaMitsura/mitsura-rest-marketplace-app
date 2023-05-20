package com.example.app.service;

import com.example.app.controller.dto.CategoryDTO;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.Category;
import com.example.app.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CategoryService.class)
class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;

    @MockBean
    CategoryRepository categoryRepository;

    ObjectMapper mapper = new ObjectMapper();
    List<Category> categories;

    @BeforeEach
    void setUp() throws IOException {
        categories = List.of(mapper.readValue(new File("src/test/resources/data/categories.json"),
                Category[].class));
    }

    @Test()
    void findCategoryByNameReturnCategory() {
        Optional<Category> smartphone = categories.stream().filter(category -> category.getName().equals("smartphone"))
                .findFirst();

        Mockito.when(categoryRepository.findByName("smartphone"))
                .thenReturn(smartphone);

        var response = categoryService.findCategoryByName("smartphone");

        Assertions.assertNotNull(response);
        Assertions.assertEquals(smartphone.get(), response);
        Assertions.assertEquals(smartphone.get().getName(), response.getName());
    }

    @Test
    void findNonExistingCategoryThrowException() {
        Mockito.when(categoryRepository.findByName("none"))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(NotFoundException.class, () ->
                categoryService.findCategoryByName("none"));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.CATEGORY_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("category with name: %s not found", "none"), exception.getMessage());
    }

    @Test
    void saveNewCategoryReturnCategory() {
        Category laptop = categories.stream().filter(category -> category.getName().equals("laptop"))
                .findFirst().orElseThrow();

        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(laptop);

        var response = categoryService.addCategory(new CategoryDTO(laptop.getName(), laptop.getDescription()));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(laptop.getName(), response.getName());
        Assertions.assertEquals(laptop.getDescription(), response.getDescription());
        Assertions.assertEquals(laptop.getId(), response.getId());
    }

    @Test
    void saveExistingCategoryThrowException() {
        Category smartphone = categories.stream().filter(category -> category.getName().equals("smartphone"))
                .findFirst().orElseThrow();

        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenThrow(DataIntegrityViolationException.class);

        var exception = Assertions.assertThrows(ResourceConflictException.class, () ->
                categoryService.addCategory(new CategoryDTO(smartphone.getName(), smartphone.getDescription())));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.DUPLICATE_ENTRY, exception.getErrorCode());
        Assertions.assertEquals(String.format("Category [%s] already exists", smartphone.getName()),
                exception.getMessage());
    }


}