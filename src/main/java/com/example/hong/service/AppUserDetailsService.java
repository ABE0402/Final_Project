package com.example.hong.service;

import com.example.hong.domain.AccountStatus;
import com.example.hong.entity.User;
import com.example.hong.repository.UserRepository;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자가 없습니다."));

        if (u.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new DisabledException("정지된 계정입니다.");
        }

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));

        return new AppUserPrincipal(
                u.getId(),
                u.getEmail(),
                u.getPassword(),
                u.getNickname(),
                authorities
        );


    }
}
