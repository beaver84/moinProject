package com.example.moinproject.domain.enums;

public enum Roles {
    ADMIN("관리자"),
    USER("유저");

    Roles(String description) {
        this.description = description;
    }

    private final String description;

    public String getDescription() {
        return description;
    }
}
