package com.skhu.skhucapstone.likes.application;

import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.likes.api.dto.response.LikeResponse;
import com.skhu.skhucapstone.likes.domain.Likes;
import com.skhu.skhucapstone.likes.domain.repository.LikesRepository;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikesService {

    private final LikesRepository likesRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public LikeResponse toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return likesRepository.findByPostAndUser(post, user)
                .map(like -> {
                    likesRepository.delete(like);

                    long likeCount = likesRepository.countByPost(post);

                    return LikeResponse.builder()
                            .liked(false)
                            .likeCount(likeCount)
                            .build();
                })
                .orElseGet(() -> {
                    Likes like = Likes.builder()
                            .post(post)
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .build();

                    likesRepository.save(like);

                    // 좋아요를 취소했다 다시 누르면 알림도 다시 간다.
                    // 지금 사용자 규모에서는 문제되지 않아 별도로 막지 않는다.
                    notificationService.notify(
                            post.getUser(),
                            user,
                            NotificationType.POST_LIKE,
                            user.getName() + "님이 회원님의 게시글을 좋아합니다.",
                            NotificationTargetType.POST,
                            post.getPostId()
                    );

                    long likeCount = likesRepository.countByPost(post);

                    return LikeResponse.builder()
                            .liked(true)
                            .likeCount(likeCount)
                            .build();
                });
    }
}