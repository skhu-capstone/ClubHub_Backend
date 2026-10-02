package com.skhu.skhucapstone.comment.api;

import com.skhu.skhucapstone.comment.api.dto.request.CommentCreateRequest;
import com.skhu.skhucapstone.comment.api.dto.response.CommentResponse;
import com.skhu.skhucapstone.comment.application.CommentService;
import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Comment", description = "댓글 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 작성",
            description = "로그인한 사용자가 특정 게시글에 댓글을 작성합니다."
    )
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CommentCreateRequest request) {

        CommentResponse response = commentService.createComment(postId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.COMMENT_CREATE_SUCCESS, response)
        );
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "댓글 작성자만 자신의 댓글을 삭제할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId) {

        commentService.deleteComment(commentId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.COMMENT_DELETE_SUCCESS, null)
        );
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(
            summary = "댓글 목록 조회",
            description = "특정 게시글에 작성된 댓글 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long userId) {

        List<CommentResponse> response = commentService.getComments(postId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.COMMENT_LIST_GET_SUCCESS, response)
        );
    }
}