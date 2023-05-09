package com.example.app.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

  @NotBlank
  private String name;
  @Min(1)
  private int price;
  @Min(1)
  private int priceInBonus;
  private String discount;
  @NotBlank
  private String category;
  private String bonus;
  @Min(1)
  private int quantity;
}
