package com.skhu.skhucapstone.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 인증번호는 5자리라 경우의 수가 9만 가지뿐이다.
    // 횟수 제한이 없으면 만료 전에 전부 대입해볼 수 있으므로 시도 횟수를 센다.
    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private int attemptCount = 0;

    public static final int MAX_ATTEMPT = 5;

    // 재발송마다 번호가 새로 나가고 시도 횟수도 초기화되므로,
    // 발송 자체에 상한이 없으면 시도 횟수 제한이 사실상 무의미해진다.
    // 메일 계정에는 하루 발송 한도가 있어 한 사람이 다 써버리면 모두가 인증을 못 한다.
    @Builder.Default
    @ColumnDefault("1")
    @Column(nullable = false)
    private int sendCount = 1;

    // 상한을 세는 24시간 구간의 시작점. 기존 행에는 값이 없을 수 있어 null을 허용한다.
    private LocalDateTime windowStartedAt;

    public static final int MAX_SEND_PER_WINDOW = 10;
    private static final int WINDOW_HOURS = 24;

    // 틀린 횟수를 올리는 일은 여기서 하지 않는다. 인증 실패는 예외를 던져 트랜잭션을
    // 되돌리므로, 엔티티를 고치는 방식으로는 기록이 남지 않는다.
    // EmailVerificationAttemptRecorder가 별도 트랜잭션에서 처리한다.
    public boolean isAttemptExceeded() {
        return this.attemptCount >= MAX_ATTEMPT;
    }

    public boolean isSendLimitExceeded() {
        return isWithinWindow() && this.sendCount >= MAX_SEND_PER_WINDOW;
    }

    // 같은 주소로 번호를 다시 내준다. 행을 지우고 새로 만들면 발송 횟수가 초기화되어
    // 상한을 둔 의미가 없어지므로, 기존 행을 그대로 두고 내용만 바꾼다.
    public void reissue(String code, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();

        if (!isWithinWindow()) {
            this.windowStartedAt = now;
            this.sendCount = 0;
        }

        this.code = code;
        this.expiresAt = expiresAt;
        this.createdAt = now;
        this.attemptCount = 0;
        this.sendCount++;
    }

    private boolean isWithinWindow() {
        return this.windowStartedAt != null
                && this.windowStartedAt.isAfter(LocalDateTime.now().minusHours(WINDOW_HOURS));
    }
}
