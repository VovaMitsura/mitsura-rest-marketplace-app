package com.example.app.controller.dto;

import com.example.app.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreationResponseDTO {

  public static final String DEFAULT_SUCCESSFUL_MESSAGE = "Order successfully created";

  private String message;
  private Order order;


}
