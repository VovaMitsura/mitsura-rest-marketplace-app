package com.example.app.controller.dto;

import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerDto {

  private Long id;
  private String fullName;
  private String email;
  private Role role;
  private List<Product> products;

  public SellerDto(User seller) {
    this.id = seller.getId();
    this.fullName = seller.getFullName();
    this.email = seller.getEmail();
    this.role = seller.getRole();
    this.products = seller.getProducts();
  }
}
