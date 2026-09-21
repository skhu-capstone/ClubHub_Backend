package com.skhu.skhucapstone.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Google 로그인
    INVALID_GOOGLE_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_TOKEN", "유효하지 않은 구글 토큰입니다."),
    FORBIDDEN_LOGIN(HttpStatus.FORBIDDEN, "FORBIDDEN_LOGIN", "허용되지 않은 로그인 요청입니다."),
    EMAIL_ALREADY_LINKED(HttpStatus.CONFLICT, "EMAIL_ALREADY_LINKED", "이미 다른 계정으로 가입된 이메일입니다."),

    // 학교 이메일
    INVALID_SCHOOL_EMAIL(HttpStatus.FORBIDDEN, "INVALID_SCHOOL_EMAIL", "학교 이메일 형식이 아닙니다."),
    ALREADY_VERIFIED_EMAIL(HttpStatus.CONFLICT, "ALREADY_VERIFIED_EMAIL", "이미 인증된 학교 이메일입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED", "인증번호 발송에 실패했습니다."),

    // 인증코드
    INVALID_VERIFICATION_CODE(HttpStatus.FORBIDDEN, "INVALID_VERIFICATION_CODE", "인증번호가 일치하지 않습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "VERIFICATION_NOT_FOUND", "인증 요청 정보를 찾을 수 없습니다."),
    VERIFICATION_EXPIRED(HttpStatus.GONE, "VERIFICATION_EXPIRED", "인증번호가 만료되었습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // 공통
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "올바르지 않은 요청입니다."),

    // 커피챗
    COFFEECHAT_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "COFFEECHAT_PROFILE_NOT_FOUND", "커피챗 프로필을 찾을 수 없습니다."),
    INVALID_COFFEECHAT_PROFILE_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_COFFEECHAT_PROFILE_REQUEST", "커피챗 프로필 요청 형식이 올바르지 않습니다."),
    COFFEECHAT_PROFILE_PRIVATE(HttpStatus.FORBIDDEN, "COFFEECHAT_PROFILE_PRIVATE", "비공개 커피챗 프로필입니다."),
    COFFEECHAT_PROFILE_IMAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "COFFEECHAT_PROFILE_IMAGE_FORBIDDEN", "본인의 커피챗 프로필 이미지만 변경할 수 있습니다."),


    // Club
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_NOT_FOUND", "해당 동아리를 찾을 수 없습니다."),
    CLUB_PRESIDENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "CLUB_PRESIDENT_ALREADY_EXISTS", "대표로 활동할 수 있는 동아리는 최대 2개입니다."),
    CLUB_MANAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_MANAGE_FORBIDDEN", "동아리 정보를 관리할 권한이 없습니다."),
    CLUB_EVENT_MANAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_EVENT_MANAGE_FORBIDDEN", "동아리 일정을 관리할 권한이 없습니다."),
    CLUB_EVENT_INVALID_TIME(HttpStatus.BAD_REQUEST, "CLUB_EVENT_INVALID_TIME", "일정 종료 시간은 시작 시간보다 늦어야 합니다."),
    CLUB_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_EVENT_NOT_FOUND", "해당 동아리 일정을 찾을 수 없습니다."),
    CLUB_EVENT_VIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_EVENT_VIEW_FORBIDDEN", "동아리 일정을 조회할 권한이 없습니다."),

    // ClubMember
    CLUB_JOIN_ALREADY_PENDING(HttpStatus.CONFLICT, "CLUB_JOIN_ALREADY_PENDING", "이미 가입 신청이 진행 중인 동아리입니다."),
    CLUB_MEMBER_ALREADY_JOINED(HttpStatus.CONFLICT, "CLUB_MEMBER_ALREADY_JOINED", "이미 가입한 동아리입니다."),
    CLUB_JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_JOIN_REQUEST_NOT_FOUND", "동아리 가입 신청 내역을 찾을 수 없습니다."),
    CLUB_JOIN_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "CLUB_JOIN_CANCEL_NOT_ALLOWED", "가입 대기 중인 신청만 취소할 수 있습니다."),
    CLUB_JOIN_LIST_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_JOIN_LIST_FORBIDDEN", "동아리 가입 신청자 목록을 조회할 권한이 없습니다."),
    CLUB_JOIN_MANAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_JOIN_MANAGE_FORBIDDEN", "동아리 가입 신청을 처리할 권한이 없습니다."),
    CLUB_JOIN_APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_JOIN_APPLICANT_NOT_FOUND", "해당 사용자의 가입 신청 내역을 찾을 수 없습니다."),
    CLUB_JOIN_NOT_PENDING(HttpStatus.CONFLICT, "CLUB_JOIN_NOT_PENDING", "가입 대기 중인 신청만 승인하거나 거절할 수 있습니다."),
    CLUB_MEMBER_ROLE_MANAGE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_MEMBER_ROLE_MANAGE_FORBIDDEN", "동아리 멤버 역할을 변경할 권한이 없습니다."),
    CLUB_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_MEMBER_NOT_FOUND", "해당 동아리의 멤버를 찾을 수 없습니다."),
    CLUB_MEMBER_ROLE_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "CLUB_MEMBER_ROLE_UPDATE_NOT_ALLOWED", "가입 완료된 일반 멤버와 운영진의 역할만 변경할 수 있습니다."),
    CLUB_MEMBER_SAME_ROLE(HttpStatus.CONFLICT, "CLUB_MEMBER_SAME_ROLE", "현재 역할과 동일한 역할로 변경할 수 없습니다."),
    CLUB_PRESIDENT_TRANSFER_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_PRESIDENT_TRANSFER_FORBIDDEN", "동아리 대표 권한을 이전할 권한이 없습니다."),
    CLUB_PRESIDENT_TRANSFER_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_PRESIDENT_TRANSFER_TARGET_NOT_FOUND", "대표 권한을 이전할 동아리 멤버를 찾을 수 없습니다."),
    CLUB_PRESIDENT_TRANSFER_NOT_ALLOWED(HttpStatus.CONFLICT, "CLUB_PRESIDENT_TRANSFER_NOT_ALLOWED", "가입 완료된 일반 멤버 또는 운영진에게만 대표 권한을 이전할 수 있습니다."),
    CLUB_PRESIDENT_TRANSFER_TO_SELF(HttpStatus.CONFLICT, "CLUB_PRESIDENT_TRANSFER_TO_SELF", "자기 자신에게 대표 권한을 이전할 수 없습니다."),
    CLUB_MEMBER_REMOVE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_MEMBER_REMOVE_FORBIDDEN", "동아리 멤버를 내보낼 권한이 없습니다."),
    CLUB_MEMBER_REMOVE_NOT_ALLOWED(HttpStatus.CONFLICT, "CLUB_MEMBER_REMOVE_NOT_ALLOWED", "가입 완료된 일반 멤버 또는 운영진만 내보낼 수 있습니다."),
    CLUB_MEMBER_REMOVE_SELF(HttpStatus.CONFLICT, "CLUB_MEMBER_REMOVE_SELF", "자기 자신을 동아리에서 내보낼 수 없습니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."),
    POST_WRITE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_WRITE_FORBIDDEN", "게시글 작성 권한이 없습니다."),
    POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_UPDATE_FORBIDDEN", "게시글 수정 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_DELETE_FORBIDDEN", "게시글 삭제 권한이 없습니다."),

    // 협업모집
    CLUB_COLLAB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_COLLAB_NOT_FOUND", "협업 모집글을 찾을 수 없습니다."),
    CLUB_COLLAB_WRITE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_COLLAB_WRITE_FORBIDDEN", "협업 모집글 작성 권한이 없습니다."),
    CLUB_COLLAB_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_COLLAB_UPDATE_FORBIDDEN", "협업 모집글 수정 권한이 없습니다."),
    CLUB_COLLAB_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "CLUB_COLLAB_DELETE_FORBIDDEN", "협업 모집글 삭제 권한이 없습니다."),
    CLUB_COLLAB_INVALID_DATE(HttpStatus.BAD_REQUEST, "CLUB_COLLAB_INVALID_DATE", "마감일은 대회 날짜보다 늦을 수 없습니다."),
    CLUB_COLLAB_DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "CLUB_COLLAB_DEADLINE_PASSED", "이미 지난 날짜로는 협업 모집글 작성이 불가 합니다."),

    // 프로젝트 팀원 모집
    PROJECT_RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_RECRUITMENT_NOT_FOUND", "모집 글을 찾을 수 없습니다."),
    PROJECT_RECRUITMENT_UPDATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_RECRUITMENT_ACCESS_DENIED", "수정 권한이 없습니다."),
    PROJECT_RECRUITMENT_DELETE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_RECRUITMENT_ACCESS_DENIED", "삭제 권한이 없습니다."),
    INVALID_PROJECT_RECRUITMENT_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_PROJECT_RECRUITMENT_REQUEST", "요청 형식이 올바르지 않습니다."),
    INVALID_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "INVALID_SEARCH_CONDITION", "유효하지 않은 검색 조건입니다."),

    // 댓글
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT_DELETE_FORBIDDEN", "댓글 삭제 권한이 없습니다."),


    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "유저를 찾을 수 없습니다."),

    // 채팅
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT_ROOM_ACCESS_DENIED", "해당 채팅방에 접근할 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_MESSAGE_NOT_FOUND", "채팅 메시지를 찾을 수 없습니다."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE_CONTENT", "메시지 내용을 입력해주세요."),
    CANNOT_CHAT_WITH_SELF(HttpStatus.BAD_REQUEST, "CANNOT_CHAT_WITH_SELF", "자기 자신과는 채팅할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
