package com.example.app.utils.factory;

import com.example.app.model.User;

public class AdminFactory implements UserFactory {
    @Override
    public User createUser() {
        return User.builder()
                .id(1L)
                .fullName("Jack John")
                .email("jack@mail.com")
                .role(User.Role.ADMIN)
                .password("123456")
                .build();
    }
}
