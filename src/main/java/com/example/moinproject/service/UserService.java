package com.example.moinproject.service;

import com.example.moinproject.config.exception.CustomAuthenticationException;
import com.example.moinproject.domain.dto.user.SignUpRequest;
import com.example.moinproject.domain.dto.user.UserDto;
import com.example.moinproject.domain.entity.Quote;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.domain.enums.IdType;
import com.example.moinproject.repository.UserRepository;
import com.example.moinproject.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    public UserDto signup(SignUpRequest request) {
        validateSignUpRequest(request);

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("해당 유저가 이미 존재합니다.");
        }

        User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .idType(String.valueOf(IdType.valueOf(String.valueOf(request.getIdType()))))
                .idValue(encryptionService.encrypt(request.getIdValue()))
                .name(request.getName())
                .build();

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public User authenticateUser(String userId, String password) throws AuthenticationException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomAuthenticationException("유저가 존재하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomAuthenticationException("암호가 올바르지 않습니다.");
        }

        return user;
    }

    private void validateSignUpRequest(SignUpRequest request) {
        if (!isValidEmail(request.getUserId())) {
            throw new IllegalArgumentException("이메일 형식이 아닙니다.");
        }
        if (!Arrays.asList("REG_NO", "BUSINESS_NO").contains(request.getIdType())) {
            throw new IllegalArgumentException("ID 타입이 올바르지 않습니다.");
        }
        if (request.getIdType().equals("REG_NO") && !isValidRegNo(request.getIdValue())) {
            throw new IllegalArgumentException("주민등록번호가 올바르지 않습니다.");
        }
        if (request.getIdType().equals("BUSINESS_NO") && !isValidBusinessNo(request.getIdValue())) {
            throw new IllegalArgumentException("사업자등록번호가 올바르지 않습니다.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidRegNo(String regNo) {
        String regNoRegex = "\\d{6}[-]\\d{7}";
        return Pattern.matches(regNoRegex, regNo);
    }

    private boolean isValidBusinessNo(String businessNo) {
        String businessNoRegex = "^\\d{3}-\\d{2}-\\d{5}$";
        return Pattern.matches(businessNoRegex, businessNo);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setIdType(String.valueOf(user.getIdType()));
        dto.setName(user.getName());
        return dto;
    }



    public BigDecimal getDailyTransferAmount(User user) {
        return userRepository.findByUserId(user.getUserId())
            .map(u -> u.getQuotes().stream()
            .map(Quote::getTargetAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add))
            .orElse(BigDecimal.ZERO);
    }
}
