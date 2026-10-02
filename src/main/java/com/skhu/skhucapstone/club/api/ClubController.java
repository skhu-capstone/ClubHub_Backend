package com.skhu.skhucapstone.club.api;

import com.skhu.skhucapstone.club.api.dto.Request.ClubCreateRequest;
import com.skhu.skhucapstone.club.api.dto.Request.ClubUpdateRequest;
import com.skhu.skhucapstone.club.api.dto.Response.ClubPageResponse;
import com.skhu.skhucapstone.club.api.dto.Response.ClubResponse;
import com.skhu.skhucapstone.club.application.ClubService;
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

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "Club", description = "동아리 관리 API")
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    @Operation(
            summary = "동아리 생성",
            description = "새로운 동아리를 생성합니다. 생성된 동아리는 별도 승인 없이 즉시 서비스에 노출되며, 생성자는 대표로 자동 등록됩니다."
    )
    public ResponseEntity<ApiResponse<ClubResponse>> createClub(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ClubCreateRequest request) {

        ClubResponse response = clubService.createClub(userId, request);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_CREATE_SUCCESS, response));
    }

    @GetMapping
    @Operation(
            summary = "동아리 목록 및 검색",
            description = "동아리명과 카테고리를 기준으로 동아리를 검색하고 페이지 단위로 조회합니다. "
                    + "로그인한 경우 각 동아리에 대한 내 가입 상태(myJoinStatus)와 신청 가능 여부(canApply)를 함께 반환합니다."
    )
    public ResponseEntity<ApiResponse<ClubPageResponse>> getClubs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId) {

        ClubPageResponse response = clubService.getClubs(keyword, category, page, size, userId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_LIST_GET_SUCCESS, response));
    }

    @GetMapping("/{clubId}")
    @Operation(
            summary = "동아리 상세 조회",
            description = "동아리 ID를 이용하여 특정 동아리의 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<ClubResponse>> getClub(
            @PathVariable Long clubId) {

        ClubResponse response = clubService.getClub(clubId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_DETAIL_GET_SUCCESS, response));
    }

    @PatchMapping("/{clubId}")
    @Operation(
            summary = "동아리 정보 수정",
            description = "동아리 대표 또는 운영진이 동아리 정보를 수정합니다."
    )
    public ResponseEntity<ApiResponse<ClubResponse>> updateClub(
            @PathVariable Long clubId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ClubUpdateRequest request) {

        ClubResponse response = clubService.updateClub(clubId, userId, request);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_UPDATE_SUCCESS, response));
    }

    @PostMapping(value = "/{clubId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "동아리 이미지 업로드", description = "동아리 대표 또는 운영진이 동아리 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<String>> uploadClubImage(
            @PathVariable Long clubId,
            @AuthenticationPrincipal Long userId,
            @RequestPart MultipartFile file) {

        String imageUrl = clubService.uploadClubImage(clubId, userId, file);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_IMAGE_UPLOAD_SUCCESS, imageUrl));
    }
}