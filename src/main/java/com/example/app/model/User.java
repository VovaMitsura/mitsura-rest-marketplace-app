package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "fullname")
  private String fullName;

  @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
      flags = Pattern.Flag.CASE_INSENSITIVE)
  private String email;

  @Column(columnDefinition = "ENUM('Customer', 'Seller', 'Admin')")
  @Enumerated(EnumType.STRING)
  private Role role;

  private String password;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "User_Bonus",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "bonus_id")
  )
  @JsonManagedReference
  private List<Bonus> bonuses;

  @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
  @JsonManagedReference(value = "product-seller")
  private List<Product> products;

  @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
  @JsonManagedReference
  private List<Order> orders;

  public enum Role {
    CUSTOMER, SELLER, ADMIN
  }


}
