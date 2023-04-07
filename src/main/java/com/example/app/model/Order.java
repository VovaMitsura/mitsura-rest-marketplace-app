package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "My_Order")
@Getter
@Setter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@NoArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "customer_id")
  @JsonBackReference
  private User customer;

  @Column(name = "total_amount")
  private int totalAmount;

  private Timestamp date;

  @OneToMany(mappedBy = "order", fetch= FetchType.EAGER)
  @JsonManagedReference
  private List<OrderDetails> orderDetails;

  @Enumerated(EnumType.STRING)
  private Status status;

  public int getTotalAmount() {
    return orderDetails.stream().mapToInt(OrderDetails::getTotalPrice)
        .sum();
  }

  @Override
  public String toString() {
    return "Order{" +
        "id=" + id +
        ", total_amount=" + totalAmount +
        ", date=" + date +
        '}';
  }

  public enum Status{
    CREATED, BOUGHT
  }
}
