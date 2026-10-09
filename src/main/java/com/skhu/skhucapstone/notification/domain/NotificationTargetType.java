package com.skhu.skhucapstone.notification.domain;

// 알림을 눌렀을 때 이동할 화면의 종류. 프론트엔드가 targetId와 묶어 경로를 만든다.
public enum NotificationTargetType {
    POST,
    CLUB,
    CLUB_EVENT,
    CLUB_COLLABORATION,
    PROJECT_RECRUITMENT,
    CHAT_ROOM
}
