package com.rohan.cloudProject.controller.advice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom Exception Handler Class. Displays Neat Messages when an Empty or Null values is supplied. Handles Bad Requests.
 *
 * @author rohan_bharti
 */
@ControllerAdvice
@ResponseBody
public class ControllerAdviceErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> invalidFormatException(final InvalidFormatException e) {
        return error(e, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> error(final Exception exception, final HttpStatus httpStatus) {
        final String message = Optional.ofNullable(exception.getMessage()).orElse(exception.getClass().getSimpleName());
        return new ResponseEntity(new ErrorResponse(message), httpStatus);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public class ErrorResponse {
        private String errorMessage;
    }
}


