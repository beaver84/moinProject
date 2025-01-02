package com.example.moinproject.domain.entity;

import com.example.moinproject.domain.enums.IdType;
import com.example.moinproject.domain.enums.Roles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private IdType idType;

    @Column(nullable = false)
    private String idValue;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Quote> quotes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transfer> transfers = new ArrayList<>();

    @Column
    protected Roles accountRole;

    @Builder
    public User(String userId, String password, IdType idType, String idValue, String name) {
        this.userId = userId;
        this.password = password;
        this.idType = idType;
        this.idValue = idValue;
        this.name = name;
        this.accountRole = Roles.USER;
    }

    public void addTransfer(Transfer transfer) {
        transfers.add(transfer);
        transfer.setUser(this);
    }

    public void removeTransfer(Transfer transfer) {
        transfers.remove(transfer);
        transfer.setUser(null);
    }

    public void addQuote(Quote quote) {
        quotes.add(quote);
        quote.setUser(this);
    }

    public void removeQuote(Quote quote) {
        quotes.remove(quote);
        quote.setUser(null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (accountRole == Roles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ADMIN"));
        }else{
            return List.of(new SimpleGrantedAuthority("USER"));
        }
    }

    @Override
    public String getUsername() {
        return this.userId;
    }
}

