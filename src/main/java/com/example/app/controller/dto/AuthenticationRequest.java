package com.example.app.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AuthenticationRequest {

  @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
          flags = Pattern.Flag.CASE_INSENSITIVE)
  private String email;
  @NonNull
  private String password;
}
