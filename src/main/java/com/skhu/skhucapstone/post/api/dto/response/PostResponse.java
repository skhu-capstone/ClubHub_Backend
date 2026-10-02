package com.skhu.skhucapstone.post.api.dto.response;

import com.skhu.skhucapstone.comment.api.dto.response.CommentResponse;
import com.skhu.skhucapstone.post.domain.PostType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponse {

    private String clubName;

    private Long postId;

    private String title;

    private String content;

    private List<String> imageUrls;

    private PostType postType;

    private String writerName;

    private String writerCoffeeChatProfileImageUrl;

    private long likeCount;

    private boolean liked;

    private List<CommentResponse> comments;

    private LocalDateTime createdAt;

    // 요청한 사용자가 이 게시글을 수정할 수 있는지 (작성자 본인만)
    private boolean canUpdate;

    // 요청한 사용자가 이 게시글을 삭제할 수 있는지 (작성자 본인만)
    private boolean canDelete;
}