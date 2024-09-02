package com.rif.validations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ObjectValidator {

    // Validate object (generic method to validate any object)
    public ResponseEntity<?> validateObject(BindingResult result) {
        List<ObjectError> errors = result.getAllErrors();
        Map<String, String> errorsMap = new HashMap<>();

        // iterate through all ObjectError
        for (ObjectError objectError : errors) {
            // check if that is an instance of Field error to get the current object errors
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                // put all fields error and the error messages into HashMap that we have instantiated
                errorsMap.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        }
        return new ResponseEntity<>(errorsMap, HttpStatus.BAD_REQUEST);
    }
}
