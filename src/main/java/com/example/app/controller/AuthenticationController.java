package com.example.app.controller;

import com.example.app.controller.dto.ActivationDTO;
import com.example.app.controller.dto.AuthenticationRequest;
import com.example.app.controller.dto.AuthenticationResponse;
import com.example.app.controller.dto.RegisterRequest;
import com.example.app.security.SimpleUserPrinciple;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<ActivationDTO> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(authService.register(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody AuthenticationRequest registerRequest) {
        return ResponseEntity.ok(authService.authenticate(registerRequest));
    }

    @GetMapping("/activation")
    public ResponseEntity<AuthenticationResponse> activation(@RequestParam(required = true, name = "token")
                                                             String token) {
        return ResponseEntity.ok(authService.activate(token));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SELLER', 'ADMIN')")
    public ResponseEntity<SimpleUserPrinciple> getMe() {

        SimpleUserPrinciple simpleUserPrinciple = UserPrincipalUtil.extractUserPrinciple();

        return ResponseEntity.ok(simpleUserPrinciple);
    }

}