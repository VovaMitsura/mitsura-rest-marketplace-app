package com.example.app.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

  @NotBlank
  private String name;
  @Min(1)
  private int price;
  @NotBlank
  private String discount;
  @NotBlank
  private String category;
  @Min(1)
  private int quantity;
}
