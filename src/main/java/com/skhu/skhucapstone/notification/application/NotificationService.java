package com.skhu.skhucapstone.notification.application;

import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.notification.api.dto.response.NotificationPageResponse;
import com.skhu.skhucapstone.notification.api.dto.response.NotificationResponse;
import com.skhu.skhucapstone.notification.domain.Notification;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.notification.domain.repository.NotificationRepository;
import com.skhu.skhucapstone.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationPageResponse getNotifications(Long userId, int page, int size) {
        validateSearchCondition(page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications =
                notificationRepository.findByReceiverUserIdOrderByCreatedAtDesc(userId, pageable);

        return NotificationPageResponse.builder()
                .content(notifications.getContent()
                        .stream()
                        .map(NotificationResponse::from)
                        .toList())
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .totalPages(notifications.getTotalPages())
                .last(notifications.isLast())
                .unreadCount(notificationRepository.countByReceiverUserIdAndIsReadFalse(userId))
                .build();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
    }

    // --- 아래는 다른 기능에서 알림을 남길 때 호출하는 메서드들 ---

    public void notify(
            User receiver,
            User actor,
            NotificationType type,
            String message,
            NotificationTargetType targetType,
            Long targetId
    ) {
        // 내가 한 일을 나에게 알릴 필요는 없다.
        if (isSamePerson(receiver, actor)) {
            return;
        }

        publish(List.of(receiver.getUserId()), type, message, targetType, targetId);
    }

    // 동아리 공지나 일정처럼 여러 명에게 같은 알림을 보내는 경우에 쓴다.
    public void notifyAll(
            List<User> receivers,
            User actor,
            NotificationType type,
            String message,
            NotificationTargetType targetType,
            Long targetId
    ) {
        List<Long> receiverIds = new ArrayList<>();

        for (User receiver : receivers) {
            if (isSamePerson(receiver, actor)) {
                continue;
            }

            receiverIds.add(receiver.getUserId());
        }

        if (receiverIds.isEmpty()) {
            return;
        }

        publish(receiverIds, type, message, targetType, targetId);
    }

    // 알림은 어디까지나 부가 기능이다. 여기서 바로 저장하지 않고 이벤트만 발행해,
    // 본 작업이 커밋된 뒤에 NotificationEventListener가 별도 트랜잭션으로 처리한다.
    // 이렇게 해야 두 가지가 동시에 지켜진다.
    //  - 알림 저장이 실패해도 가입 신청 같은 본 작업은 성공한다
    //    (실제로 알림 테이블에 락이 걸려 가입 신청이 전부 500으로 떨어진 적이 있다)
    //  - 본 작업이 실패하면 알림도 남지 않는다
    private void publish(
            List<Long> receiverIds,
            NotificationType type,
            String message,
            NotificationTargetType targetType,
            Long targetId
    ) {
        eventPublisher.publishEvent(new NotificationCreateEvent(
                receiverIds, type, truncate(message), targetType, targetId));
    }

    // 보관 기간이 지난 알림을 지우고 지운 건수를 돌려준다.
    @Transactional
    public int deleteOlderThan(LocalDateTime threshold) {
        return notificationRepository.deleteByCreatedAtBefore(threshold);
    }

    // 글 제목이 아주 긴 경우에도 저장에 실패하지 않도록 길이를 맞춘다.
    // 알림 하나 때문에 정작 중요한 댓글·지원 요청이 실패해서는 안 된다.
    private String truncate(String message) {
        if (message.length() <= Notification.MESSAGE_MAX_LENGTH) {
            return message;
        }

        return message.substring(0, Notification.MESSAGE_MAX_LENGTH - 1) + "…";
    }

    private boolean isSamePerson(User receiver, User actor) {
        return actor != null
                && receiver.getUserId().equals(actor.getUserId());
    }

    private void validateSearchCondition(int page, int size) {
        if (page < 0 || size < 1) {
            throw new CustomException(ErrorCode.INVALID_SEARCH_CONDITION);
        }
    }
}
