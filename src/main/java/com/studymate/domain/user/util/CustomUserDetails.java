package com.studymate.domain.user.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
    private final UUID uuid;
    private final String name;

    public CustomUserDetails(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUsername() {
        return uuid.toString();
    }
    public UUID getUuid() {
        return uuid;
    }
    public String getName() {
        return name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(() -> "USER");
    }
    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }




}
