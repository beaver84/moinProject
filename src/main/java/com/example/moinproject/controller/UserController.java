package com.example.moinproject.controller;

import com.example.moinproject.config.JwtTokenProvider;
import com.example.moinproject.config.response.ErrorResponse;
import com.example.moinproject.domain.dto.user.LoginRequest;
import com.example.moinproject.domain.dto.user.LoginResponse;
import com.example.moinproject.domain.dto.user.SignUpRequest;
import com.example.moinproject.domain.dto.user.UserDto;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request) {
        try {
            UserDto createdUser = userService.signup(request);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticateUser(loginRequest.getUserId(), loginRequest.getPassword());
            String token = jwtTokenProvider.generateToken(user);

            LoginResponse response = new LoginResponse();
            response.setResultCode(200);
            response.setResultMsg("OK");
            response.setToken(token);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials"));
        }
    }
}

