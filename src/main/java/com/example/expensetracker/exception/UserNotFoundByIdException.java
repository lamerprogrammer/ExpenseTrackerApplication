package com.example.expensetracker.exception;

public class UserNotFoundByIdException extends RuntimeException {
    public UserNotFoundByIdException(String message) {
        super(message);
    }
}
