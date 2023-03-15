package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "fullname")
  private String fullName;

  private String email;

  @Column(columnDefinition = "ENUM('Customer', 'Seller', 'Admin')")
  @Enumerated(EnumType.STRING)
  private Role role;

  @ManyToMany
  @JoinTable(
      name = "User_Bonus",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "bonus_id")
  )
  @JsonManagedReference
  private List<Bonus> bonuses;

  @OneToMany(mappedBy = "seller")
  @JsonManagedReference
  private List<Product> products;

  public enum Role{
    CUSTOMER, SELLER, ADMIN
  }


}
