package com.example.app.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class CategoryDTO {
  private final String name;
  private final String description;
}
