package com.example.app.controller;

import com.example.app.controller.dto.ProductDTO;
import com.example.app.controller.dto.SellerDTO;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.security.SimpleUserPrinciple;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.ProductService;
import com.example.app.service.UserService;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasAuthority('SELLER')")
  public ResponseEntity<Product> addProductToTheSystem(@Valid @RequestBody ProductDTO productDto) {

    SimpleUserPrinciple userPrinciple = UserPrincipalUtil.extractUserPrinciple();

    User seller = sellerService.getUserByEmail(userPrinciple.getUsername());
    Product product = productService.createProduct(productDto, seller);

    return new ResponseEntity<>(product, HttpStatus.CREATED);
  }

  @GetMapping()
  @PreAuthorize("hasAuthority('SELLER')")
  public ResponseEntity<List<SellerDTO>> getAllSellers() {
    List<User> sellers = sellerService.getUsersByRole(Role.SELLER);
    List<SellerDTO> sellerDTOS = sellers.stream().map(SellerDTO::new).toList();

    return ResponseEntity.ok(sellerDTOS);
  }

  @GetMapping(path = "/{id}")
  @PreAuthorize("hasAuthority('SELLER')")
  public ResponseEntity<SellerDTO> getUserById(@PathVariable("id") Long sellerId) {
    User seller = sellerService.getUserByIdAndRole(sellerId, Role.SELLER);
    SellerDTO sellerDto = new SellerDTO(seller);
    return ResponseEntity.ok(sellerDto);
  }
}
