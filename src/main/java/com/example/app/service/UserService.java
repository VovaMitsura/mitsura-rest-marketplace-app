package com.example.app.service;


import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsersByRole(Role role) {
        List<User> users = userRepository.getUsersByRole(role);

        if (users.isEmpty()) {
            throw new NotFoundException(
                    ApplicationExceptionHandler.USER_NOT_FOUND,
                    String.format("No exists %ss in system", role));
        }

        return users;
    }

    public User getUserByIdAndRole(Long id, Role role) {
        Optional<User> user = userRepository.findByIdAndRole(id, role);

        return user.orElseThrow(
                () -> new NotFoundException(
                        ApplicationExceptionHandler.USER_NOT_FOUND,
                        String.format("No exists %s with id: %d in system", role, id)));
    }

    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.USER_NOT_FOUND,
                String.format("User with email [%s] not found", email)));
    }

    public User updateUser(User update) {
        User currentUser = getUserByEmail(update.getEmail());

        currentUser.setOrders(update.getOrders());
        currentUser.setTotalBonusAmount(update.getTotalBonusAmount());
        currentUser.setOrders(update.getOrders());
        currentUser.setProducts(update.getProducts());

        return userRepository.save(currentUser);
    }

    public User grantUserRole(String userEmail, Role role) {
        User user = getUserByEmail(userEmail);
        user.setRole(role);
        return userRepository.save(user);
    }

}
