package com.example.app.controller.dto;


import com.example.app.model.Order;
import com.example.app.model.User;
import lombok.*;

import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SellerStatistic {

    private ProductDTO product;
    private List<CustomerStat> customers;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CustomerStat {
        private Long id;
        private String fullName;
        private String email;
        private User.Role role;
        private int totalBonusAmount;
        private Order.Status status;
    }
}
