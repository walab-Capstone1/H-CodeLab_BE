package com.project.handongjudge.auth.service;

import com.project.handongjudge.auth.entity.PasswordResetToken;
import com.project.handongjudge.auth.repository.PasswordResetTokenRepository;
import com.project.handongjudge.common.EmailService;
import com.project.handongjudge.user.entity.User;
import com.project.handongjudge.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 비밀번호 재설정 서비스 (DB 토큰 방식)
 *
 * 흐름:
 * 1. requestPasswordReset(email) → UUID 토큰 생성 → DB 저장 → 이메일 발송
 * 2. resetPassword(token, newPassword) → 토큰 검증 → 비밀번호 변경 → 토큰 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expire-minutes:15}")
    private int tokenExpireMinutes;

    /**
     * 비밀번호 재설정 요청
     * - 이메일로 사용자 조회
     * - 기존 유효 토큰이 있으면 재사용 방지를 위해 삭제 후 새 토큰 발급
     * - 이메일 발송
     *
     * @param email 요청자 이메일
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일로 가입된 계정이 없습니다."));

        // LOCAL 가입자만 비밀번호 재설정 가능 (OAuth2 사용자는 소셜 계정에서 변경해야 함)
        if (user.getProvider() != User.AuthProvider.LOCAL) {
            throw new RuntimeException("소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다. " +
                    "가입하신 소셜 서비스에서 비밀번호를 변경해주세요.");
        }

        // 기존 미사용 토큰 삭제 (중복 발송 방지)
        passwordResetTokenRepository.deleteAllByUserId(user.getId());

        // 새 토큰 생성 및 저장
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .email(email)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpireMinutes))
                .build();
        passwordResetTokenRepository.save(resetToken);

        // 이메일 발송
        emailService.sendPasswordResetEmail(email, token);
        log.info("비밀번호 재설정 이메일 발송: email={}, userId={}", email, user.getId());
    }

    /**
     * 비밀번호 재설정 처리
     * - 토큰 유효성 검증 (존재, 미사용, 미만료)
     * - 새 비밀번호로 변경
     * - 사용한 토큰 삭제
     *
     * @param token       재설정 토큰 (UUID)
     * @param newPassword 새 비밀번호 (평문)
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 재설정 링크입니다."));

        if (!resetToken.isValid()) {
            if (resetToken.isExpired()) {
                throw new RuntimeException("재설정 링크가 만료되었습니다. 다시 요청해주세요.");
            }
            throw new RuntimeException("이미 사용된 재설정 링크입니다.");
        }

        // 비밀번호 변경
        userService.updatePassword(resetToken.getUserId(), newPassword);

        // 토큰 삭제 (사용 완료)
        passwordResetTokenRepository.deleteAllByUserId(resetToken.getUserId());
        log.info("비밀번호 재설정 완료: userId={}", resetToken.getUserId());
    }
}
