package com.example.app.exception;

public class NotFoundException extends RuntimeException {

  private final String errorCode;

  public NotFoundException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
