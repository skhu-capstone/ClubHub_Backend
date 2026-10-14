package com.skhu.skhucapstone.auth.service;

import com.skhu.skhucapstone.auth.client.GoogleApiClient;
import com.skhu.skhucapstone.auth.client.GoogleUserInfo;
import com.skhu.skhucapstone.auth.dto.GoogleLoginRes;
import com.skhu.skhucapstone.auth.entity.EmailVerification;
import com.skhu.skhucapstone.auth.repository.EmailVerificationRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.common.jwt.JwtUtil;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.skhu.skhucapstone.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final GoogleApiClient googleApiClient;
    private final JwtUtil jwtUtil;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final EmailVerificationAttemptRecorder attemptRecorder;

    public GoogleLoginRes googleLogin(String googleAccessToken) {
        // TODO
        // Google API 토큰 검증, 사용자 조회
        GoogleUserInfo userInfo = googleApiClient.getUserInfo(googleAccessToken);

        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> registerNewUser(userInfo));

        // JWT 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 응답 반환
        return GoogleLoginRes.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .schoolEmail(user.getSchoolEmail())
                .isVerified(user.getIsVerified())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    // DB 유저 확인 후 있으면 로그인, 없으면 회원가입
    private User registerNewUser (GoogleUserInfo userInfo){
        return userRepository.save(
                User.builder()
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .profileImage(userInfo.getPicture())
                        .build()
            );
    }

    // 학교 메일 서버가 스팸 차단 때문에 메일을 몇 분 늦게 받아주는 경우가 있다.
    // 만료가 5분이던 때는 번호를 받는 순간 이미 만료돼 인증을 못 한 제보가 있었다.
    // 다만 길게 둘수록 대입을 시도할 시간을 주는 셈이라, 시도 횟수 제한과 함께 10분으로 잡는다.
    private static final int VERIFICATION_EXPIRY_MINUTES = 10;

    // 같은 주소로 연달아 발송 요청이 오는 것을 막는다.
    // 메일 계정에는 하루 발송 한도가 있어, 한 사람이 다 써버리면 모두가 인증을 못 하게 된다.
    private static final int RESEND_COOLDOWN_MINUTES = 1;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // 최초 인증번호 발송
    @Transactional
    public void sendVerificationEmail(String schoolEmail, Long userId){
        // 학교 이메일 도메인 검증
        if(!schoolEmail.endsWith("@office.skhu.ac.kr")){
            throw new CustomException(ErrorCode.INVALID_SCHOOL_EMAIL);
        }

        // 이미 인증된 이메일인지
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        if(user.getIsVerified()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED_EMAIL);
        }

        // 남이 이미 인증한 주소면 번호를 보낼 필요가 없다.
        validateNotLinkedToAnotherUser(schoolEmail);

        String code = issueCode(schoolEmail);

        emailService.sendVerificationCode(schoolEmail, code, VERIFICATION_EXPIRY_MINUTES);
    }

    // 이메일 검증
    @Transactional
    public void verifyEmail(String schoolEmail, String code, Long userId) {
        validateNotLinkedToAnotherUser(schoolEmail);

        EmailVerification verification = emailVerificationRepository.findByEmail(schoolEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.VERIFICATION_EXPIRED);
        }

        if (verification.isAttemptExceeded()) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        if (!verification.getCode().equals(code)) {
            // 틀린 횟수를 세어 두지 않으면 만료 전에 번호를 전부 대입해볼 수 있다.
            // 아래 예외로 이 트랜잭션은 롤백되므로, 기록은 별도 트랜잭션에 맡긴다.
            attemptRecorder.record(verification.getId());
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        user.verifySchoolEmail(schoolEmail);  // dirty checking으로 자동 UPDATE

        emailVerificationRepository.deleteByEmail(schoolEmail);
    }

    // 인증번호 재발송
    @Transactional
    public void resendVerificationEmail(String schoolEmail, Long userId) {
        // 도메인 검증
        if (!schoolEmail.endsWith("@office.skhu.ac.kr")) {
            throw new CustomException(ErrorCode.INVALID_SCHOOL_EMAIL);
        }

        validateNotLinkedToAnotherUser(schoolEmail);

        // 기존 인증 요청 확인
        emailVerificationRepository.findByEmail(schoolEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        String code = issueCode(schoolEmail);

        emailService.sendVerificationCode(schoolEmail, code, VERIFICATION_EXPIRY_MINUTES);
    }

    // 발송 제한을 확인하고 번호를 내준다. 최초 발송과 재발송이 같은 규칙을 따라야
    // 한쪽으로만 우회해서 메일을 무한히 보내는 일이 생기지 않는다.
    private String issueCode(String schoolEmail) {
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_EXPIRY_MINUTES);

        EmailVerification verification = emailVerificationRepository.findByEmail(schoolEmail)
                .orElse(null);

        if (verification == null) {
            emailVerificationRepository.save(EmailVerification.builder()
                    .email(schoolEmail)
                    .code(code)
                    .expiresAt(expiresAt)
                    .createdAt(LocalDateTime.now())
                    .windowStartedAt(LocalDateTime.now())
                    .build());

            return code;
        }

        validateCooldown(verification);

        if (verification.isSendLimitExceeded()) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        verification.reissue(code, expiresAt);

        return code;
    }

    // 학교 이메일은 본인 확인 수단이므로 한 주소에 한 계정만 연결되어야 한다.
    // 검사가 없던 동안 같은 주소로 여러 계정이 인증된 사례가 실제로 있었다.
    private void validateNotLinkedToAnotherUser(String schoolEmail) {
        if (userRepository.existsBySchoolEmail(schoolEmail)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_LINKED);
        }
    }

    private void validateCooldown(EmailVerification verification) {
        LocalDateTime sentRecently = LocalDateTime.now().minusMinutes(RESEND_COOLDOWN_MINUTES);

        if (verification.getCreatedAt().isAfter(sentRecently)) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private String generateCode() {
        // 인증번호는 추측 가능하면 안 되므로 예측 불가능한 난수를 쓴다.
        // Random은 시드만 알면 다음 값을 계산할 수 있어 적절하지 않다.
        // 자릿수는 프론트 입력창에 맞춰 5자리를 유지한다. 경우의 수가 9만 가지라
        // 그 자체로는 넉넉하지 않지만, 시도 횟수 제한이 실질적인 방어선이다.
        return String.valueOf(10000 + SECURE_RANDOM.nextInt(90000));
    }
}
