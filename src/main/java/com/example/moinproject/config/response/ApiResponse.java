package com.example.moinproject.config.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private String status;
    private String message;
    private Object data;

    // 기본 생성자 추가
    public ApiResponse() {}

    // data가 없는 경우를 위한 생성자
    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }
}
