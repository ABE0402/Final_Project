package com.example.hong.repository;

import com.example.hong.domain.AuthProvider;
import com.example.hong.entity.AuthIdentity;
import com.example.hong.domain.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {
    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
