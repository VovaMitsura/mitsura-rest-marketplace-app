package com.example.app.controller;

import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

  private final UserService customerService;

  @Autowired
  public CustomerController(UserService customerService) {
    this.customerService = customerService;
  }


  @GetMapping()
  public ResponseEntity<List<User>> getUsers() {
    List<User> customers = customerService.getUsersByRole(Role.CUSTOMER);
    return ResponseEntity.ok(customers);
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<User> getUserById(@PathVariable("id") Long customerId) {
    User user = customerService.getUserByIdAndRole(customerId, Role.CUSTOMER);
    return ResponseEntity.ok(user);
  }

}
