package com.example.app.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "My_Order")
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "customer_id")
  @JsonBackReference
  private User customer;

  private String city;

  private String street;

  private String zip;

  @Column(name = "contact_phone")
  private String contactPhone;

  @Column(name = "total_amount")
  private int totalAmount;

  private Timestamp date;

  @OneToMany(mappedBy = "order")
  @JsonManagedReference
  private List<OrderDetails> orderDetails;

  @Override
  public String toString() {
    return "Order{" +
        "id=" + id +
        ", city='" + city + '\'' +
        ", street='" + street + '\'' +
        ", zip='" + zip + '\'' +
        ", contact_phone='" + contactPhone + '\'' +
        ", total_amount=" + totalAmount +
        ", date=" + date +
        '}';
  }
}
