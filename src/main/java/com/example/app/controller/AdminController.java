package com.example.app.controller;

import com.example.app.controller.dto.GrantDTO;
import com.example.app.model.User;
import com.example.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<GrantDTO> grantRole(@RequestParam(value = "email") String email,
                                              @RequestParam("role") String role) {
        User user = userService.grantUserRole(email, User.Role.valueOf(role));

        return ResponseEntity.ok(new GrantDTO(user.getEmail(), user.getRole()));
    }
}
