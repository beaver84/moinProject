package com.example.moinproject.service;

import com.example.moinproject.domain.dto.SignUpRequest;
import com.example.moinproject.domain.dto.UserDto;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.domain.enums.IdType;
import com.example.moinproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EncryptionService encryptionService;

    public UserDto signup(SignUpRequest request) {
        validateSignUpRequest(request);

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User already exists with this email");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIdType(String.valueOf(IdType.valueOf(String.valueOf(request.getIdType()))));
        user.setIdValue(encryptionService.encrypt(request.getIdValue()));
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    private void validateSignUpRequest(SignUpRequest request) {
        if (!isValidEmail(request.getUserId())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!Arrays.asList("REG_NO", "BUSINESS_NO").contains(request.getIdType())) {
            throw new IllegalArgumentException("Invalid idType");
        }
        if (request.getIdType().equals("REG_NO") && !isValidRegNo(request.getIdValue())) {
            throw new IllegalArgumentException("Invalid registration number");
        }
        if (request.getIdType().equals("BUSINESS_NO") && !isValidBusinessNo(request.getIdValue())) {
            throw new IllegalArgumentException("Invalid business number");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidRegNo(String regNo) {
        // Implement registration number validation logic
        return true;
    }

    private boolean isValidBusinessNo(String businessNo) {
        // Implement business number validation logic
        return true;
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setIdType(String.valueOf(user.getIdType()));
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }
}
