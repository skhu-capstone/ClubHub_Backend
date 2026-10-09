package com.skhu.skhucapstone.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    LOGIN_SUCCESS(HttpStatus.OK, "LOGIN_SUCCESS", "구글 소셜 로그인이 완료되었습니다."),

    // 인증번호
    EMAIL_SEND_SUCCESS(HttpStatus.OK, "EMAIL_SEND_SUCCESS", "인증번호가 발송되었습니다."),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK, "EMAIL_VERIFY_SUCCESS", "학교 이메일 인증이 완료되었습니다."),
    EMAIL_RESEND_SUCCESS(HttpStatus.OK, "EMAIL_RESEND_SUCCESS", "인증번호가 재발송되었습니다."),

    // 커피챗
    COFFEECHAT_PROFILE_SAVE_SUCCESS(HttpStatus.OK, "COFFEECHAT_PROFILE_SAVE_SUCCESS", "커피챗 프로필이 저장되었습니다."),
    COFFEECHAT_PROFILE_VISIBILITY_UPDATE_SUCCESS(HttpStatus.OK, "COFFEECHAT_PROFILE_VISIBILITY_UPDATE_SUCCESS", "커피챗 프로필 공개 여부가 변경되었습니다."),
    COFFEECHAT_PROFILE_GET_SUCCESS(HttpStatus.OK, "COFFEECHAT_PROFILE_GET_SUCCESS", "커피챗 프로필 조회가 완료되었습니다."),
    COFFEECHAT_PROFILE_LIST_GET_SUCCESS(HttpStatus.OK, "COFFEECHAT_PROFILE_LIST_GET_SUCCESS", "커피챗 프로필 목록 조회가 완료되었습니다."),
    COFFEECHAT_PROFILE_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "COFFEECHAT_PROFILE_IMAGE_UPLOAD_SUCCESS", "커피챗 프로필 이미지가 업로드되었습니다."),
    COFFEECHAT_PROFILE_IMAGE_DELETE_SUCCESS(HttpStatus.OK, "COFFEECHAT_PROFILE_IMAGE_DELETE_SUCCESS", "커피챗 프로필 이미지가 삭제되었습니다."),
    CLUB_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "CLUB_IMAGE_UPLOAD_SUCCESS", "동아리 이미지가 업로드되었습니다."),
    CLUB_COLLAB_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_IMAGE_UPLOAD_SUCCESS", "협업 모집글 이미지가 업로드되었습니다."),
    PROJECT_RECRUITMENT_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "PROJECT_RECRUITMENT_IMAGE_UPLOAD_SUCCESS", "프로젝트 팀원 모집글 이미지가 업로드되었습니다."),
    POST_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "POST_IMAGE_UPLOAD_SUCCESS", "게시글 이미지가 업로드되었습니다."),

    // Post
    POST_CREATE_SUCCESS(HttpStatus.OK, "POST_CREATE_SUCCESS", "게시글 작성이 완료되었습니다."),
    POST_LIST_GET_SUCCESS(HttpStatus.OK, "POST_LIST_GET_SUCCESS", "게시글 전체 목록 조회가 완료되었습니다."),
    POST_CLUB_LIST_GET_SUCCESS(HttpStatus.OK, "POST_CLUB_LIST_GET_SUCCESS", "동아리별 게시물 조회가 완료되었습니다."),
    POST_DETAIL_GET_SUCCESS(HttpStatus.OK, "POST_DETAIL_GET_SUCCESS", "게시물 상세 조회가 완료되었습니다."),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "POST_UPDATE_SUCCESS", "게시글 수정이 완료되었습니다."),
    POST_DELETE_SUCCESS(HttpStatus.OK, "POST_DELETE_SUCCESS", "게시글 삭제가 완료되었습니다."),

    //Commment
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "COMMENT_DELETE_SUCCESS", "댓글 삭제가 완료 되었습니다."),
    COMMENT_CREATE_SUCCESS(HttpStatus.OK, "COMMENT_CREATE_SUCCESS", "댓글 작성이 완료 되었습니다."),
    COMMENT_LIST_GET_SUCCESS(HttpStatus.OK, "COMMENT_LIST_GET_SUCCESS", "댓글 목록 조회가 완료 되었습니다."),

    // Like
    LIKE_TOGGLE_SUCCESS(HttpStatus.OK, "LIKE_TOGGLE_SUCCESS", "좋아요 상태가 변경되었습니다."),

    // Club
    CLUB_CREATE_SUCCESS(HttpStatus.OK, "CLUB_CREATE_SUCCESS", "동아리 생성이 완료되었습니다."),
    CLUB_LIST_GET_SUCCESS(HttpStatus.OK, "CLUB_LIST_GET_SUCCESS", "동아리 목록 조회가 완료되었습니다."),
    CLUB_DETAIL_GET_SUCCESS(HttpStatus.OK, "CLUB_DETAIL_GET_SUCCESS", "동아리 상세 조회가 완료되었습니다."),
    MY_CLUB_LIST_GET_SUCCESS(HttpStatus.OK, "MY_CLUB_LIST_GET_SUCCESS", "내 동아리 목록 조회가 완료되었습니다."),
    CLUB_UPDATE_SUCCESS(HttpStatus.OK, "CLUB_UPDATE_SUCCESS", "동아리 정보가 수정되었습니다."),
    CLUB_EVENT_CREATE_SUCCESS(HttpStatus.OK, "CLUB_EVENT_CREATE_SUCCESS", "동아리 일정이 생성되었습니다."),
    CLUB_EVENT_LIST_GET_SUCCESS(HttpStatus.OK, "CLUB_EVENT_LIST_GET_SUCCESS", "동아리 일정 목록 조회가 완료되었습니다."),
    CLUB_EVENT_DETAIL_GET_SUCCESS(HttpStatus.OK, "CLUB_EVENT_DETAIL_GET_SUCCESS", "동아리 일정 상세 조회가 완료되었습니다."),
    CLUB_EVENT_UPDATE_SUCCESS(HttpStatus.OK, "CLUB_EVENT_UPDATE_SUCCESS", "동아리 일정이 수정되었습니다."),
    CLUB_EVENT_DELETE_SUCCESS(HttpStatus.OK, "CLUB_EVENT_DELETE_SUCCESS", "동아리 일정이 삭제되었습니다."),

    // ClubMember
    CLUB_MEMBER_LIST_GET_SUCCESS(HttpStatus.OK, "CLUB_MEMBER_LIST_GET_SUCCESS", "동아리 멤버 목록 조회가 완료되었습니다."),
    CLUB_JOIN_REQUEST_SUCCESS(HttpStatus.OK, "CLUB_JOIN_REQUEST_SUCCESS", "동아리 가입 신청이 완료되었습니다."),
    CLUB_JOIN_CANCEL_SUCCESS(HttpStatus.OK, "CLUB_JOIN_CANCEL_SUCCESS", "동아리 가입 신청이 취소되었습니다."),
    MY_CLUB_JOIN_LIST_GET_SUCCESS(HttpStatus.OK, "MY_CLUB_JOIN_LIST_GET_SUCCESS", "내 동아리 가입 신청 목록 조회가 완료되었습니다."),
    CLUB_JOIN_LIST_GET_SUCCESS(HttpStatus.OK, "CLUB_JOIN_LIST_GET_SUCCESS", "동아리 가입 신청자 목록 조회가 완료되었습니다."),
    CLUB_JOIN_APPROVE_SUCCESS(HttpStatus.OK, "CLUB_JOIN_APPROVE_SUCCESS", "동아리 가입 신청이 승인되었습니다."),
    CLUB_JOIN_REJECT_SUCCESS(HttpStatus.OK, "CLUB_JOIN_REJECT_SUCCESS", "동아리 가입 신청이 거절되었습니다."),
    CLUB_MEMBER_ROLE_UPDATE_SUCCESS(HttpStatus.OK, "CLUB_MEMBER_ROLE_UPDATE_SUCCESS", "동아리 멤버 역할이 변경되었습니다."),
    CLUB_PRESIDENT_TRANSFER_SUCCESS(HttpStatus.OK, "CLUB_PRESIDENT_TRANSFER_SUCCESS", "동아리 대표 권한이 이전되었습니다."),
    CLUB_MEMBER_REMOVE_SUCCESS(HttpStatus.OK, "CLUB_MEMBER_REMOVE_SUCCESS", "동아리 멤버가 내보내기 처리되었습니다."),

    //Main
    MAIN_GET_SUCCESS(HttpStatus.OK, "MAIN_GET_SUCCESS", "메인페이지 조회가 완료되었습니다."),

    // 협업모집
    CLUB_COLLAB_CREATE_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_CREATE_SUCCESS", "협업 모집글 작성이 완료되었습니다."),
    CLUB_COLLAB_LIST_GET_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_LIST_GET_SUCCESS", "협업 모집글 목록 조회가 완료되었습니다."),
    CLUB_COLLAB_DETAIL_GET_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_DETAIL_GET_SUCCESS", "협업 모집글 상세 조회가 완료되었습니다."),
    CLUB_COLLAB_UPDATE_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_UPDATE_SUCCESS", "협업 모집글 수정이 완료되었습니다."),
    CLUB_COLLAB_DELETE_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_DELETE_SUCCESS", "협업 모집글 삭제가 완료되었습니다."),
    CLUB_COLLAB_APPLY_SUCCESS(HttpStatus.OK, "CLUB_COLLAB_APPLY_SUCCESS", "협업 모집 문의가 완료되었습니다."),

    // 프로젝트 팀원 모집
    PROJECT_RECRUITMENT_CREATE_SUCCESS(HttpStatus.OK, "PROJECT_RECRUITMENT_CREATE_SUCCESS", "프로젝트 팀원 모집 글이 등록되었습니다."),
    PROJECT_RECRUITMENT_LIST_FETCH_SUCCESS(HttpStatus.OK, "PROJECT_RECRUITMENT_LIST_FETCH_SUCCESS", "모집 목록 조회에 성공했습니다."),
    PROJECT_RECRUITMENT_FETCH_SUCCESS(HttpStatus.OK, "PROJECT_RECRUITMENT_FETCH_SUCCESS", "모집 글 조회에 성공했습니다."),
    PROJECT_RECRUITMENT_UPDATE_SUCCESS(HttpStatus.OK, "PROJECT_RECRUITMENT_UPDATE_SUCCESS", "모집 글이 수정되었습니다."),
    PROJECT_RECRUITMENT_DELETE_SUCCESS(HttpStatus.OK, "PROJECT_RECRUITMENT_DELETE_SUCCESS", "모집 글이 삭제되었습니다."),

    // 마이페이지
    MYPAGE_GET_SUCCESS(HttpStatus.OK, "MYPAGE_GET_SUCCESS", "마이페이지 조회가 완료되었습니다."),
    MYPAGE_NAME_UPDATE_SUCCESS(HttpStatus.OK, "MYPAGE_NAME_UPDATE_SUCCESS", "닉네임 수정이 완료되었습니다."),

    // 채팅
    CHAT_ROOM_CREATE_SUCCESS(HttpStatus.OK, "CHAT_ROOM_CREATE_SUCCESS", "채팅방이 생성되었습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.OK, "CHAT_ROOM_ALREADY_EXISTS", "기존 채팅방을 반환합니다."),
    CHAT_ROOM_LIST_FETCH_SUCCESS(HttpStatus.OK, "CHAT_ROOM_LIST_FETCH_SUCCESS", "채팅방 목록 조회에 성공했습니다."),
    CHAT_MESSAGE_SEND_SUCCESS(HttpStatus.OK, "CHAT_MESSAGE_SEND_SUCCESS", "메시지 전송에 성공했습니다."),
    CHAT_MESSAGE_LIST_FETCH_SUCCESS(HttpStatus.OK, "CHAT_MESSAGE_LIST_FETCH_SUCCESS", "채팅 메시지 목록 조회에 성공했습니다."),
    CHAT_MESSAGE_READ_SUCCESS(HttpStatus.OK, "CHAT_MESSAGE_READ_SUCCESS", "메시지를 읽음 처리했습니다."),

    // 알림
    NOTIFICATION_LIST_GET_SUCCESS(HttpStatus.OK, "NOTIFICATION_LIST_GET_SUCCESS", "알림 목록 조회에 성공했습니다."),
    NOTIFICATION_UNREAD_COUNT_GET_SUCCESS(HttpStatus.OK, "NOTIFICATION_UNREAD_COUNT_GET_SUCCESS", "안 읽은 알림 개수 조회에 성공했습니다."),
    NOTIFICATION_READ_SUCCESS(HttpStatus.OK, "NOTIFICATION_READ_SUCCESS", "알림을 읽음 처리했습니다."),
    NOTIFICATION_READ_ALL_SUCCESS(HttpStatus.OK, "NOTIFICATION_READ_ALL_SUCCESS", "모든 알림을 읽음 처리했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
