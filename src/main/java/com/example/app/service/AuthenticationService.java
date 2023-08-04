package com.example.app.service;

import com.example.app.controller.dto.ActivationDTO;
import com.example.app.controller.dto.AuthenticationRequest;
import com.example.app.controller.dto.AuthenticationResponse;
import com.example.app.controller.dto.RegisterRequest;
import com.example.app.exception.ApplicationExceptionHandler;
import com.example.app.exception.ResourceConflictException;
import com.example.app.model.User;
import com.example.app.model.User.Role;
import com.example.app.repository.UserRepository;
import com.example.app.security.JwtService;
import com.example.app.security.SimpleUserPrinciple;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationTokenService verificationTokenService;
    private final MailingService mailingService;

    public static final String VERIFICATION = "verification";
    public static final String VERIFICATION_LINK = "http://localhost:8080/api/v1/auth/activation?token=";

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    @Transactional
    public ActivationDTO register(RegisterRequest registerRequest) {
        var user = User.builder().fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.valueOf(registerRequest.getRole()))
                .enabled(false)
                .build();

        Optional<User> saved = Optional.empty();

        try {
            saved = Optional.of(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException(ApplicationExceptionHandler.DUPLICATE_ENTRY,
                    e.getCause().getCause().getMessage());
        }

        saved.ifPresent(u -> {
            String token = null;
            try {
                token = UUID.randomUUID().toString();
                verificationTokenService.save(u, token);
            } catch (DataIntegrityViolationException e) {
                throw new ResourceConflictException(ApplicationExceptionHandler.DUPLICATE_ENTRY,
                        e.getCause().getCause().getMessage());
            }

            Map<String, Object> playLoad = new HashMap<>();
            playLoad.put("customer", u);
            playLoad.put(VERIFICATION, VERIFICATION_LINK + token);
            try {
                mailingService.send(u, VERIFICATION, playLoad);
            } catch (IOException | MessagingException | TemplateException e) {
                logger.error(String.format("Exception %s occur while sending confirmation mail to %s",
                        e.getMessage(), u.getEmail()));
            }
        });

        return new ActivationDTO("Confirm email to activate account");
    }

    public AuthenticationResponse activate(String token) {
        var verToken = verificationTokenService.findByToken(token);
        var user = verToken.getUser();

        if (!user.isEnabled()) {
            Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
            if (verToken.getExpiryDate().before(currentTimeStamp)) {
                throw new ResourceConflictException(ApplicationExceptionHandler.TOKEN_EXCEPTION,
                        String.format("Your verification token has expired %s", verToken.getExpiryDate()));
            } else {
                user.setEnabled(true);
                userRepository.save(user);
            }
        }

        var jwtToken = jwtService.generateToken(new SimpleUserPrinciple(user));
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest registerRequest) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        var user = userRepository.findByEmail(registerRequest.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(new SimpleUserPrinciple(user));
        return AuthenticationResponse.builder().token(jwtToken).build();

    }
}
