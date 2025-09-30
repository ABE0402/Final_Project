package com.example.hong.config;

import com.example.hong.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http


                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/signup",
                                "/error", "/favicon.ico",       // ✅ 추가
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/oauth2/**","/uploads/**"
                        ).permitAll()
                        .requestMatchers("/mypage", "/owner/**").authenticated() // 마이페이지/점주신청은 로그인 필요
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자
                        .requestMatchers("/owner/**").hasAnyRole("OWNER","ADMIN")
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((req, res, authn) -> {
                            var auths = authn.getAuthorities();
                            boolean isAdmin = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            boolean isOwner = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
                            res.sendRedirect(isAdmin ? "/admin" : (isOwner ? "/owner" : "/"));
                        })
                        .failureUrl("/login?error=1")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = authentication.getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin");   //  관리자면 /admin
            } else {
                response.sendRedirect("/");        // 일반/점주면 /
            }
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
