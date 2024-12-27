package com.example.moinproject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IdType idType;

    @Column(nullable = false)
    private String idValue;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Builder
    public User(String userId, String password, IdType idType, String idValue, String name, String phoneNumber) {
        this.userId = userId;
        this.password = password;
        this.idType = idType;
        this.idValue = idValue;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}

