package com.example.app.exception;

public class ResourceConflictException extends RuntimeException {

  private final String errorCode;

  public ResourceConflictException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }

}
