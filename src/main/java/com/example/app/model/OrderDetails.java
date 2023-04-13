package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @Transient
    private String productName;

    @ManyToOne()
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    private int quantity;

    @Transient
    public int getTotalPrice() {
        if (product != null) return product.getPrice() * quantity;
        return 0;
    }

    @Transient
    public String getProductName() {
        return product.getName();
    }

}
