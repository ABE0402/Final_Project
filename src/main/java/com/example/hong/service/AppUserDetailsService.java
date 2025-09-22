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

    /**
     * 여기의 파라미터 email 값은 SecurityConfig의
     * .usernameParameter("email") 설정에서 넘어온다.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자가 없습니다."));

        if (u.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new DisabledException("정지된 계정입니다.");
        }

        // 권한 부여 (DB의 ENUM: USER/OWNER/ADMIN → ROLE_USER/ROLE_OWNER/ROLE_ADMIN)
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));

        // 커스텀 Principal에 닉네임 포함
        return new AppUserPrincipal(
                u.getId(),
                u.getEmail(),     // 인증 아이디(이메일)
                u.getPassword(),  // 반드시 BCrypt로 저장되어 있어야 함
                u.getNickname(),  // 화면 표시용
                authorities
        );


    }
}
