package com.example.app.utils.factory;

import com.example.app.model.User;

public class CustomerFactory implements UserFactory{
    @Override
    public User createUser() {
        return User.builder().id(1L)
                .fullName("John Smith")
                .role(User.Role.CUSTOMER)
                .email("john@mail.com")
                .password("123456")
                .build();
    }
}
