package com.example.app.repository;

import com.example.app.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByName(String name);
}
