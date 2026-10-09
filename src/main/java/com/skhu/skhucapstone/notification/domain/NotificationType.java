package com.skhu.skhucapstone.notification.domain;

// 알림 종류. 프론트엔드는 이 값으로 아이콘이나 분류를 구분한다.
public enum NotificationType {

    // 누군가 나에게 채팅을 걸어온 경우. 출처에 따라 세 가지로 나눈다.
    COFFEE_CHAT_REQUEST,
    CLUB_COLLABORATION_APPLY,
    PROJECT_RECRUITMENT_APPLY,

    // 내가 속한 동아리에 새 소식이 올라온 경우.
    CLUB_NOTICE_CREATED,
    CLUB_EVENT_CREATED,

    // 동아리 가입 절차.
    CLUB_JOIN_REQUEST,
    CLUB_JOIN_APPROVED,
    CLUB_JOIN_REJECTED,

    // 동아리 구성원 변동.
    CLUB_MEMBER_EXPELLED,
    CLUB_ROLE_CHANGED,

    // 내 게시글에 달린 반응.
    POST_COMMENT,
    POST_LIKE
}
