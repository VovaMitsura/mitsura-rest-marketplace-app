package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

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

  @JsonIgnore
  private String password;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "User_Bonus",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "bonus_id")
  )
  private List<Bonus> bonuses;

  @OneToMany(mappedBy = "seller", fetch = FetchType.EAGER)
  @JsonManagedReference()
  private List<Product> products;

  @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
  @JsonManagedReference
  private List<Order> orders;

  public enum Role {
    CUSTOMER, SELLER, ADMIN
  }


}
