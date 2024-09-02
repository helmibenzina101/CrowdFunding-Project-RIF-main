package com.rif.categories.controller;

import com.rif.authentication.models.User;
import com.rif.categories.exceptions.*;
import com.rif.categories.exceptions.CategoryExistWithNameAndImage;
import com.rif.categories.exceptions.CategoryNameExistException;
import com.rif.categories.dto.APIResponse;
import com.rif.categories.dto.CategoryResponse;
import com.rif.categories.dto.CategoryRequest;
import com.rif.categories.entity.Category;
import com.rif.categories.service.CategoryService;
import com.rif.validations.ObjectValidator;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    // inject needed services
    private final CategoryService service;
    private final ObjectValidator objectValidator;

    // CREATE Category
    @PostMapping("/add")
    public ResponseEntity<?> saveCategory(@RequestParam("category_image") MultipartFile file,
                                          @ModelAttribute @Valid CategoryRequest request, BindingResult result,
                                          Principal principal)
            throws IOException, CategroyFileException, CategoryExistWithNameAndImage, CategoryNameExistException, CategoryNotNull {
        if (result.hasErrors()) {return objectValidator.validateObject(result);}
        this.service.validateImage(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.service.add(file, request.getName(),principal));
    }

    // UPDATE Category
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @RequestParam("category_image") MultipartFile file,
                                    @ModelAttribute @Valid CategoryRequest request, BindingResult result,
                                    Principal principal)
            throws IOException, CategroyFileException, CategoryNotFoundException, CategoryExistWithNameAndImage, CategoryExistException, CategoryNameExistException, UserNotAuthorizedToThisCategory, CategoryNotNull {
        if (result.hasErrors()) {return this.objectValidator.validateObject(result);}
        this.service.validateImage(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.service.update(file, request, id,principal));
    }

    // GET Category
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getCategory(@PathVariable("id") Long id) throws IOException, CategoryNotFoundException {
        return ResponseEntity.ok(this.service.getById(id));
    }

    // DELETE Category
    @DeleteMapping("/{id}")
    public ResponseEntity<?> destroyCategory(@PathVariable("id") Long id, Principal principal) throws CategoryNotFoundException, UserNotAuthorizedToThisCategory {
        return ResponseEntity.ok(this.service.delete(id,principal));
    }

    // GET : get all categories
    @GetMapping("/all")
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponse> categoryResponses = this.service.getAll();
        APIResponse<List<CategoryResponse>> apis = new APIResponse<>(categoryResponses.size(), categoryResponses);
        return new ResponseEntity<>(apis, HttpStatus.OK);
    }

    // GET : get all categories belong to authenticated user (admin role)
    @GetMapping("/all/admin")
    public ResponseEntity<List<CategoryResponse>> getUserCategories(Principal principal) throws CategoryNotFoundException {
        User admin = this.service.getUserByEmail(principal.getName());
        List<CategoryResponse> categories = this.service.findByUserId(admin.getId());
        return ResponseEntity.ok(categories);
    }

    // SEARCH : find category or categories by name /// ::: to do search by entity category
    @PostMapping("/search")
    public ResponseEntity<List<Category>> searchCategory(@RequestParam("content") String content)
            throws CategoryNotFoundException {
        return ResponseEntity.ok(this.service.search(content));
    }

}
