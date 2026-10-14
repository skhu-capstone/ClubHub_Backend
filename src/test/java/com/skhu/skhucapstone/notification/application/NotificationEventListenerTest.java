package com.skhu.skhucapstone.notification.application;

import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

// 알림 저장이 실패해도 본 작업(가입 신청, 댓글 등)에 영향이 가지 않는지 검증한다.
@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationWriter notificationWriter;

    @InjectMocks
    private NotificationEventListener listener;

    private NotificationCreateEvent event() {
        return new NotificationCreateEvent(
                List.of(1L),
                NotificationType.CLUB_JOIN_REQUEST,
                "김민수님이 가입을 신청했습니다.",
                NotificationTargetType.CLUB,
                4L);
    }

    @Test
    @DisplayName("알림 저장이 실패해도 예외가 밖으로 나가지 않는다")
    void swallowsFailure() {
        // 알림 테이블에 락이 걸려 가입 신청이 전부 500으로 떨어진 적이 있다.
        doThrow(new CannotAcquireLockException("Lock wait timeout exceeded"))
                .when(notificationWriter).save(any(), any(), any(), any(), any());

        assertThatCode(() -> listener.handle(event())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이벤트에 담긴 내용 그대로 저장을 맡긴다")
    void delegatesToWriter() {
        listener.handle(event());

        verify(notificationWriter).save(
                List.of(1L),
                NotificationType.CLUB_JOIN_REQUEST,
                "김민수님이 가입을 신청했습니다.",
                NotificationTargetType.CLUB,
                4L);
    }
}
