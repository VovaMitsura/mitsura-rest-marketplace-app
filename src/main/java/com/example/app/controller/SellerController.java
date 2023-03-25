package com.example.app.controller;

import com.example.app.controller.dto.ProductDto;
import com.example.app.controller.dto.SellerDto;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.service.ProductService;
import com.example.app.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {

  private final ProductService productService;
  private final UserService sellerService;

  @Autowired
  public SellerController(ProductService productService, UserService userService) {
    this.productService = productService;
    this.sellerService = userService;
  }


  @PostMapping("/product")
  public ResponseEntity<Product> addProductToTheSystem(@RequestBody ProductDto productDto) {

    User seller = sellerService.getUserByIdAndRole(4L, Role.SELLER);
    Product product = productService.createProduct(productDto, seller);

    return new ResponseEntity<>(product, HttpStatus.CREATED);
  }

  @GetMapping()
  public ResponseEntity<List<SellerDto>> getAllSellers() {
    List<User> sellers = sellerService.getUsersByRole(Role.SELLER);
    List<SellerDto> sellerDTOs = sellers.stream().map(SellerDto::new).toList();

    return ResponseEntity.ok(sellerDTOs);
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<SellerDto> getUserById(@PathVariable("id") Long sellerId) {
    User seller = sellerService.getUserByIdAndRole(sellerId, Role.SELLER);
    SellerDto sellerDto = new SellerDto(seller);
    return ResponseEntity.ok(sellerDto);
  }
}
