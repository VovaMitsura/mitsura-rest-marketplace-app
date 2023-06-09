package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UserService.class)
class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    UserRepository userRepository;

    ObjectMapper mapper = new ObjectMapper();
    List<User> users;

    @BeforeEach()
    void setUp() throws IOException {
        users = List.of(mapper.readValue(new File("src/test/resources/data/users.json"), User[].class));
    }

    @Test
    void getUsersByRoleShouldReturnList() {
        Mockito.when(userRepository.getUsersByRole(User.Role.CUSTOMER))
                .thenReturn(users.stream().filter(user -> user.getRole().equals(User.Role.CUSTOMER)).toList());

        List<User> expected = users.stream().filter(user -> user.getRole().equals(User.Role.CUSTOMER)).toList();
        List<User> response = userService.getUsersByRole(User.Role.CUSTOMER);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(expected.size(), response.size());
        Assertions.assertEquals(expected.get(0), response.get(0));
    }

    @Test
    void getUsersByRoleThrowException() {
        Mockito.when(userRepository.getUsersByRole(User.Role.CUSTOMER))
                .thenReturn(List.of());

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.getUsersByRole(User.Role.CUSTOMER));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.USER_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("No exists %ss in system", User.Role.CUSTOMER), exception.getMessage());
    }

    @Test
    void getUserByIdAndRoleReturnUser() {
        Optional<User> expected = users.stream().filter(user -> user.getId() == 1 && user.getRole().equals(User.Role.CUSTOMER))
                .findFirst();

        Mockito.when(userRepository.findByIdAndRole(1L, User.Role.CUSTOMER))
                .thenReturn(expected);

        User response = userService.getUserByIdAndRole(1L, User.Role.CUSTOMER);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(expected.get(), response);
    }

    @Test
    void getUserByIdAndRoleThrowException() {
        Optional<User> expected = users.stream().filter(user -> user.getId() == 10 &&
                        user.getRole().equals(User.Role.CUSTOMER))
                .findFirst();

        Mockito.when(userRepository.findByIdAndRole(10L, User.Role.CUSTOMER))
                .thenReturn(expected);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.getUserByIdAndRole(10L, User.Role.CUSTOMER));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.USER_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("No exists %s with id: %d in system", User.Role.CUSTOMER, 10L),
                exception.getMessage());
    }

    @Test
    void getUserByEmailReturnUser() {
        Optional<User> expected = users.stream().filter(user -> user.getEmail().equals("john@mail.com"))
                .findFirst();

        Mockito.when(userRepository.findByEmail("john@mail.com"))
                .thenReturn(expected);

        User response = userService.getUserByEmail("john@mail.com");

        Assertions.assertNotNull(response);
        Assertions.assertEquals(expected.get(), response);
    }

    @Test
    void getUserByEmailThrowException() {
        Optional<User> expected = users.stream().filter(user -> user.getEmail().equals("lol@mail.com"))
                .findFirst();

        Mockito.when(userRepository.findByEmail("lol@mail.com"))
                .thenReturn(expected);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () ->
                userService.getUserByEmail("lol@mail.com"));

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.USER_NOT_FOUND, exception.getErrorCode());
        Assertions.assertEquals(String.format("User with email [%s] not found", "lol@mail.com"), exception.getMessage());
    }

    @Test
    void updateUserBonusAmountReturnNewUser() {
        Optional<User> optionalUser = users.stream().filter(u -> u.getEmail().equals("john@mail.com")).findFirst();

        User update = new User(1L, "John Smith", "john@mail.com", User.Role.CUSTOMER,
                1000, optionalUser.get().getPassword(),
                optionalUser.get().getProducts(), optionalUser.get().getOrders(), true);

        Mockito.when(userRepository.findByEmail("john@mail.com"))
                .thenReturn(optionalUser);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(update);

        User response = userService.updateUser(update);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(update.getTotalBonusAmount(), response.getTotalBonusAmount());
    }

    @Test
    void grantUserRoleAdmin() {
        Optional<User> optionalUser = users.stream().filter(u -> u.getEmail().equals("john@mail.com")).findFirst();
        User updatedUser = optionalUser.get();
        updatedUser.setRole(User.Role.ADMIN);

        Mockito.when(userRepository.findByEmail("john@mail.com"))
                .thenReturn(optionalUser);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(updatedUser);

        User response = userService.grantUserRole("john@mail.com", User.Role.ADMIN);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(updatedUser.getEmail(), response.getEmail());
        Assertions.assertEquals(User.Role.ADMIN, response.getRole());
    }
}