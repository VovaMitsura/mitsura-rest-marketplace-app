package com.example.app.service;

import com.example.app.controller.dto.AuthenticationRequest;
import com.example.app.controller.dto.AuthenticationResponse;
import com.example.app.controller.dto.RegisterRequest;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtService;
import com.example.app.security.SimpleUserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authManager;

  public AuthenticationResponse register(RegisterRequest registerRequest) {
    var user = User.builder().fullName(registerRequest.getFullName())
        .email(registerRequest.getEmail())
        .password(passwordEncoder.encode(registerRequest.getPassword()))
        .role(Role.valueOf(registerRequest.getRole())).build();

    userRepository.save(user);
    var jwtToken = jwtService.generateToken(new SimpleUserPrinciple(user));
    return AuthenticationResponse.builder().token(jwtToken).build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest registerRequest) {
    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(registerRequest.getEmail(),
            registerRequest.getPassword()
        )
    );

    var user = userRepository.findByEmail(registerRequest.getEmail())
        .orElseThrow();

    var jwtToken = jwtService.generateToken(new SimpleUserPrinciple(user));
    return AuthenticationResponse.builder().token(jwtToken).build();

  }
}
