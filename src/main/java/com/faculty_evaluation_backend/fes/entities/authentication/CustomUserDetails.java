package com.faculty_evaluation_backend.fes.entities.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final UserAccounts userAccounts;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return Collections.singletonList(new SimpleGrantedAuthority(userAccounts.getRole().name()));
    }

    @Override
    public String getPassword(){
        return userAccounts.getPassword();
    }

    @Override
    public String getUsername(){
        return userAccounts.getUsername();
    }
    public String getEmail(){
        return userAccounts.getEmail();
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }
    @Override
    public boolean isAccountNonLocked(){
        return !Boolean.TRUE.equals(userAccounts.getIsLocked());
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(userAccounts.getIsEnabled());
    }
    public Long getUserId(){
        return userAccounts.getUserId();
    }
    public UserAccounts getUser(){
        return userAccounts;
    }
}
