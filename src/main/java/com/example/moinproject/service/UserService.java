package com.example.moinproject.service;

import com.example.moinproject.config.exception.CustomAuthenticationException;
import com.example.moinproject.domain.dto.user.SignUpRequest;
import com.example.moinproject.domain.dto.user.UserDto;
import com.example.moinproject.domain.entity.Quote;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.domain.enums.IdType;
import com.example.moinproject.repository.UserRepository;
import com.example.moinproject.util.EncryptionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Key;
import java.util.Arrays;
import java.util.Comparator;
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

    private Key key;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public UserDto signup(SignUpRequest request) {
        validateSignUpRequest(request);

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User already exists with this email");
        }

        User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .idType(String.valueOf(IdType.valueOf(String.valueOf(request.getIdType()))))
                .idValue(encryptionService.encrypt(request.getIdValue()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber()).build();

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public User authenticateUser(String userId, String password) throws AuthenticationException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomAuthenticationException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomAuthenticationException("Invalid password");
        }

        return user;
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
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }



    public BigDecimal getDailyTransferAmount(User user) {
        Optional<User> userId = userRepository.findByUserId(user.getUserId());
        List<Quote> quotes = userId.get().getQuotes();
        if (quotes.size() >= 2) {
            Quote secondLastQuote = quotes.get(quotes.size() - 2);
            return secondLastQuote.getTargetAmount();
        }
        return BigDecimal.valueOf(0);
    }

}
