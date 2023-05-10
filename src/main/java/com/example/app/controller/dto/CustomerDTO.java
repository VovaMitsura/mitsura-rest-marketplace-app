package com.example.app.controller.dto;

import com.example.app.model.Order;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerDTO {

  private Long id;
  private String fullName;
  private String email;
  private Role role;
  private int totalBonusAmount;
  private List<Order> orders;

  public CustomerDTO(User customer) {
    this.id = customer.getId();
    this.fullName = customer.getFullName();
    this.email = customer.getEmail();
    this.role = customer.getRole();
    this.totalBonusAmount = customer.getTotalBonusAmount();
    this.orders = customer.getOrders();
  }
}
