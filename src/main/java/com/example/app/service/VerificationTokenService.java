package com.example.app.service;

import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.NotFoundException;
import com.example.app.model.User;
import com.example.app.model.VerificationToken;
import com.example.app.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;

@Service
@AllArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.TOKEN_EXCEPTION,
                        String.format("Token: %s not found", token)));
    }

    public VerificationToken findByUser(User user) {
        return verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException(ApplicationExceptionHandler.TOKEN_EXCEPTION,
                        String.format("Token of %s not found", user.getEmail())));
    }

    public VerificationToken save(User user, String token) {
        var expirationDate = calculateExpirationDate(24 * 60);
        var verToken = new VerificationToken(token, user, expirationDate);
        verificationTokenRepository.save(verToken);
        return verToken;
    }

    private Timestamp calculateExpirationDate(int expiryTimeMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryTimeMinutes);
        return new Timestamp(cal.getTime().getTime());
    }

}
