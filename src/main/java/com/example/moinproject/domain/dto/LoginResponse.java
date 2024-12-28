package com.example.moinproject.domain.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private int resultCode;
    private String resultMsg;
    private String token;
}