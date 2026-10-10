package com.skhu.skhucapstone.post.application;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.club.domain.repository.ClubRepository;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import com.skhu.skhucapstone.clubmember.domain.ClubMember;
import com.skhu.skhucapstone.clubmember.domain.ClubRole;
import com.skhu.skhucapstone.clubmember.domain.repository.ClubMemberRepository;
import com.skhu.skhucapstone.coffeechat.repository.CoffeeChatProfileRepository;
import com.skhu.skhucapstone.comment.api.dto.response.CommentResponse;
import com.skhu.skhucapstone.comment.domain.Comment;
import com.skhu.skhucapstone.comment.domain.repository.CommentRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.common.file.ImageUploadService;
import com.skhu.skhucapstone.likes.domain.repository.LikesRepository;
import com.skhu.skhucapstone.notification.application.NotificationService;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.post.api.dto.request.PostCreateRequest;
import com.skhu.skhucapstone.post.api.dto.request.PostUpdateRequest;
import com.skhu.skhucapstone.post.api.dto.response.PostPageResponse;
import com.skhu.skhucapstone.post.api.dto.response.PostResponse;
import com.skhu.skhucapstone.post.domain.Post;
import com.skhu.skhucapstone.post.domain.PostImage;
import com.skhu.skhucapstone.post.domain.PostType;
import com.skhu.skhucapstone.post.domain.repository.PostImageRepository;
import com.skhu.skhucapstone.post.domain.repository.PostRepository;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ImageUploadService imageUploadService;
    private final LikesRepository likesRepository;
    private final CoffeeChatProfileRepository coffeeChatProfileRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Transactional
    public PostResponse createPost(
            Long clubId,
            Long userId,
            PostCreateRequest request
    ) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.CLUB_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND));

        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_WRITE_FORBIDDEN));

        if (clubMember.getRole() != ClubRole.STAFF
                && clubMember.getRole() != ClubRole.PRESIDENT) {
            throw new CustomException(ErrorCode.POST_WRITE_FORBIDDEN);
        }

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .postType(request.postType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .club(club)
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        savePostImages(savedPost, request.imageUrls());

        notifyClubMembersOfNotice(club, user, savedPost);

        return toPostResponse(savedPost, userId);
    }

    // 공지만 알린다. 일반 게시글까지 알리면 동아리원 알림함이 금세 가득 찬다.
    private void notifyClubMembersOfNotice(Club club, User writer, Post post) {
        if (post.getPostType() != PostType.NOTICE) {
            return;
        }

        List<User> members = clubMemberRepository
                .findByClubAndClubJoinStatus(club, ClubJoinStatus.JOINED)
                .stream()
                .map(ClubMember::getUser)
                .toList();

        notificationService.notifyAll(
                members,
                writer,
                NotificationType.CLUB_NOTICE_CREATED,
                club.getClubName() + " 동아리에 새 공지가 올라왔습니다.",
                NotificationTargetType.POST,
                post.getPostId()
        );
    }

    public PostPageResponse getPosts(
            Long userId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> posts =
                postRepository.findAllByOrderByCreatedAtDesc(pageable);

        return PostPageResponse.builder()
                .content(
                        posts.getContent()
                                .stream()
                                .map(post -> toPostResponse(post, userId))
                                .toList()
                )
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .last(posts.isLast())
                .build();
    }

    public PostPageResponse getPosts(
            int page,
            int size
    ) {
        return getPosts(null, page, size);
    }

    public PostPageResponse getClubPosts(
            Long clubId,
            Long userId,
            int page,
            int size
    ) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.CLUB_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);

        Page<Post> posts =
                postRepository.findByClubOrderByCreatedAtDesc(
                        club,
                        pageable
                );

        return PostPageResponse.builder()
                .content(
                        posts.getContent()
                                .stream()
                                .map(post -> toPostResponse(post, userId))
                                .toList()
                )
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .last(posts.isLast())
                .build();
    }

    public PostResponse getPost(
            Long postId,
            Long userId
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));

        return toPostResponse(post, userId);
    }

    @Transactional
    public PostResponse updatePost(
            Long postId,
            Long userId,
            PostUpdateRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));

        validateWriter(post, userId, ErrorCode.POST_UPDATE_FORBIDDEN);

        post.updatePost(
                request.title(),
                request.content(),
                request.postType()
        );

        // 수정 후 목록에서 빠진 이미지는 저장소에도 남을 이유가 없으므로 같이 지운다.
        List<String> removedImageUrls =
                findRemovedImageUrls(post, request.imageUrls());

        postImageRepository.deleteByPost(post);
        savePostImages(post, request.imageUrls());

        removedImageUrls.forEach(imageUploadService::delete);

        return toPostResponse(post, userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));

        validateWriter(post, userId, ErrorCode.POST_DELETE_FORBIDDEN);

        List<String> imageUrls = postImageRepository.findByPostOrderByImageOrderAsc(post)
                .stream()
                .map(PostImage::getImageUrl)
                .toList();

        commentRepository.deleteByPost(post);
        likesRepository.deleteByPost(post);
        postImageRepository.deleteByPost(post);

        postRepository.delete(post);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String imageUrl : imageUrls) {
                    try {
                        imageUploadService.delete(imageUrl);
                    } catch (Exception e) {
                        log.error("게시글 삭제 후 이미지 정리 실패: postId={}, imageUrl={}",
                                postId, imageUrl, e);
                    }
                }
            }
        });
    }

    @Transactional
    public String uploadPostImage(
            Long postId,
            Long userId,
            MultipartFile file
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));

        validateWriter(post, userId, ErrorCode.POST_UPDATE_FORBIDDEN);

        // 여러 장을 연속으로 올리므로 기존 이미지를 지우지 않고 뒤에 덧붙인다.
        int nextOrder =
                postImageRepository.findByPostOrderByImageOrderAsc(post).size();

        String imageUrl =
                imageUploadService.upload(file, "post");

        PostImage postImage = PostImage.builder()
                .post(post)
                .imageUrl(imageUrl)
                .imageOrder(nextOrder)
                .build();

        postImageRepository.save(postImage);

        return imageUrl;
    }

    private void validateWriter(
            Post post,
            Long userId,
            ErrorCode errorCode
    ) {
        if (userId == null || !post.getUser().getUserId().equals(userId)) {
            throw new CustomException(errorCode);
        }
    }

    // 수정 요청에 포함되지 않아 더 이상 쓰이지 않게 되는 이미지 URL을 찾는다.
    private List<String> findRemovedImageUrls(
            Post post,
            List<String> newImageUrls
    ) {
        List<String> keptImageUrls =
                newImageUrls == null ? List.of() : newImageUrls;

        return postImageRepository.findByPostOrderByImageOrderAsc(post)
                .stream()
                .map(PostImage::getImageUrl)
                .filter(imageUrl -> !keptImageUrls.contains(imageUrl))
                .toList();
    }

    private void savePostImages(
            Post post,
            List<String> imageUrls
    ) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<PostImage> postImages = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrls.get(i))
                    .imageOrder(i)
                    .build();

            postImages.add(postImage);
        }

        postImageRepository.saveAll(postImages);
    }

    private PostResponse toPostResponse(
            Post post,
            Long userId
    ) {
        List<String> imageUrls =
                postImageRepository.findByPostOrderByImageOrderAsc(post)
                        .stream()
                        .map(PostImage::getImageUrl)
                        .toList();

        long likeCount =
                likesRepository.countByPost(post);

        boolean liked =
                userId != null
                        && likesRepository.existsByPostAndUser_UserId(
                        post,
                        userId
                );

        String writerCoffeeChatProfileImageUrl =
                coffeeChatProfileRepository
                        .findByUserUserId(post.getUser().getUserId())
                        .map(profile ->
                                profile.getProfileImageUrl() != null
                                        && !profile.getProfileImageUrl().isBlank()
                                        ? profile.getProfileImageUrl()
                                        : post.getUser().getProfileImage()
                        )
                        .orElse(post.getUser().getProfileImage());

        List<CommentResponse> comments =
                commentRepository.findByPostOrderByCreatedAtAsc(post)
                        .stream()
                        .map(comment -> toCommentResponse(comment, userId))
                        .toList();

        boolean isWriter =
                userId != null
                        && post.getUser().getUserId().equals(userId);

        return PostResponse.builder()
                .clubName(post.getClub().getClubName())
                .postId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(imageUrls)
                .postType(post.getPostType())
                .writerName(post.getUser().getName())
                .writerCoffeeChatProfileImageUrl(writerCoffeeChatProfileImageUrl)
                .likeCount(likeCount)
                .liked(liked)
                .comments(comments)
                .createdAt(post.getCreatedAt())
                // 수정·삭제 모두 작성자 본인만 가능하다.
                .canUpdate(isWriter)
                .canDelete(isWriter)
                .build();
    }

    private CommentResponse toCommentResponse(Comment comment, Long userId) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .writerName(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .canDelete(userId != null
                        && comment.getUser().getUserId().equals(userId))
                .build();
    }

    public PostPageResponse getRecommendedPosts(
            Long userId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> posts =
                postRepository.findAllOrderByLikeCountDesc(pageable);

        return PostPageResponse.builder()
                .content(
                        posts.getContent()
                                .stream()
                                .map(post -> toPostResponse(post, userId))
                                .toList()
                )
                .page(posts.getNumber())
                .size(posts.getSize())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .last(posts.isLast())
                .build();
    }
}