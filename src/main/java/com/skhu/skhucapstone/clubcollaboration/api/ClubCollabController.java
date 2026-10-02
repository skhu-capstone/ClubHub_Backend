package com.skhu.skhucapstone.clubcollaboration.api;

import com.skhu.skhucapstone.clubcollaboration.api.dto.request.ClubCollabCreateRequest;
import com.skhu.skhucapstone.clubcollaboration.api.dto.request.ClubCollabUpdateRequest;
import com.skhu.skhucapstone.clubcollaboration.api.dto.response.ClubCollabPageResponse;
import com.skhu.skhucapstone.clubcollaboration.api.dto.response.ClubCollabResponse;
import com.skhu.skhucapstone.clubcollaboration.application.ClubCollabService;
import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.skhu.skhucapstone.clubcollaboration.api.dto.response.ClubCollabApplyResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club-collaborations")
@Tag(name = "ClubCollab", description = "동아리 협업 모집 API")
public class ClubCollabController {

    private final ClubCollabService clubCollabService;

    @PostMapping
    @Operation(summary = "협업 모집글 작성", description = "해당 동아리의 STAFF 또는 PRESIDENT만 협업 모집글을 작성할 수 있습니다.")
    public ResponseEntity<ApiResponse<ClubCollabResponse>> createCollab(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ClubCollabCreateRequest request
    ) {
        ClubCollabResponse response = clubCollabService.createCollab(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CLUB_COLLAB_CREATE_SUCCESS, response)
        );
    }

    @GetMapping
    @Operation(summary = "협업 모집글 목록 조회", description = "협업 모집글을 최신순으로 페이지 단위 조회하고, 키워드 검색을 지원합니다.")
    public ResponseEntity<ApiResponse<ClubCollabPageResponse>> getCollabs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId
    ) {
        ClubCollabPageResponse response = clubCollabService.getCollabs(keyword, page, size, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CLUB_COLLAB_LIST_GET_SUCCESS, response)
        );
    }

    @GetMapping("/{collabId}")
    @Operation(summary = "협업 모집글 상세 조회", description = "협업 모집글 ID로 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ClubCollabResponse>> getCollab(
            @PathVariable Long collabId,
            @AuthenticationPrincipal Long userId
    ) {
        ClubCollabResponse response = clubCollabService.getCollab(collabId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CLUB_COLLAB_DETAIL_GET_SUCCESS, response)
        );
    }

    @PatchMapping("/{collabId}")
    @Operation(summary = "협업 모집글 수정", description = "협업 모집글 작성자 본인만 수정할 수 있습니다.")
    public ResponseEntity<ApiResponse<ClubCollabResponse>> updateCollab(
            @PathVariable Long collabId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ClubCollabUpdateRequest request
    ) {
        ClubCollabResponse response = clubCollabService.updateCollab(collabId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CLUB_COLLAB_UPDATE_SUCCESS, response)
        );
    }

    @DeleteMapping("/{collabId}")
    @Operation(summary = "협업 모집글 삭제", description = "협업 모집글 작성자 본인만 삭제할 수 있습니다.")
    public ResponseEntity<ApiResponse<Void>> deleteCollab(
            @PathVariable Long collabId,
            @AuthenticationPrincipal Long userId
    ) {
        clubCollabService.deleteCollab(collabId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CLUB_COLLAB_DELETE_SUCCESS, null)
        );
    }

    @PostMapping(value = "/{collabId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "협업 모집글 이미지 업로드", description = "협업 모집글 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<String>> uploadCollabImage(
            @PathVariable Long collabId,
            @RequestPart MultipartFile file) {
        String imageUrl = clubCollabService.uploadCollabImage(collabId, file);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_COLLAB_IMAGE_UPLOAD_SUCCESS, imageUrl));
    }

    @PostMapping("/{collabId}/apply")
    @Operation(
            summary = "협업 모집 문의하기",
            description = "협업 모집글 작성자와 채팅방을 생성하거나 기존 채팅방을 반환합니다."
    )
    public ResponseEntity<ApiResponse<ClubCollabApplyResponse>> applyCollab(
            @PathVariable Long collabId,
            @AuthenticationPrincipal Long userId
    ) {
        ClubCollabApplyResponse response =
                clubCollabService.applyCollab(collabId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.CLUB_COLLAB_APPLY_SUCCESS,
                        response
                )
        );
    }
}