package com.example.moinproject.domain.entity;

import com.example.moinproject.domain.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String idType;

    @Column(nullable = false)
    private String idValue;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quote> quotes = new ArrayList<>();

    @Builder
    public User(String userId, String password, String idType, String idValue, String name, String phoneNumber) {
        this.userId = userId;
        this.password = password;
        this.idType = idType;
        this.idValue = idValue;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void addQuote(Quote quote) {
        quotes.add(quote);
        quote.setUser(this);
    }

    public void removeQuote(Quote quote) {
        quotes.remove(quote);
        quote.setUser(null);
    }
}

