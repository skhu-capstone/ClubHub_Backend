package com.skhu.skhucapstone.notification.application;

import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;

import java.util.List;

// 알림을 남겨 달라는 요청. 본 작업이 커밋된 뒤에 처리된다.
// User 엔티티가 아니라 식별자만 담는다. 처리 시점에는 본 작업의 영속성 컨텍스트가
// 이미 닫혀 있어, 엔티티를 그대로 들고 가면 지연 로딩에서 터진다.
public record NotificationCreateEvent(
        List<Long> receiverIds,
        NotificationType type,
        String message,
        NotificationTargetType targetType,
        Long targetId
) {
}
