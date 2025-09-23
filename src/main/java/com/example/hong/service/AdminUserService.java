package com.example.hong.service;

import com.example.hong.domain.AccountStatus;
import com.example.hong.domain.UserRole;
import com.example.hong.entity.User;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

    /** OWNER → USER 권한 회수 */
    @Transactional
    public void demoteOwnerToUser(Long userId, String reason) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (u.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("관리자 권한은 이 화면에서 변경할 수 없습니다.");
        }
        if (u.getRole() != UserRole.OWNER) {
            // 이미 점주가 아니면 무시(또는 예외)
            return;
        }
        u.setRole(UserRole.USER);
        // TODO: reason을 저장하려면 AdminAudit 같은 테이블을 추가해 보관
    }


    public List<User> search(String q, AccountStatus status, UserRole roleFilter) {
        return userRepository.searchUsers(q, status, roleFilter);
    }

    @Transactional
    public void suspend(Long targetUserId, Long actorAdminId) {
        User u = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (u.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("관리자 계정은 정지할 수 없습니다.");
        }
        if (u.getId().equals(actorAdminId)) {
            throw new IllegalStateException("본인 계정은 정지할 수 없습니다.");
        }
        u.setAccountStatus(AccountStatus.SUSPENDED);
    }

    @Transactional
    public void resume(Long targetUserId) {
        User u = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        u.setAccountStatus(AccountStatus.ACTIVE);
    }
}
