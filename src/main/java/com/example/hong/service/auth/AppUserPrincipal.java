package com.example.hong.service.auth;


import com.example.hong.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AppUserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserPrincipal(Long id, String email, String password, String nickname,
                            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    // Spring Security uses this as “username” internally → 우리는 이메일 사용
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked()  { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public static AppUserPrincipal from(User u) {
        var role = new SimpleGrantedAuthority("ROLE_" + u.getRole().name());
        return new AppUserPrincipal(u.getId(), u.getEmail(), u.getPassword(), u.getNickname(),
                List.of(role));
    }
}