package com.skhu.skhucapstone.projectrecruitment.controller;

import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import com.skhu.skhucapstone.projectrecruitment.dto.req.ProjectRecruitmentCreateReq;
import com.skhu.skhucapstone.projectrecruitment.dto.req.ProjectRecruitmentUpdateReq;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentCreateRes;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentDeleteRes;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentDetailRes;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentPageRes;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentUpdateRes;
import com.skhu.skhucapstone.projectrecruitment.service.ProjectRecruitmentService;
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
@RequestMapping("/api/project-recruitments")
@Tag(name = "ProjectRecruitment", description = "프로젝트 팀원 모집 API")
public class ProjectRecruitmentController {

    private final ProjectRecruitmentService projectRecruitmentService;

    @PostMapping
    @Operation(summary = "프로젝트 팀원 모집 글 작성", description = "프로젝트 팀원 모집 글을 작성합니다.")
    public ResponseEntity<ApiResponse<ProjectRecruitmentCreateRes>> createRecruitment(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ProjectRecruitmentCreateReq request
    ) {
        ProjectRecruitmentCreateRes response =
                projectRecruitmentService.createRecruitment(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_RECRUITMENT_CREATE_SUCCESS, response)
        );
    }

    @GetMapping
    @Operation(summary = "프로젝트 팀원 모집 목록 조회", description = "모집 글을 최신순으로 페이지 단위 조회하고, 키워드 검색을 지원합니다.")
    public ResponseEntity<ApiResponse<ProjectRecruitmentPageRes>> getRecruitments(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProjectRecruitmentPageRes response =
                projectRecruitmentService.getRecruitments(keyword, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_RECRUITMENT_LIST_FETCH_SUCCESS, response)
        );
    }

    @GetMapping("/{projectRecruitmentId}")
    @Operation(summary = "프로젝트 팀원 모집 상세 조회", description = "모집 글 ID로 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProjectRecruitmentDetailRes>> getRecruitment(
            @PathVariable Long projectRecruitmentId,
            @AuthenticationPrincipal Long userId
    ) {
        ProjectRecruitmentDetailRes response =
                projectRecruitmentService.getRecruitment(projectRecruitmentId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_RECRUITMENT_FETCH_SUCCESS, response)
        );
    }

    @PatchMapping("/{projectRecruitmentId}")
    @Operation(summary = "프로젝트 팀원 모집 글 수정", description = "모집 글 작성자 본인만 수정할 수 있습니다.")
    public ResponseEntity<ApiResponse<ProjectRecruitmentUpdateRes>> updateRecruitment(
            @PathVariable Long projectRecruitmentId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ProjectRecruitmentUpdateReq request
    ) {
        ProjectRecruitmentUpdateRes response =
                projectRecruitmentService.updateRecruitment(projectRecruitmentId, userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_RECRUITMENT_UPDATE_SUCCESS, response)
        );
    }

    @PostMapping(value = "/{projectRecruitmentId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로젝트 팀원 모집글 이미지 업로드", description = "프로젝트 팀원 모집글 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<String>> uploadRecruitmentImage(
            @PathVariable Long projectRecruitmentId,
            @RequestPart MultipartFile file) {
        String imageUrl = projectRecruitmentService.uploadRecruitmentImage(projectRecruitmentId, file);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.PROJECT_RECRUITMENT_IMAGE_UPLOAD_SUCCESS, imageUrl));
    }

    @DeleteMapping("/{projectRecruitmentId}")
    @Operation(summary = "프로젝트 팀원 모집 글 삭제", description = "모집 글 작성자 본인만 삭제할 수 있습니다.")
    public ResponseEntity<ApiResponse<ProjectRecruitmentDeleteRes>> deleteRecruitment(
            @PathVariable Long projectRecruitmentId,
            @AuthenticationPrincipal Long userId
    ) {
        ProjectRecruitmentDeleteRes response =
                projectRecruitmentService.deleteRecruitment(projectRecruitmentId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PROJECT_RECRUITMENT_DELETE_SUCCESS, response)
        );
    }

}
