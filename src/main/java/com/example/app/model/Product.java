package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity()
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private int price;

  @ManyToOne
  @JoinColumn(name = "discount_id")
  @JsonIgnoreProperties(value = "products")
  private Discount discount;

  @ManyToOne
  @JoinColumn(name = "category_id")
  //@JsonBackReference(value = "product-category")
  @JsonIgnoreProperties(value = "products")
  private Category category;

  @ManyToOne
  @JoinColumn(name = "seller_id")
  @JsonBackReference
  private User seller;

  @ManyToOne
  @JoinColumn(name = "bonus_id")
  @JsonIgnoreProperties(value = {"products", "users"})
  private Bonus bonus;


  private Integer quantity;

  @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
  @JsonIgnore
  private List<OrderDetails> orderDetails;
}
