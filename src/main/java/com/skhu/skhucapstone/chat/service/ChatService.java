package com.skhu.skhucapstone.chat.service;

import com.skhu.skhucapstone.chat.dto.req.ChatMessageReadReq;
import com.skhu.skhucapstone.chat.dto.req.ChatMessageSendReq;
import com.skhu.skhucapstone.chat.dto.req.ChatRoomCreateReq;
import com.skhu.skhucapstone.chat.dto.res.*;
import com.skhu.skhucapstone.chat.entity.ChatMessage;
import com.skhu.skhucapstone.chat.entity.ChatRoom;
import com.skhu.skhucapstone.chat.repository.ChatMessageRepository;
import com.skhu.skhucapstone.chat.repository.ChatRoomRepository;
import com.skhu.skhucapstone.chat.dto.req.ChatRoomSource;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.notification.application.NotificationService;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.projectrecruitment.entity.ProjectRecruitment;
import com.skhu.skhucapstone.projectrecruitment.repository.ProjectRecruitmentRepository;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ProjectRecruitmentRepository projectRecruitmentRepository;

    // 채팅방 생성 or 반환 (이미 존재하면 기존 채팅방 반환)
    @Transactional
    public ChatRoomRes createOrGetChatRoom(Long userId, ChatRoomCreateReq req) {
        // 자기 자신과의 채팅 불가
        if (userId.equals(req.getTargetUserId())) {
            throw new CustomException(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(req.getTargetUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 존재하는 채팅방이 있으면 isNew=false로 반환
        ChatRoomRes room = chatRoomRepository.findByUsers(user, targetUser)
                .map(existing -> ChatRoomRes.of(existing, false))
                .orElseGet(() -> {
                    // 없으면 새로 생성 후 isNew=true로 반환
                    ChatRoom newRoom = chatRoomRepository.save(
                            ChatRoom.builder()
                                    .user1(user)
                                    .user2(targetUser)
                                    .build()
                    );
                    return ChatRoomRes.of(newRoom, true);
                });

        notifyChatRequest(user, targetUser, req, room);

        return room;
    }

    // 상대방에게 "누가 말을 걸었다"는 알림을 보낸다.
    // source를 담아 보낸 요청은 지원 버튼을 누른 것이므로 매번 알리고,
    // source가 없는 요청은 채팅 목록에서 다시 들어온 경우일 수 있어 새 방일 때만 알린다.
    private void notifyChatRequest(
            User sender,
            User receiver,
            ChatRoomCreateReq req,
            ChatRoomRes room
    ) {
        ChatRoomSource source = req.getSource();

        if (source == null && !room.getIsNew()) {
            return;
        }

        NotificationType type = source == ChatRoomSource.PROJECT_RECRUITMENT
                ? NotificationType.PROJECT_RECRUITMENT_APPLY
                : NotificationType.COFFEE_CHAT_REQUEST;

        notificationService.notify(
                receiver,
                sender,
                type,
                buildChatRequestMessage(sender, source, req.getSourceId()),
                NotificationTargetType.CHAT_ROOM,
                room.getChatRoomId()
        );
    }

    private String buildChatRequestMessage(
            User sender,
            ChatRoomSource source,
            Long sourceId
    ) {
        if (source != ChatRoomSource.PROJECT_RECRUITMENT) {
            return sender.getName() + "님이 커피챗을 신청했습니다.";
        }

        // 글 제목까지 알려주면 어떤 모집 건인지 바로 알 수 있다.
        String title = sourceId == null ? null
                : projectRecruitmentRepository.findById(sourceId)
                        .map(ProjectRecruitment::getTitle)
                        .orElse(null);

        return title == null
                ? sender.getName() + "님이 프로젝트 팀원 모집에 지원했습니다."
                : sender.getName() + "님이 프로젝트 '" + title + "'에 지원했습니다.";
    }
    // 협업 모집글 문의하기 등 내부 서비스에서 targetUserId를 바로 알고 있을 때 사용하는 채팅방 생성/반환 메서드
    // 기존 createOrGetChatRoom(Long userId, ChatRoomCreateReq req)는 컨트롤러 요청용으로 유지하고,
    // 이 메서드는 DTO 생성 없이 userId와 targetUserId만으로 기존 채팅방을 찾거나 새 채팅방을 생성한다.
    @Transactional
    public ChatRoomRes createOrGetChatRoom(
            Long userId,
            Long targetUserId
    ) {
        if (userId.equals(targetUserId)) {
            throw new CustomException(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return chatRoomRepository.findByUsers(user, targetUser)
                .map(existing -> ChatRoomRes.of(existing, false))
                .orElseGet(() -> {
                    ChatRoom newRoom = chatRoomRepository.save(
                            ChatRoom.builder()
                                    .user1(user)
                                    .user2(targetUser)
                                    .build()
                    );

                    return ChatRoomRes.of(newRoom, true);
                });
    }

    // 내 채팅방 목록 조회 (최신 메시지 순)
    @Transactional(readOnly = true)
    public ChatRoomListRes getChatRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<ChatRoomListItemRes> content = chatRoomRepository.findAllByUser(user).stream()
                .map(room -> {
                    // 상대방 정보 추출 (내가 user1이면 상대는 user2, 반대도 마찬가지)
                    User target = room.getUser1().getUserId().equals(userId)
                            ? room.getUser2()
                            : room.getUser1();

                    // 마지막 메시지 조회 (없으면 null)
                    ChatMessage lastMsg = chatMessageRepository.findFirstByChatRoomOrderByCreatedAtDesc(room).orElse(null);

                    // 안 읽은 메시지 수 조회
                    long unreadCount = chatMessageRepository.countUnreadMessages(room, user);

                    return ChatRoomListItemRes.builder()
                            .chatRoomId(room.getChatRoomId())
                            .targetUserId(target.getUserId())
                            .targetUserName(target.getName())
                            .targetProfileImage(target.getProfileImage())
                            .lastMessage(lastMsg != null ? lastMsg.getContent() : null)
                            .lastMessageAt(lastMsg != null ? lastMsg.getCreatedAt() : null)
                            .unreadCount(unreadCount)
                            .build();
                })
                .toList();

        return ChatRoomListRes.builder()
                .content(content)
                .build();
    }

    // 메시지 전송
    @Transactional
    public ChatMessageRes sendMessage(Long userId, Long chatRoomId, ChatMessageSendReq req) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자인지 확인
        validateChatRoomAccess(chatRoom, userId);

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .content(req.getContent())
                        .build()
        );

        // 채팅방 최신 메시지 시각 갱신
        chatRoom.updateUpdatedAt();

        return ChatMessageRes.from(message);
    }

    // 채팅방 메시지 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public ChatMessageListRes getMessages(Long userId, Long chatRoomId, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자인지 확인
        validateChatRoomAccess(chatRoom, userId);

        return ChatMessageListRes.from(
                chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom, PageRequest.of(page, size))
                        .map(ChatMessageRes::from)
        );
    }

    // 메시지 읽음 처리
    @Transactional
    public ChatMessageReadRes readMessages(Long userId, Long chatRoomId, ChatMessageReadReq req) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 참여자인지 확인
        validateChatRoomAccess(chatRoom, userId);

        List<ChatMessage> messages = chatMessageRepository.findAllByChatMessageIdIn(req.getMessageIds());

        // 요청한 messageIds 중 존재하지 않는 것이 있으면 예외
        if (messages.size() != req.getMessageIds().size()) {
            throw new CustomException(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
        }

        // 내가 보낸 메시지는 제외하고, 상대방 메시지만 읽음 처리
        int readCount = 0;
        for (ChatMessage message : messages) {
            if (!message.getSender().getUserId().equals(userId) && !message.getIsRead()) {
                message.markAsRead();
                readCount++;
            }
        }

        return ChatMessageReadRes.builder()
                .chatRoomId(chatRoomId)
                .readCount(readCount)
                .build();
    }

    // 채팅방 접근 권한 확인 (user1 또는 user2여야 함)
    private void validateChatRoomAccess(ChatRoom chatRoom, Long userId) {
        boolean isParticipant = chatRoom.getUser1().getUserId().equals(userId)
                || chatRoom.getUser2().getUserId().equals(userId);
        if (!isParticipant) {
            throw new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }
}
