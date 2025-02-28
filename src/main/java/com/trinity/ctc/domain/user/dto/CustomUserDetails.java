package com.trinity.ctc.domain.user.dto;

import com.trinity.ctc.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {

        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getStatus().name()));
        // 예: "ROLE_AVAILABLE", "ROLE_TEMPORARILY_UNAVAILABLE"
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {

        return String.valueOf(user.getKakaoId());
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }
}
