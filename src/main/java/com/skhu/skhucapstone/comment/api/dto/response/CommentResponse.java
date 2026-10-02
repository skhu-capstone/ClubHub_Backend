package com.skhu.skhucapstone.comment.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long commentId;

    private String content;

    private String writerName;

    private LocalDateTime createdAt;

    // 요청한 사용자가 이 댓글을 삭제할 수 있는지 (작성자 본인만)
    private boolean canDelete;
}