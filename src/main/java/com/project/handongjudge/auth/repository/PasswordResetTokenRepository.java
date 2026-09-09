package com.project.handongjudge.auth.repository;

import com.project.handongjudge.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** 특정 사용자의 유효한(미사용, 미만료) 토큰 조회 */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.userId = :userId AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidTokenByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /** 만료된 토큰 일괄 삭제 (선택적 배치 정리용) */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /** 특정 사용자의 모든 토큰 삭제 (비밀번호 변경 후 정리) */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
