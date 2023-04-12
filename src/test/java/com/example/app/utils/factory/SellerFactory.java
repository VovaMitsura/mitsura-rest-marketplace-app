package com.example.app.utils.factory;

import com.example.app.model.User;

public class SellerFactory implements UserFactory {
    @Override
    public User createUser() {
        return User.builder().id(1L)
                .fullName("Tanya Smith")
                .role(User.Role.SELLER)
                .email("tanya@mail.com")
                .password("123456")
                .build();
    }
}
