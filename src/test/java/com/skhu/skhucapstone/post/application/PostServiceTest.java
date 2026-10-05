package com.skhu.skhucapstone.post.application;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.club.domain.repository.ClubRepository;
import com.skhu.skhucapstone.clubmember.domain.repository.ClubMemberRepository;
import com.skhu.skhucapstone.coffeechat.repository.CoffeeChatProfileRepository;
import com.skhu.skhucapstone.comment.domain.repository.CommentRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.common.file.ImageUploadService;
import com.skhu.skhucapstone.likes.domain.repository.LikesRepository;
import com.skhu.skhucapstone.post.api.dto.request.PostUpdateRequest;
import com.skhu.skhucapstone.post.domain.Post;
import com.skhu.skhucapstone.post.domain.PostImage;
import com.skhu.skhucapstone.post.domain.PostType;
import com.skhu.skhucapstone.post.domain.repository.PostImageRepository;
import com.skhu.skhucapstone.post.domain.repository.PostRepository;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 게시글 이미지가 여러 장 업로드될 때 기존 이미지가 유지되는지, 그리고
// 더 이상 쓰이지 않는 이미지만 저장소에서 지워지는지 검증한다.
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubMemberRepository clubMemberRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private CoffeeChatProfileRepository coffeeChatProfileRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private static final Long POST_ID = 10L;
    private static final Long WRITER_ID = 1L;
    private static final Long OTHER_ID = 2L;

    private static final String FIRST_IMAGE = "https://objectstorage.example.com/o/post%2Ffirst.jpg";
    private static final String SECOND_IMAGE = "https://objectstorage.example.com/o/post%2Fsecond.jpg";
    private static final String THIRD_IMAGE = "https://objectstorage.example.com/o/post%2Fthird.jpg";

    private Post post() {
        return Post.builder()
                .postId(POST_ID)
                .title("1주 1회차 활동")
                .content("Java 기초 및 심화를 진행했습니다.")
                .postType(PostType.NOTICE)
                .user(User.builder().userId(WRITER_ID).name("정다운").build())
                .club(Club.builder().clubName("멋쟁이 사자처럼").build())
                .build();
    }

    private PostImage image(String imageUrl, int order) {
        return PostImage.builder()
                .post(post())
                .imageUrl(imageUrl)
                .imageOrder(order)
                .build();
    }

    private MultipartFile file() {
        return new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "dummy".getBytes());
    }

    @Test
    @DisplayName("이미지를 추가로 업로드해도 기존 이미지를 저장소에서 지우지 않는다")
    void uploadKeepsExistingImages() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));
        when(postImageRepository.findByPostOrderByImageOrderAsc(any()))
                .thenReturn(List.of(image(FIRST_IMAGE, 0)));
        when(imageUploadService.upload(any(), eq("post"))).thenReturn(SECOND_IMAGE);

        String uploaded = postService.uploadPostImage(POST_ID, WRITER_ID, file());

        assertThat(uploaded).isEqualTo(SECOND_IMAGE);
        verify(imageUploadService, never()).delete(anyString());
        verify(postImageRepository, never()).deleteByPost(any());
    }

    @Test
    @DisplayName("추가로 업로드한 이미지는 기존 이미지 뒤 순서로 저장된다")
    void uploadAppendsAfterExistingImages() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));
        when(postImageRepository.findByPostOrderByImageOrderAsc(any()))
                .thenReturn(List.of(image(FIRST_IMAGE, 0), image(SECOND_IMAGE, 1)));
        when(imageUploadService.upload(any(), eq("post"))).thenReturn(THIRD_IMAGE);

        postService.uploadPostImage(POST_ID, WRITER_ID, file());

        ArgumentCaptor<PostImage> saved = ArgumentCaptor.forClass(PostImage.class);
        verify(postImageRepository).save(saved.capture());

        assertThat(saved.getValue().getImageUrl()).isEqualTo(THIRD_IMAGE);
        assertThat(saved.getValue().getImageOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("작성자가 아니면 이미지를 업로드할 수 없고 저장소에도 올리지 않는다")
    void uploadRejectsNonWriter() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));

        assertThatThrownBy(() ->
                postService.uploadPostImage(POST_ID, OTHER_ID, file()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_UPDATE_FORBIDDEN);

        verify(imageUploadService, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("비로그인 상태로는 이미지를 업로드할 수 없다")
    void uploadRejectsAnonymous() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));

        assertThatThrownBy(() ->
                postService.uploadPostImage(POST_ID, null, file()))
                .isInstanceOf(CustomException.class);

        verify(imageUploadService, never()).upload(any(), anyString());
    }

    @Test
    @DisplayName("게시글을 수정하면 목록에서 빠진 이미지만 저장소에서 지운다")
    void updateDeletesOnlyRemovedImages() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));
        when(postImageRepository.findByPostOrderByImageOrderAsc(any()))
                .thenReturn(List.of(
                        image(FIRST_IMAGE, 0),
                        image(SECOND_IMAGE, 1),
                        image(THIRD_IMAGE, 2)));

        PostUpdateRequest request = new PostUpdateRequest(
                "수정한 제목",
                "수정한 내용",
                List.of(FIRST_IMAGE, THIRD_IMAGE),
                PostType.NOTICE);

        postService.updatePost(POST_ID, WRITER_ID, request);

        verify(imageUploadService).delete(SECOND_IMAGE);
        verify(imageUploadService, never()).delete(FIRST_IMAGE);
        verify(imageUploadService, never()).delete(THIRD_IMAGE);
    }

    @Test
    @DisplayName("게시글을 삭제하면 이미지도 저장소에서 함께 지운다")
    void deleteRemovesImagesFromStorage() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));
        when(postImageRepository.findByPostOrderByImageOrderAsc(any()))
                .thenReturn(List.of(image(FIRST_IMAGE, 0), image(SECOND_IMAGE, 1)));

        postService.deletePost(POST_ID, WRITER_ID);

        verify(imageUploadService).delete(FIRST_IMAGE);
        verify(imageUploadService).delete(SECOND_IMAGE);
        verify(postRepository).delete(any());
    }

    @Test
    @DisplayName("작성자가 아니면 게시글을 삭제할 수 없고 이미지도 그대로 둔다")
    void deleteRejectsNonWriter() {
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post()));

        assertThatThrownBy(() ->
                postService.deletePost(POST_ID, OTHER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_DELETE_FORBIDDEN);

        verify(imageUploadService, never()).delete(anyString());
        verify(postRepository, never()).delete(any());
    }
}
