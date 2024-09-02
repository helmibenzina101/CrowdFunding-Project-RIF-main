package com.rif.categories.exceptions;

public class UserNotAuthorizedToThisCategory extends Exception {
    public UserNotAuthorizedToThisCategory(String message) {
        super(message);
    }
}
