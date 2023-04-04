package com.example.app.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

  private String name;
  private int price;
  private String discount;
  private String category;
  private int quantity;
}
