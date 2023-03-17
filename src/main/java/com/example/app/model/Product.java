package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
  @JsonBackReference(value = "product-discount")
  private Discount discount;

  @ManyToOne
  @JoinColumn(name = "category_id")
  @JsonBackReference(value = "product-category")
  private Category category;

  @ManyToOne
  @JoinColumn(name = "seller_id")
  @JsonBackReference(value = "product-seller")
  private User seller;

  private int quantity;

  @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
  @JsonBackReference(value = "product-orderDetails")
  private List<OrderDetails> orderDetails;
}
