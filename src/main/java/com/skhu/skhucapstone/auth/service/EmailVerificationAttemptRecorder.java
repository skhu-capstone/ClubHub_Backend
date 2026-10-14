package com.skhu.skhucapstone.auth.service;

import com.skhu.skhucapstone.auth.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 인증 실패 횟수를 세는 자리.
@Component
@RequiredArgsConstructor
public class EmailVerificationAttemptRecorder {

    private final EmailVerificationRepository emailVerificationRepository;

    // 인증에 실패하면 예외를 던져 트랜잭션이 롤백되므로, 같은 트랜잭션에서 횟수를 올리면
    // 그 증가분까지 함께 되돌아간다. 그러면 횟수 제한이 아무 역할도 하지 못한다.
    // 실패 기록만은 남아야 하므로 별도 트랜잭션에서 올린다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long verificationId) {
        emailVerificationRepository.increaseAttemptCount(verificationId);
    }
}
