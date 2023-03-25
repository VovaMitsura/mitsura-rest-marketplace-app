package com.example.app.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ApplicationExceptionHandler {

  public static final String USER_NOT_FOUND = "user_not_found";
  public static final String DISCOUNT_NOT_FOUND = "discount_not_found";
  public static final String CATEGORY_NOT_FOUND = "category_not_found";
  public static final String NO_PERMISSION = "no_permission";

  @ResponseBody
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public ErrorResponse handleUserNotFoundException(NotFoundException e) {
    return new ErrorResponse(e.getErrorCode(), e.getMessage());
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class ErrorResponse {

    private String errorCode;
    private String errorMessage;

  }

}
