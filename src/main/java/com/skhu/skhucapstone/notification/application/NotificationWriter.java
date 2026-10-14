package com.skhu.skhucapstone.notification.application;

import com.skhu.skhucapstone.notification.domain.Notification;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.notification.domain.repository.NotificationRepository;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 알림 저장만 담당한다. 알림을 부르는 쪽(가입 신청, 댓글 등)과 트랜잭션을 분리하기 위해
// 따로 떼어낸 빈이다. 같은 클래스 안에서 호출하면 프록시를 타지 않아 분리가 되지 않는다.
@Component
@RequiredArgsConstructor
public class NotificationWriter {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // REQUIRES_NEW로 바깥 트랜잭션과 완전히 끊는다.
    // 같은 트랜잭션에 묶여 있으면 알림 저장이 실패한 순간 트랜잭션이 rollback-only로 찍혀,
    // 호출부에서 예외를 잡아도 커밋 때 결국 본 작업까지 실패한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(
            List<Long> receiverIds,
            NotificationType type,
            String message,
            NotificationTargetType targetType,
            Long targetId
    ) {
        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications = receiverIds.stream()
                .map(receiverId -> Notification.builder()
                        // 트랜잭션이 분리되면서 영속성 컨텍스트도 달라진다.
                        // 바깥에서 읽은 User를 그대로 쓰면 준영속 상태가 되므로,
                        // 외래 키만 필요한 여기서는 프록시 참조로 받는다. 조회도 일어나지 않는다.
                        .receiver(userRepository.getReferenceById(receiverId))
                        .type(type)
                        .message(message)
                        .targetType(targetType)
                        .targetId(targetId)
                        .isRead(false)
                        .createdAt(now)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }
}
