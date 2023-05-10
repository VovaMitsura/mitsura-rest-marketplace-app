package com.example.app.controller.dto;

import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SellerDTO {

  private Long id;
  private String fullName;
  private String email;
  private Role role;
  private List<Product> products;

  public SellerDTO(User seller) {
    this.id = seller.getId();
    this.fullName = seller.getFullName();
    this.email = seller.getEmail();
    this.role = seller.getRole();
    this.products = seller.getProducts();
  }
}
