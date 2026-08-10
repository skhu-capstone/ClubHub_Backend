package com.skhu.skhucapstone.main.api.dto.response;

import com.skhu.skhucapstone.comment.api.dto.response.CommentResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ClubFeedRes {

    private Long postId;

    private String writerName;

    private String writerCoffeeChatProfileImageUrl;

    private String clubName;

    private LocalDateTime createdAt;

    private List<String> imageUrls;

    private String content;

    private long likeCount;

    private boolean liked;

    private List<CommentResponse> comments;
}