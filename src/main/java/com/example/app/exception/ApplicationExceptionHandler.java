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

  @ResponseBody
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserNotFoundException.class)
  public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
    return new ErrorResponse(USER_NOT_FOUND, e.getMessage());
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class ErrorResponse {

    private String errorCode;
    private String errorMessage;

  }

}
