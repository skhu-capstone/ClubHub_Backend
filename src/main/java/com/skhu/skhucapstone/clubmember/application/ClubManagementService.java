package com.skhu.skhucapstone.clubmember.application;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.club.domain.repository.ClubRepository;
import com.skhu.skhucapstone.clubmember.api.dto.request.ClubMemberRoleUpdateRequest;
import com.skhu.skhucapstone.clubmember.api.dto.request.ClubPresidentTransferRequest;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubJoinApplicantResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubJoinProcessResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubMemberRemoveResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubMemberRoleUpdateResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubPresidentTransferResponse;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import com.skhu.skhucapstone.clubmember.domain.ClubMember;
import com.skhu.skhucapstone.clubmember.domain.ClubRole;
import com.skhu.skhucapstone.clubmember.domain.repository.ClubMemberRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagementService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;

    public List<ClubJoinApplicantResponse> getJoinApplicants(Long clubId, Long userId) {
        Club club = findClub(clubId);
        User user = findUser(userId);

        validateJoinListPermission(club, user);

        return clubMemberRepository
                .findByClubAndClubJoinStatusOrderByRequestedAtDesc(
                        club,
                        ClubJoinStatus.PENDING
                )
                .stream()
                .map(clubMember -> ClubJoinApplicantResponse.builder()
                        .userId(clubMember.getUser().getUserId())
                        .name(clubMember.getUser().getName())
                        .profileImage(clubMember.getUser().getProfileImage())
                        .joinMessage(clubMember.getJoinMessage())
                        .clubJoinStatus(clubMember.getClubJoinStatus())
                        .requestedAt(clubMember.getRequestedAt())
                        .build())
                .toList();
    }

    @Transactional
    public ClubJoinProcessResponse approveJoin(
            Long clubId,
            Long applicantUserId,
            Long managerUserId
    ) {
        Club club = findClub(clubId);
        User manager = findUser(managerUserId);

        validateJoinManagePermission(club, manager);

        User applicant = findUser(applicantUserId);
        ClubMember applicantMember = findApplicant(club, applicant);

        validatePendingApplicant(applicantMember);
        applicantMember.approveJoin();

        return toJoinProcessResponse(applicantMember);
    }

    @Transactional
    public ClubJoinProcessResponse rejectJoin(
            Long clubId,
            Long applicantUserId,
            Long managerUserId
    ) {
        Club club = findClub(clubId);
        User manager = findUser(managerUserId);

        validateJoinManagePermission(club, manager);

        User applicant = findUser(applicantUserId);
        ClubMember applicantMember = findApplicant(club, applicant);

        validatePendingApplicant(applicantMember);
        applicantMember.rejectJoin();

        return toJoinProcessResponse(applicantMember);
    }

    @Transactional
    public ClubMemberRoleUpdateResponse updateMemberRole(
            Long clubId,
            Long targetUserId,
            Long managerUserId,
            ClubMemberRoleUpdateRequest request
    ) {
        Club club = findClub(clubId);
        User manager = findUser(managerUserId);

        validateRoleManagePermission(club, manager);

        User targetUser = findUser(targetUserId);
        ClubMember targetMember = clubMemberRepository.findByClubAndUser(club, targetUser)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND));

        validateRoleUpdate(targetMember, request.role());
        targetMember.changeRole(request.role());

        return ClubMemberRoleUpdateResponse.builder()
                .clubId(club.getId())
                .userId(targetUser.getUserId())
                .role(targetMember.getRole())
                .build();
    }

    @Transactional
    public ClubPresidentTransferResponse transferPresident(
            Long clubId,
            Long currentPresidentUserId,
            ClubPresidentTransferRequest request
    ) {
        Club club = findClub(clubId);
        User currentPresident = findUser(currentPresidentUserId);
        ClubMember currentPresidentMember = findCurrentPresident(club, currentPresident);

        if (currentPresidentUserId.equals(request.newPresidentUserId())) {
            throw new CustomException(ErrorCode.CLUB_PRESIDENT_TRANSFER_TO_SELF);
        }

        User newPresident = findUser(request.newPresidentUserId());
        ClubMember newPresidentMember = clubMemberRepository.findByClubAndUser(club, newPresident)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_PRESIDENT_TRANSFER_TARGET_NOT_FOUND
                ));

        validatePresidentTransferTarget(newPresidentMember);
        validatePresidentLimit(newPresident.getUserId());

        currentPresidentMember.changeRole(ClubRole.STAFF);
        newPresidentMember.changeRole(ClubRole.PRESIDENT);

        return ClubPresidentTransferResponse.builder()
                .clubId(club.getId())
                .previousPresidentUserId(currentPresident.getUserId())
                .previousPresidentRole(currentPresidentMember.getRole())
                .newPresidentUserId(newPresident.getUserId())
                .newPresidentRole(newPresidentMember.getRole())
                .build();
    }

    @Transactional
    public ClubMemberRemoveResponse removeMember(
            Long clubId,
            Long targetUserId,
            Long managerUserId
    ) {
        Club club = findClub(clubId);
        User manager = findUser(managerUserId);

        validateMemberRemovePermission(club, manager);

        if (managerUserId.equals(targetUserId)) {
            throw new CustomException(ErrorCode.CLUB_MEMBER_REMOVE_SELF);
        }

        User targetUser = findUser(targetUserId);
        ClubMember targetMember = clubMemberRepository.findByClubAndUser(club, targetUser)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_MEMBER_NOT_FOUND));

        validateMemberRemoveTarget(targetMember);
        targetMember.removeFromClub();

        return ClubMemberRemoveResponse.builder()
                .clubId(club.getId())
                .userId(targetUser.getUserId())
                .clubJoinStatus(targetMember.getClubJoinStatus())
                .build();
    }

    private Club findClub(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private ClubMember findApplicant(Club club, User applicant) {
        return clubMemberRepository.findByClubAndUser(club, applicant)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_JOIN_APPLICANT_NOT_FOUND
                ));
    }

    private ClubMember findCurrentPresident(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_PRESIDENT_TRANSFER_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean president = clubMember.getRole() == ClubRole.PRESIDENT;

        if (!joined || !president) {
            throw new CustomException(ErrorCode.CLUB_PRESIDENT_TRANSFER_FORBIDDEN);
        }

        return clubMember;
    }

    private void validateJoinListPermission(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_JOIN_LIST_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean manager = clubMember.getRole() == ClubRole.PRESIDENT
                || clubMember.getRole() == ClubRole.STAFF;

        if (!joined || !manager) {
            throw new CustomException(ErrorCode.CLUB_JOIN_LIST_FORBIDDEN);
        }
    }

    private void validateJoinManagePermission(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_JOIN_MANAGE_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean president = clubMember.getRole() == ClubRole.PRESIDENT;

        if (!joined || !president) {
            throw new CustomException(ErrorCode.CLUB_JOIN_MANAGE_FORBIDDEN);
        }
    }

    private void validateRoleManagePermission(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_MEMBER_ROLE_MANAGE_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean president = clubMember.getRole() == ClubRole.PRESIDENT;

        if (!joined || !president) {
            throw new CustomException(ErrorCode.CLUB_MEMBER_ROLE_MANAGE_FORBIDDEN);
        }
    }

    private void validatePendingApplicant(ClubMember applicantMember) {
        if (applicantMember.getClubJoinStatus() != ClubJoinStatus.PENDING) {
            throw new CustomException(ErrorCode.CLUB_JOIN_NOT_PENDING);
        }
    }

    private void validateRoleUpdate(ClubMember targetMember, ClubRole requestedRole) {
        boolean joined = targetMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean targetIsPresident = targetMember.getRole() == ClubRole.PRESIDENT;
        boolean allowedRequestedRole = requestedRole == ClubRole.MEMBER
                || requestedRole == ClubRole.STAFF;

        if (!joined || targetIsPresident || !allowedRequestedRole) {
            throw new CustomException(ErrorCode.CLUB_MEMBER_ROLE_UPDATE_NOT_ALLOWED);
        }

        if (targetMember.getRole() == requestedRole) {
            throw new CustomException(ErrorCode.CLUB_MEMBER_SAME_ROLE);
        }
    }

    private void validatePresidentTransferTarget(ClubMember newPresidentMember) {
        boolean joined = newPresidentMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean allowedRole = newPresidentMember.getRole() == ClubRole.MEMBER
                || newPresidentMember.getRole() == ClubRole.STAFF;

        if (!joined || !allowedRole) {
            throw new CustomException(ErrorCode.CLUB_PRESIDENT_TRANSFER_NOT_ALLOWED);
        }
    }

    private void validatePresidentLimit(Long userId) {
        long presidentCount = clubMemberRepository
                .countByUserUserIdAndRoleAndClubJoinStatus(
                        userId,
                        ClubRole.PRESIDENT,
                        ClubJoinStatus.JOINED
                );

        if (presidentCount >= 2) {
            throw new CustomException(ErrorCode.CLUB_PRESIDENT_ALREADY_EXISTS);
        }
    }

    private ClubJoinProcessResponse toJoinProcessResponse(ClubMember clubMember) {
        return ClubJoinProcessResponse.builder()
                .clubId(clubMember.getClub().getId())
                .userId(clubMember.getUser().getUserId())
                .role(clubMember.getRole())
                .clubJoinStatus(clubMember.getClubJoinStatus())
                .joinedAt(clubMember.getJoinedAt())
                .build();
    }

    private void validateMemberRemovePermission(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_MEMBER_REMOVE_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean president = clubMember.getRole() == ClubRole.PRESIDENT;

        if (!joined || !president) {
            throw new CustomException(ErrorCode.CLUB_MEMBER_REMOVE_FORBIDDEN);
        }
    }

    private void validateMemberRemoveTarget(ClubMember targetMember) {
        boolean joined = targetMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean removableRole = targetMember.getRole() == ClubRole.MEMBER
                || targetMember.getRole() == ClubRole.STAFF;

        if (!joined || !removableRole) {
            throw new CustomException(ErrorCode.CLUB_MEMBER_REMOVE_NOT_ALLOWED);
        }
    }
}