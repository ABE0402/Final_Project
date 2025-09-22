package com.example.hong.repository;

import com.example.hong.domain.AccountStatus;
import com.example.hong.domain.UserRole;
import com.example.hong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    List<User> findAllByRoleOrderByCreatedAtDesc(UserRole role);
    List<User> findAllByRoleNotOrderByCreatedAtDesc(UserRole role); // ADMIN 제외 기본목록

    // 검색 + 필터 (ADMIN 제외)
    @Query("""
      SELECT u FROM User u
      WHERE u.role <> com.example.hong.domain.UserRole.ADMIN
        AND (:status IS NULL OR u.accountStatus = :status)
        AND (:roleFilter IS NULL OR u.role = :roleFilter)
        AND (
          :kw IS NULL OR :kw = '' OR
          lower(u.email)    LIKE lower(concat('%', :kw, '%')) OR
          lower(u.name)     LIKE lower(concat('%', :kw, '%')) OR
          lower(u.nickname) LIKE lower(concat('%', :kw, '%')) OR
          lower(u.phone)    LIKE lower(concat('%', :kw, '%'))
        )
      ORDER BY u.createdAt DESC
    """)
    List<User> searchUsers(@Param("kw") String kw,
                           @Param("status") AccountStatus status,
                           @Param("roleFilter") UserRole roleFilter);
}