package com.skhu.skhucapstone.auth.service;

import com.skhu.skhucapstone.auth.entity.EmailVerification;
import com.skhu.skhucapstone.auth.repository.EmailVerificationRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 인증번호를 무한히 대입하거나 메일을 무한히 보낼 수 없는지 검증한다.
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationAttemptRecorder attemptRecorder;

    @InjectMocks
    private AuthService authService;

    private static final Long USER_ID = 1L;
    private static final String SCHOOL_EMAIL = "test@office.skhu.ac.kr";

    private EmailVerification verification(String code, int attemptCount, LocalDateTime createdAt) {
        return verification(code, attemptCount, createdAt, 1);
    }

    private EmailVerification verification(
            String code, int attemptCount, LocalDateTime createdAt, int sendCount) {
        return EmailVerification.builder()
                .id(1L)
                .email(SCHOOL_EMAIL)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(createdAt)
                .attemptCount(attemptCount)
                .sendCount(sendCount)
                .windowStartedAt(LocalDateTime.now())
                .build();
    }

    private void loginUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).isVerified(false).build()));
    }

    @Test
    @DisplayName("남이 이미 인증한 학교 이메일은 인증할 수 없다")
    void rejectsSchoolEmailAlreadyLinked() {
        // 학교 이메일이 본인 확인 수단이므로 한 주소로 여러 계정이 인증되면 안 된다.
        when(userRepository.existsBySchoolEmail(SCHOOL_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.verifyEmail(SCHOOL_EMAIL, "12345", USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_LINKED);
    }

    @Test
    @DisplayName("인증번호를 틀리면 틀린 횟수가 별도로 기록된다")
    void countsFailedAttempt() {
        EmailVerification verification = verification("12345", 0, LocalDateTime.now());
        when(emailVerificationRepository.findByEmail(SCHOOL_EMAIL))
                .thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> authService.verifyEmail(SCHOOL_EMAIL, "99999", USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VERIFICATION_CODE);

        // 같은 트랜잭션에서 세면 위 예외로 롤백되어 기록이 사라진다.
        verify(attemptRecorder).record(verification.getId());
    }

    @Test
    @DisplayName("정해진 횟수를 넘겨 틀리면 맞는 번호를 넣어도 통과하지 못한다")
    void blocksAfterTooManyAttempts() {
        EmailVerification verification =
                verification("12345", EmailVerification.MAX_ATTEMPT, LocalDateTime.now());
        when(emailVerificationRepository.findByEmail(SCHOOL_EMAIL))
                .thenReturn(Optional.of(verification));

        assertThatThrownBy(() -> authService.verifyEmail(SCHOOL_EMAIL, "12345", USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("방금 보낸 주소로 다시 발송을 요청하면 거절한다")
    void rejectsSendWhileCooldown() {
        loginUser();
        when(emailVerificationRepository.findByEmail(SCHOOL_EMAIL))
                .thenReturn(Optional.of(verification("12345", 0, LocalDateTime.now())));

        assertThatThrownBy(() -> authService.sendVerificationEmail(SCHOOL_EMAIL, USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);

        verify(emailService, never()).sendVerificationCode(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("간격이 지난 뒤에는 다시 발송된다")
    void sendsAfterCooldown() {
        loginUser();
        EmailVerification existing =
                verification("12345", 3, LocalDateTime.now().minusMinutes(5), 2);
        when(emailVerificationRepository.findByEmail(SCHOOL_EMAIL))
                .thenReturn(Optional.of(existing));

        authService.sendVerificationEmail(SCHOOL_EMAIL, USER_ID);

        verify(emailService).sendVerificationCode(anyString(), anyString(), anyInt());
        // 번호를 다시 내줄 때 틀린 횟수는 초기화되고 발송 횟수는 쌓여야 한다.
        assertThat(existing.getAttemptCount()).isZero();
        assertThat(existing.getSendCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("하루 발송 상한을 넘기면 더 보내지 않는다")
    void rejectsWhenDailySendLimitReached() {
        // 발송에 상한이 없으면 재발송으로 번호를 계속 새로 받아
        // 시도 횟수 제한을 우회할 수 있고, 메일 계정의 하루 한도도 소진된다.
        loginUser();
        when(emailVerificationRepository.findByEmail(SCHOOL_EMAIL))
                .thenReturn(Optional.of(verification(
                        "12345", 0, LocalDateTime.now().minusMinutes(5),
                        EmailVerification.MAX_SEND_PER_WINDOW)));

        assertThatThrownBy(() -> authService.sendVerificationEmail(SCHOOL_EMAIL, USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);

        verify(emailService, never()).sendVerificationCode(anyString(), anyString(), anyInt());
    }
}
