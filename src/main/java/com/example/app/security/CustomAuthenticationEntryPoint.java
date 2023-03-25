package com.example.app.security;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ApplicationExceptionHandler.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    ErrorResponse errorResponse = new ErrorResponse(ApplicationExceptionHandler.NO_PERMISSION,
        "Have not provide any credentials");

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
