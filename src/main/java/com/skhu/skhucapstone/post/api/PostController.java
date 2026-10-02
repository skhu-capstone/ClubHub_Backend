package com.skhu.skhucapstone.post.api;

import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import com.skhu.skhucapstone.post.api.dto.request.PostCreateRequest;
import com.skhu.skhucapstone.post.api.dto.request.PostUpdateRequest;
import com.skhu.skhucapstone.post.api.dto.response.PostPageResponse;
import com.skhu.skhucapstone.post.api.dto.response.PostResponse;
import com.skhu.skhucapstone.post.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Post", description = "게시글 API")
public class PostController {

    private final PostService postService;

    @PostMapping("/clubs/{clubId}/posts")
    @Operation(
            summary = "게시글 생성",
            description = "STAFF 또는 PRESIDENT 권한을 가진 동아리 멤버가 게시글을 작성합니다."
    )
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @PathVariable Long clubId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid PostCreateRequest request
    ) {
        PostResponse response = postService.createPost(clubId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_CREATE_SUCCESS, response));
    }

    @GetMapping("/posts")
    @Operation(
            summary = "전체 게시글 조회",
            description = "전체 동아리 게시글을 최신순 또는 좋아요순으로 조회합니다."
    )
    public ResponseEntity<ApiResponse<PostPageResponse>> getPosts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        PostPageResponse response;

        if ("likes".equals(sort)) {
            response = postService.getRecommendedPosts(userId, page, size);
        } else {
            response = postService.getPosts(userId, page, size);
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_LIST_GET_SUCCESS, response));
    }

    @GetMapping("/clubs/{clubId}/posts")
    @Operation(
            summary = "동아리별 게시글 조회",
            description = "특정 동아리의 게시글을 페이지 단위로 조회합니다."
    )
    public ResponseEntity<ApiResponse<PostPageResponse>> getClubPosts(
            @PathVariable Long clubId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        PostPageResponse response = postService.getClubPosts(clubId, userId, page, size);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_CLUB_LIST_GET_SUCCESS, response));
    }

    @GetMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 게시글 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long userId
    ) {
        PostResponse response = postService.getPost(postId, userId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_DETAIL_GET_SUCCESS, response));
    }

    @PatchMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 수정",
            description = "게시글 작성자만 게시글을 수정할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid PostUpdateRequest request
    ) {
        PostResponse response = postService.updatePost(postId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_UPDATE_SUCCESS, response));
    }

    @PostMapping(value = "/posts/{postId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "게시글 이미지 업로드",
            description = "게시글 이미지를 업로드합니다."
    )
    public ResponseEntity<ApiResponse<String>> uploadPostImage(
            @PathVariable Long postId,
            @RequestPart MultipartFile file
    ) {
        String imageUrl = postService.uploadPostImage(postId, file);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_IMAGE_UPLOAD_SUCCESS, imageUrl));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(
            summary = "게시글 삭제",
            description = "게시글 작성자 본인만 삭제할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long userId
    ) {
        postService.deletePost(postId, userId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.POST_DELETE_SUCCESS, null));
    }
}