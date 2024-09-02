package com.rif.categories.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationCategoryExceptionsHandler {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map handleValidationsError(MethodArgumentNotValidException ex) {
        // create new hashMap
        Map<String, String> errorsMap = new HashMap<>();
        // iterate through all errors
        ex.getBindingResult().getAllErrors().forEach(err -> {
            // put all errors fields & message into errorsMap
            errorsMap.put(err.getObjectName(), err.getDefaultMessage());
        });
        return errorsMap;
    }

    // category not found exception function
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(CategoryNotFoundException.class)
    public Map handleCategoryNotFoundException(CategoryNotFoundException ex) {
        // create new hashMap
        Map<String, String> errorsMap = new HashMap<>();
        // save error message to errorsMap
        errorsMap.put("category_not_found", ex.getMessage());
        return errorsMap;
    }

    // category file not found exception function
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(CategroyFileException.class)
    public Map handleCategoryFileException(CategroyFileException ex) {
        // create new hashMap
        Map<String, String> errorsMap = new HashMap<>();
        // save error message to errorsMap
        errorsMap.put("image_exist", ex.getMessage());
        return errorsMap;
    }

    // category exist exception method
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(CategoryExistException.class)
    public Map handleCategoryExist(CategoryExistException ex) {
        // create new hashMap
        Map<String, String> errorsMap = new HashMap<>();
        // save error message to errorsMap
        errorsMap.put("category_exist", ex.getMessage());
        return errorsMap;
    }

    // category name and image exist exception method
    @ExceptionHandler(CategoryExistWithNameAndImage.class)
    public ResponseEntity<Map> handleException(CategoryExistWithNameAndImage ex) {
        Map<String, String> errorsMap = new HashMap<>();
        // save error message to errorsMap
        errorsMap.put("error", ex.getMessage());
        return new ResponseEntity<>(errorsMap, HttpStatus.FORBIDDEN);
    }

    // category name and image exist exception method
    @ExceptionHandler(CategoryNameExistException.class)
    public ResponseEntity<Map> handleCategoryNameExistException(CategoryNameExistException ex) {
        Map<String, String> errorsMap = new HashMap<>();
        // save error message to errorsMap
        errorsMap.put("error", ex.getMessage());
        return new ResponseEntity<>(errorsMap, HttpStatus.FORBIDDEN);
    }

    // user can't delete category not belong to him
    @ExceptionHandler(UserNotAuthorizedToThisCategory.class)
    public ResponseEntity<Map> categoryNotBelongToUser(UserNotAuthorizedToThisCategory ex) {
        Map<String, String> errorsMap = new HashMap<>();
        errorsMap.put("error", ex.getMessage());
        return new ResponseEntity<>(errorsMap, HttpStatus.FORBIDDEN);
    }

    // category name must not be null with spacing ,lowercase or uppercase
    @ExceptionHandler(CategoryNotNull.class)
    public ResponseEntity<Map> categoryNotNull(CategoryNotNull ex) {
        Map<String, String> errorsMap = new HashMap<>();
        errorsMap.put("error", ex.getMessage());
        return new ResponseEntity<>(errorsMap, HttpStatus.FORBIDDEN);
    }
}
