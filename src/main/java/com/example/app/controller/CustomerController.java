package com.example.app.controller;

import com.example.app.controller.dto.CustomerDto;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.service.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@Transactional
public class CustomerController {

  private final UserService customerService;

  @Autowired
  public CustomerController(UserService customerService) {
    this.customerService = customerService;
  }


  @GetMapping()
  @PreAuthorize("hasAuthority('CUSTOMER')")
  public ResponseEntity<List<CustomerDto>> getAllCustomers() {
    List<User> customers = customerService.getUsersByRole(Role.CUSTOMER);
    List<CustomerDto> customerDTOs = customers.stream().map(CustomerDto::new).toList();

    return ResponseEntity.ok(customerDTOs);
  }

  @GetMapping(path = "/{id}")
  @PreAuthorize("hasAuthority('CUSTOMER')")
  public ResponseEntity<CustomerDto> getUserById(@PathVariable("id") Long customerId) {
    User user = customerService.getUserByIdAndRole(customerId, Role.CUSTOMER);
    CustomerDto customerDTO = new CustomerDto(user);

    return ResponseEntity.ok(customerDTO);
  }

}