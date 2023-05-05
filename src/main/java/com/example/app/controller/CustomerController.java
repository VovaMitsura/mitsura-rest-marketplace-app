package com.example.app.controller;

import com.example.app.controller.dto.CustomerDTO;
import com.example.app.model.Order;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.OrderService;
import com.example.app.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@PreAuthorize("hasAuthority('CUSTOMER')")
@Transactional
public class CustomerController {

  private final UserService customerService;
  private final OrderService orderService;

  @Autowired
  public CustomerController(UserService customerService, OrderService orderService) {
    this.customerService = customerService;
    this.orderService = orderService;
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<CustomerDTO> getUserById(@PathVariable("id") Long customerId) {
    User user = customerService.getUserByIdAndRole(customerId, Role.CUSTOMER);
    CustomerDTO customerDTO = new CustomerDTO(user);

    return ResponseEntity.ok(customerDTO);
  }

  @GetMapping(path = "/history")
  public ResponseEntity<List<Order>> getBoughtOrders(){
    String userEmail = UserPrincipalUtil.extractUserEmail();
    List<Order> userOrders = orderService.getUserOrders(userEmail, Order.Status.BOUGHT);

    return ResponseEntity.ok(userOrders);
  }
}