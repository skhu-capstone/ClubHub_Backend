package com.skhu.skhucapstone.notification.application;

import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.notification.domain.Notification;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.notification.domain.repository.NotificationRepository;
import com.skhu.skhucapstone.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 알림이 누구에게 남고 누구에게 남지 않는지, 그리고 읽음 처리 권한을 검증한다.
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private static final Long OWNER_ID = 1L;
    private static final Long ACTOR_ID = 2L;
    private static final Long OTHER_ID = 3L;

    private User user(Long userId, String name) {
        return User.builder().userId(userId).name(name).build();
    }

    private Notification notification(User receiver) {
        return Notification.builder()
                .notificationId(100L)
                .receiver(receiver)
                .type(NotificationType.POST_COMMENT)
                .message("댓글이 달렸습니다.")
                .targetType(NotificationTargetType.POST)
                .targetId(10L)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("다른 사람이 한 행동은 알림으로 남는다")
    void notifiesWhenActorIsSomeoneElse() {
        notificationService.notify(
                user(OWNER_ID, "정다운"),
                user(ACTOR_ID, "김민수"),
                NotificationType.POST_COMMENT,
                "김민수님이 회원님의 게시글에 댓글을 남겼습니다.",
                NotificationTargetType.POST,
                10L);

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());

        assertThat(saved.getValue().getReceiver().getUserId()).isEqualTo(OWNER_ID);
        assertThat(saved.getValue().isRead()).isFalse();
    }

    @Test
    @DisplayName("내가 한 행동은 나에게 알림으로 남지 않는다")
    void doesNotNotifySelf() {
        User me = user(OWNER_ID, "정다운");

        notificationService.notify(
                me,
                me,
                NotificationType.POST_LIKE,
                "내가 내 글을 좋아합니다.",
                NotificationTargetType.POST,
                10L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("여러 명에게 보낼 때 행동한 본인만 빠진다")
    void notifyAllExcludesActor() {
        User writer = user(ACTOR_ID, "김민수");

        notificationService.notifyAll(
                List.of(user(OWNER_ID, "정다운"), writer, user(OTHER_ID, "이지은")),
                writer,
                NotificationType.CLUB_NOTICE_CREATED,
                "새 공지가 올라왔습니다.",
                NotificationTargetType.POST,
                10L);

        ArgumentCaptor<List<Notification>> saved = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(saved.capture());

        assertThat(saved.getValue())
                .extracting(n -> n.getReceiver().getUserId())
                .containsExactly(OWNER_ID, OTHER_ID);
    }

    @Test
    @DisplayName("받을 사람이 본인뿐이면 아무것도 저장하지 않는다")
    void notifyAllSkipsWhenOnlyActorRemains() {
        User writer = user(ACTOR_ID, "김민수");

        notificationService.notifyAll(
                List.of(writer),
                writer,
                NotificationType.CLUB_EVENT_CREATED,
                "새 일정이 등록되었습니다.",
                NotificationTargetType.CLUB_EVENT,
                5L);

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("본인에게 온 알림은 읽음으로 바뀐다")
    void marksOwnNotificationAsRead() {
        Notification notification = notification(user(OWNER_ID, "정다운"));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(100L, OWNER_ID);

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("남에게 온 알림은 읽음 처리할 수 없다")
    void rejectsReadingOthersNotification() {
        Notification notification = notification(user(OWNER_ID, "정다운"));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(100L, OTHER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_ACCESS_DENIED);

        assertThat(notification.isRead()).isFalse();
    }
}
