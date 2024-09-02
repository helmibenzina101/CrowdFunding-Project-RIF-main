package com.rif.categories.repository;

import com.rif.authentication.models.User;
import com.rif.categories.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContaining(String name);

    // check existence of name and imagePath fields
    boolean existsByImagePath(String imagePath);
    boolean existsByName(String name);

    Category findByNameContainingIgnoreCase(String name);
    // Get all categories belong to userId
    List<Category> findByCreatorId(Long id);
}
