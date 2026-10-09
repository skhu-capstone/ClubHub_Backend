package com.skhu.skhucapstone.notification.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationPageResponse {

    private List<NotificationResponse> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean last;

    // 목록과 함께 뱃지 숫자도 맞출 수 있도록 안 읽은 개수를 같이 내려준다.
    private long unreadCount;
}
