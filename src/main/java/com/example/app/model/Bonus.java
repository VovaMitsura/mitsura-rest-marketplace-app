package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Bonus {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String name;

  private Timestamp date;

  private int amount;

  @ManyToMany(mappedBy = "bonuses")
  @JsonIgnore
  private List<User> users;

  @OneToMany(mappedBy = "bonus")
  private List<Product> products;


}
