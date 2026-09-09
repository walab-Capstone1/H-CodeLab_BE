package com.project.handongjudge.auth.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰 엔티티 (DB 저장 방식)
 * - UUID 토큰을 DB에 저장하고 15분 TTL을 expires_at 컬럼으로 관리
 * - 사용 후 used=true 처리하여 재사용 방지
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_prt_token", columnList = "token"),
        @Index(name = "idx_prt_user_id", columnList = "user_id")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token; // UUID

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 토큰 만료 여부 */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /** 토큰 유효 여부 (미사용 + 미만료) */
    public boolean isValid() {
        return !used && !isExpired();
    }

    /** 토큰 사용 처리 */
    public void markAsUsed() {
        this.used = true;
    }
}
