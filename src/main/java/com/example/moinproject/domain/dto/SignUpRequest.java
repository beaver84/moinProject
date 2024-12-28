package com.example.moinproject.domain.dto;

import com.example.moinproject.domain.enums.IdType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest {
    @Email(message = "유효한 이메일 형식이어야 합니다")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotNull(message = "ID 타입은 필수입니다")
    private String idType;

    @NotBlank(message = "ID 값은 필수입니다")
    private String idValue;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phoneNumber;
}