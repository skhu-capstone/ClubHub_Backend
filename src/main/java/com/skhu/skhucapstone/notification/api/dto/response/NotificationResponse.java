package com.skhu.skhucapstone.notification.api.dto.response;

import com.skhu.skhucapstone.notification.domain.Notification;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long notificationId;

    // 알림 종류. 아이콘이나 분류를 나눌 때 쓴다.
    private NotificationType type;

    // 화면에 그대로 보여주면 되는 문구.
    private String message;

    // 눌렀을 때 이동할 화면과 대상 ID.
    private NotificationTargetType targetType;

    private Long targetId;

    private boolean isRead;

    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .message(notification.getMessage())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
