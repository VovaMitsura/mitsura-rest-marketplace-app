package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.Product;
import com.example.app.model.User;
import com.example.app.model.VerificationToken;
import com.example.app.repository.VerificationTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = VerificationTokenService.class)
class VerificationTokenServiceTest {

    @Autowired
    VerificationTokenService tokenService;

    @MockBean
    VerificationTokenRepository tokenRepository;

    ObjectMapper mapper = new ObjectMapper();
    List<VerificationToken> tokens;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        VerificationToken[] verificationTokens =
                mapper.readValue(new File("src/test/resources/data/verificationTokens.json"),
                        VerificationToken[].class);
        tokens = Arrays.asList(verificationTokens);
    }

    @Test
    void findByExistTokenReturnToken() {
        VerificationToken token = tokens.get(0);

        Mockito.when(tokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        var response = tokenService.findByToken(token.getToken());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(token, response);
    }

    @Test
    void findNonExistingTokenThrowException() {
        Mockito.when(tokenRepository.findByToken("abc"))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(NotFoundException.class, () -> {
            tokenService.findByToken("abc");
        });

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.TOKEN_EXCEPTION, exception.getErrorCode());
        Assertions.assertEquals(String.format("Token: %s not found", "abc"), exception.getMessage());
    }

    @Test
    void findByUserReturnToken() {
        var token = tokens.get(0);
        var user = token.getUser();
        Mockito.when(tokenRepository.findByUser(user))
                .thenReturn(Optional.of(token));

        var response = tokenService.findByUser(user);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(token, response);
    }

    @Test
    void findByNonExistingUserThrowException() {
        var user = tokens.get(0).getUser();
        Mockito.when(tokenRepository.findByUser(Mockito.any(User.class)))
                .thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(NotFoundException.class, () -> {
            tokenService.findByUser(user);
        });

        Assertions.assertNotNull(exception);
        Assertions.assertEquals(ApplicationExceptionHandler.TOKEN_EXCEPTION, exception.getErrorCode());
        Assertions.assertEquals(String.format("Token of %s not found", user.getEmail()), exception.getMessage());
    }

    @Test
    void saveReturnToken() {
        var token = tokens.get(0);
        Mockito.when(tokenRepository.save(token))
                .thenReturn(token);

        var response = tokenService.save(token.getUser(), token.getToken());

        Assertions.assertNotNull(response);
        Assertions.assertEquals(token.getToken(), response.getToken());
        Assertions.assertEquals(token.getUser(), response.getUser());
    }
}