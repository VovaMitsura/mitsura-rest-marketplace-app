package com.example.app.controller.dto;

import com.example.app.model.Bonus;
import com.example.app.model.Order;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDto {

  private Long id;
  private String fullName;
  private String email;
  private Role role;
  private List<Bonus> bonuses;
  private List<Order> orders;

  public CustomerDto(User customer) {
    this.id = customer.getId();
    this.fullName = customer.getFullName();
    this.email = customer.getEmail();
    this.role = customer.getRole();
    this.bonuses = customer.getBonuses();
    this.orders = customer.getOrders();
  }
}
