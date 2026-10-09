package com.skhu.skhucapstone.chat.dto.req;

import lombok.Getter;

@Getter
public class ChatRoomCreateReq {

    // 채팅 상대방 userId
    private Long targetUserId;

    // 어느 화면에서 말을 걸었는지. 보내지 않으면 커피챗으로 본다.
    private ChatRoomSource source;

    // source가 가리키는 글의 id. 알림 문구에 글 제목을 넣을 때 쓴다.
    private Long sourceId;
}
