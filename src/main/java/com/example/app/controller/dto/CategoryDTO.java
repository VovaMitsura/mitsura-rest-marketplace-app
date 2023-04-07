package com.example.app.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class CategoryDTO {
  @NotBlank
  private final String name;
  @NotBlank
  private final String description;
}
