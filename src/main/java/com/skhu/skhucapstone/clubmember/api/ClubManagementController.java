package com.skhu.skhucapstone.clubmember.api;

import com.skhu.skhucapstone.clubmember.api.dto.request.ClubMemberRoleUpdateRequest;
import com.skhu.skhucapstone.clubmember.api.dto.request.ClubPresidentTransferRequest;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubJoinApplicantResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubJoinProcessResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubMemberRoleUpdateResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubPresidentTransferResponse;
import com.skhu.skhucapstone.clubmember.application.ClubManagementService;
import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubMemberRemoveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs/{clubId}")
@RequiredArgsConstructor
@Tag(name = "Club Management", description = "동아리 대표 및 운영진 관리 API")
public class ClubManagementController {

    private final ClubManagementService clubManagementService;

    @GetMapping("/join")
    @Operation(
            summary = "동아리 가입 신청자 목록 조회",
            description = "동아리 대표와 운영진이 가입 대기 중인 신청자 목록을 최근 신청 순으로 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<ClubJoinApplicantResponse>>> getJoinApplicants(
            @PathVariable Long clubId,
            @AuthenticationPrincipal Long userId) {

        List<ClubJoinApplicantResponse> response = clubManagementService.getJoinApplicants(clubId, userId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_JOIN_LIST_GET_SUCCESS, response));
    }

    @PatchMapping("/join/{applicantUserId}/approve")
    @Operation(
            summary = "동아리 가입 신청 승인",
            description = "동아리 대표가 가입 대기 중인 사용자의 가입 신청을 승인합니다."
    )
    public ResponseEntity<ApiResponse<ClubJoinProcessResponse>> approveJoin(
            @PathVariable Long clubId,
            @PathVariable Long applicantUserId,
            @AuthenticationPrincipal Long managerUserId) {

        ClubJoinProcessResponse response = clubManagementService.approveJoin(clubId, applicantUserId, managerUserId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_JOIN_APPROVE_SUCCESS, response));
    }

    @PatchMapping("/join/{applicantUserId}/reject")
    @Operation(
            summary = "동아리 가입 신청 거절",
            description = "동아리 대표가 가입 대기 중인 사용자의 가입 신청을 거절합니다."
    )
    public ResponseEntity<ApiResponse<ClubJoinProcessResponse>> rejectJoin(
            @PathVariable Long clubId,
            @PathVariable Long applicantUserId,
            @AuthenticationPrincipal Long managerUserId) {

        ClubJoinProcessResponse response = clubManagementService.rejectJoin(clubId, applicantUserId, managerUserId);

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_JOIN_REJECT_SUCCESS, response));
    }

    @PatchMapping("/members/{targetUserId}/role")
    @Operation(
            summary = "동아리 멤버 역할 변경",
            description = "동아리 대표가 가입 완료된 일반 멤버와 운영진의 역할을 변경합니다."
    )
    public ResponseEntity<ApiResponse<ClubMemberRoleUpdateResponse>> updateMemberRole(
            @PathVariable Long clubId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal Long managerUserId,
            @Valid @RequestBody ClubMemberRoleUpdateRequest request) {

        ClubMemberRoleUpdateResponse response =
                clubManagementService.updateMemberRole(
                        clubId,
                        targetUserId,
                        managerUserId,
                        request
                );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_MEMBER_ROLE_UPDATE_SUCCESS, response));
    }

    @PatchMapping("/president")
    @Operation(
            summary = "동아리 대표 권한 이전",
            description = "현재 동아리 대표가 가입 완료된 일반 멤버 또는 운영진에게 대표 권한을 이전합니다."
    )
    public ResponseEntity<ApiResponse<ClubPresidentTransferResponse>> transferPresident(
            @PathVariable Long clubId,
            @AuthenticationPrincipal Long currentPresidentUserId,
            @Valid @RequestBody ClubPresidentTransferRequest request) {

        ClubPresidentTransferResponse response = clubManagementService.transferPresident(clubId, currentPresidentUserId, request
        );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_PRESIDENT_TRANSFER_SUCCESS, response));
    }
    @DeleteMapping("/members/{targetUserId}")
    @Operation(
            summary = "동아리 멤버 내보내기",
            description = "동아리 대표가 가입 완료된 일반 멤버 또는 운영진을 동아리에서 내보냅니다."
    )
    public ResponseEntity<ApiResponse<ClubMemberRemoveResponse>> removeMember(
            @PathVariable Long clubId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal Long managerUserId) {

        System.out.println("=== DELETE MEMBER CONTROLLER 진입 ===");
        System.out.println("clubId = " + clubId);
        System.out.println("targetUserId = " + targetUserId);
        System.out.println("managerUserId = " + managerUserId);

        ClubMemberRemoveResponse response =
                clubManagementService.removeMember(
                        clubId,
                        targetUserId,
                        managerUserId
                );

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.CLUB_MEMBER_REMOVE_SUCCESS, response));
    }
}