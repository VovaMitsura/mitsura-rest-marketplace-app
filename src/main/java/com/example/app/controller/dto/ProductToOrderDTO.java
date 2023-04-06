package com.example.app.controller.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
public class ProductToOrderDTO {

  @Min(1)
  private Long orderId;
  @Min(1)
  private Long productId;
  @Min(1)
  private Integer quantity;
}