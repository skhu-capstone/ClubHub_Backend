package com.skhu.skhucapstone.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 알림 저장을 본 작업에서 떼어내는 자리.
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationWriter notificationWriter;

    // AFTER_COMMIT이라 본 작업이 성공했을 때만 실행된다.
    // 가입 신청이 중간에 실패하면 알림도 남지 않는다.
    // fallbackExecution은 트랜잭션 없이 호출된 경우에도 알림이 빠지지 않게 한다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(NotificationCreateEvent event) {
        try {
            notificationWriter.save(
                    event.receiverIds(),
                    event.type(),
                    event.message(),
                    event.targetType(),
                    event.targetId());
        } catch (Exception e) {
            // 알림은 부가 기능이다. 여기서 터져도 이미 커밋된 본 작업에는 영향이 없고,
            // 영향을 주어서도 안 된다. 원인을 추적할 수 있도록 로그만 남긴다.
            log.warn("알림 저장에 실패했다. type={}, targetType={}, targetId={}, 받는사람={}",
                    event.type(), event.targetType(), event.targetId(), event.receiverIds(), e);
        }
    }
}
