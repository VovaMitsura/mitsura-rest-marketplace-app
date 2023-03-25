package com.example.app.controller;

import com.example.app.controller.dto.AuthenticationRequest;
import com.example.app.controller.dto.AuthenticationResponse;
import com.example.app.controller.dto.RegisterRequest;
import com.example.app.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest registerRequest) {
    return ResponseEntity.ok(service.register(registerRequest));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody AuthenticationRequest registerRequest) {
    return ResponseEntity.ok(service.authenticate(registerRequest));
  }

}