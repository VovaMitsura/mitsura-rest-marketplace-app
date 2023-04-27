package com.example.app.controller;

import com.example.app.controller.dto.DiscountDTO;
import com.example.app.controller.dto.ProductDTO;
import com.example.app.controller.dto.SellerDTO;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.security.SimpleUserPrinciple;
import com.example.app.security.util.UserPrincipalUtil;
import com.example.app.service.ProductService;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers")
@PreAuthorize("hasAuthority('SELLER')")
public class SellerController {

    private final ProductService productService;
    private final UserService sellerService;

    @Autowired
    public SellerController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.sellerService = userService;
    }


    @PostMapping("/products")
    public ResponseEntity<Product> addProductToTheSystem(@Valid @RequestBody ProductDTO productDto) {

        SimpleUserPrinciple userPrinciple = UserPrincipalUtil.extractUserPrinciple();

        User seller = sellerService.getUserByEmail(userPrinciple.getUsername());
        Product product = productService.createProduct(productDto, seller);

        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<SellerDTO>> getAllSellers() {
        List<User> sellers = sellerService.getUsersByRole(Role.SELLER);
        List<SellerDTO> sellerDTOS = sellers.stream().map(SellerDTO::new).toList();

        return ResponseEntity.ok(sellerDTOS);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<SellerDTO> getUserById(@PathVariable("id") Long sellerId) {
        User seller = sellerService.getUserByIdAndRole(sellerId, Role.SELLER);
        SellerDTO sellerDto = new SellerDTO(seller);
        return ResponseEntity.ok(sellerDto);
    }

    @PutMapping("/products/{id}")
    ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO update) {
        String userEmail = UserPrincipalUtil.extractUserEmail();
        return new ResponseEntity<>(productService.update(id, update, userEmail), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/products/{id}")
    ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
        String userEmail = UserPrincipalUtil.extractUserEmail();
        return new ResponseEntity<>(productService.delete(id, userEmail), HttpStatus.OK);
    }

    @PostMapping("/products/{id}/discounts")
    ResponseEntity<ProductDTO> addDiscountToProduct(@PathVariable Long id,
                                                    @Valid @RequestBody DiscountDTO req) {
        String userEmail = UserPrincipalUtil.extractUserEmail();
        Product product = productService.addDiscountToProduct(id, userEmail, req);
        ProductDTO response = new ProductDTO(product.getName(), product.getPrice(),
                product.getDiscount().getName(), product.getCategory().getName(), product.getQuantity());
        return ResponseEntity.ok(response);
    }
}
