package com.skhu.skhucapstone.notification.domain;

import com.skhu.skhucapstone.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(indexes = {
        // 목록은 항상 "내 알림을 최신순으로", 뱃지는 "내 안 읽은 알림 수"만 센다.
        @Index(name = "idx_notification_receiver_created", columnList = "receiver_id, createdAt"),
        @Index(name = "idx_notification_receiver_read", columnList = "receiver_id, isRead")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // 표시 문구는 알림을 만들 때 확정해 둔다. 나중에 원본 글이 수정되거나
    // 삭제돼도 "무엇 때문에 온 알림인지"는 그대로 남아야 하기 때문이다.
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public void markAsRead() {
        this.isRead = true;
    }
}
