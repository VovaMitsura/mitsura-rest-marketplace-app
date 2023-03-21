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
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@NoArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "fullname")
  private String fullName;

  private String email;

  @Column(columnDefinition = "ENUM('Customer', 'Seller', 'Admin')")
  @Enumerated(EnumType.STRING)
  private Role role;

  private String password;

  @ManyToMany
  @JoinTable(
      name = "User_Bonus",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "bonus_id")
  )
  @JsonManagedReference
  private List<Bonus> bonuses;

  @OneToMany(mappedBy = "seller")
  @JsonManagedReference(value = "product-seller")
  private List<Product> products;

  @OneToMany(mappedBy = "customer")
  @JsonManagedReference
  private List<Order> orders;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(getRole().toString()));
  }

  @Override
  public String getUsername() {
    return getFullName();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public enum Role {
    CUSTOMER, SELLER, ADMIN
  }


}
