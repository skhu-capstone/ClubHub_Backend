package com.skhu.skhucapstone.notification.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationUnreadCountResponse {

    // 헤더 종 아이콘 옆에 띄울 안 읽은 알림 개수.
    private long unreadCount;

    public static NotificationUnreadCountResponse of(long unreadCount) {
        return NotificationUnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }
}
