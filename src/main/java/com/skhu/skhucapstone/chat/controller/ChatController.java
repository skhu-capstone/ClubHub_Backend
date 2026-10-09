package com.skhu.skhucapstone.chat.controller;

import com.skhu.skhucapstone.chat.dto.req.ChatMessageReadReq;
import com.skhu.skhucapstone.chat.dto.req.ChatMessageSendReq;
import com.skhu.skhucapstone.chat.dto.req.ChatRoomCreateReq;
import com.skhu.skhucapstone.chat.dto.res.*;
import com.skhu.skhucapstone.chat.service.ChatService;
import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 isNew=true면 생성, false면 기존 채팅방 반환
    @PostMapping("/rooms")
    @Operation(
            summary = "채팅방 생성 or 반환",
            description = "상대방과의 채팅방이 없으면 생성하고, 있으면 기존 채팅방을 반환합니다. "
                    + "커피챗이나 프로젝트 모집글에서 말을 건 경우 source(COFFEE_CHAT, PROJECT_RECRUITMENT)와 "
                    + "sourceId(해당 글 id)를 함께 보내면 상대방에게 맞는 문구의 알림이 갑니다. "
                    + "source를 생략하면 커피챗으로 간주하며, 이때는 채팅방이 새로 생길 때만 알림을 보냅니다."
    )
    public ResponseEntity<ApiResponse<ChatRoomRes>> createOrGetChatRoom(
            @AuthenticationPrincipal Long userId,
            @RequestBody ChatRoomCreateReq req) {
        ChatRoomRes res = chatService.createOrGetChatRoom(userId, req);
        // isNew 여부에 따라 성공 코드 나눔
        SuccessCode code = res.getIsNew()
                ? SuccessCode.CHAT_ROOM_CREATE_SUCCESS
                : SuccessCode.CHAT_ROOM_ALREADY_EXISTS;
        return ResponseEntity.ok(ApiResponse.success(code, res));
    }

    // 내 채팅방 목록 조회
    @GetMapping("/rooms")
    @Operation(summary = "내 채팅방 목록 조회", description = "내가 참여 중인 채팅방 목록을 최신 메시지 순으로 조회합니다.")
    public ResponseEntity<ApiResponse<ChatRoomListRes>> getChatRooms(
            @AuthenticationPrincipal Long userId) {
        ChatRoomListRes res = chatService.getChatRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CHAT_ROOM_LIST_FETCH_SUCCESS, res));
    }

    // 메시지 전송 - WebSocket으로 대체됨 (/app/chat/{chatRoomId}/send)
//    @PostMapping("/rooms/{chatRoomId}/messages")
//    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다.")
//    public ResponseEntity<ApiResponse<ChatMessageRes>> sendMessage(
//            @AuthenticationPrincipal Long userId,
//            @PathVariable Long chatRoomId,
//            @Valid @RequestBody ChatMessageSendReq req) {
//        ChatMessageRes res = chatService.sendMessage(userId, chatRoomId, req);
//        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CHAT_MESSAGE_SEND_SUCCESS, res));
//    }

    // 채팅방 메시지 목록 조회 (페이징)
    @GetMapping("/rooms/{chatRoomId}/messages")
    @Operation(summary = "채팅방 메시지 목록 조회(채팅방 속 메시지들)", description = "채팅방의 메시지 목록을 페이징(20개)으로 조회합니다.")
    public ResponseEntity<ApiResponse<ChatMessageListRes>> getMessages(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ChatMessageListRes res = chatService.getMessages(userId, chatRoomId, page, size);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CHAT_MESSAGE_LIST_FETCH_SUCCESS, res));
    }

    // 메시지 읽음 처리
    @PatchMapping("/rooms/{chatRoomId}/read")
    @Operation(summary = "메시지 읽음 처리", description = "채팅방의 특정 메시지들을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse<ChatMessageReadRes>> readMessages(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageReadReq req) {
        ChatMessageReadRes res = chatService.readMessages(userId, chatRoomId, req);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CHAT_MESSAGE_READ_SUCCESS, res));
    }
}
