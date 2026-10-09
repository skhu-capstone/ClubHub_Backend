package com.skhu.skhucapstone.comment.application;

import com.skhu.skhucapstone.comment.api.dto.request.CommentCreateRequest;
import com.skhu.skhucapstone.comment.api.dto.response.CommentResponse;
import com.skhu.skhucapstone.comment.domain.Comment;
import com.skhu.skhucapstone.comment.domain.repository.CommentRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.notification.application.NotificationService;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.post.domain.Post;
import com.skhu.skhucapstone.post.domain.repository.PostRepository;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(
            Long postId,
            Long userId,
            CommentCreateRequest request
    ) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.content())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .post(post)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        notificationService.notify(
                post.getUser(),
                user,
                NotificationType.POST_COMMENT,
                user.getName() + "님이 회원님의 게시글에 댓글을 남겼습니다.",
                NotificationTargetType.POST,
                post.getPostId()
        );

        return CommentResponse.builder()
                .commentId(savedComment.getCommentId())
                .content(savedComment.getContent())
                .writerName(savedComment.getUser().getName())
                .createdAt(savedComment.getCreatedAt())
                // 방금 작성한 본인의 댓글이므로 항상 삭제할 수 있다.
                .canDelete(true)
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        commentRepository.delete(comment);
    }

    public List<CommentResponse> getComments(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(()  -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return commentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(comment -> CommentResponse.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .writerName(comment.getUser().getName())
                        .createdAt(comment.getCreatedAt())
                        // 비로그인 조회도 가능하므로 userId가 없으면 삭제 불가로 둔다.
                        .canDelete(userId != null
                                && comment.getUser().getUserId().equals(userId))
                        .build())
                .toList();
    }
}