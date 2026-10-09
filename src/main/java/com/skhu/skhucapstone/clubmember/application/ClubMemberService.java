package com.skhu.skhucapstone.clubmember.application;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.club.domain.repository.ClubRepository;
import com.skhu.skhucapstone.clubmember.api.dto.request.ClubJoinRequest;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubJoinCancelResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubJoinResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.ClubMemberListResponse;
import com.skhu.skhucapstone.clubmember.api.dto.response.MyClubResponse;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import com.skhu.skhucapstone.clubmember.domain.ClubMember;
import com.skhu.skhucapstone.clubmember.domain.ClubRole;
import com.skhu.skhucapstone.clubmember.domain.repository.ClubMemberRepository;
import com.skhu.skhucapstone.coffeechat.repository.CoffeeChatProfileRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.notification.application.NotificationService;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skhu.skhucapstone.clubmember.api.dto.response.MyClubJoinResponse;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    private final CoffeeChatProfileRepository coffeeChatProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public ClubJoinResponse requestJoin(
            Long clubId,
            Long userId,
            ClubJoinRequest request
    ) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Optional<ClubMember> existingClubMember =
                clubMemberRepository.findByClubAndUser(club, user);

        ClubMember clubMember;

        if (existingClubMember.isEmpty()) {
            clubMember = ClubMember.builder()
                    .club(club)
                    .user(user)
                    .role(ClubRole.MEMBER)
                    .clubJoinStatus(ClubJoinStatus.PENDING)
                    .joinMessage(request.joinMessage())
                    .build();

            clubMemberRepository.save(clubMember);
        } else {
            clubMember = existingClubMember.get();

            if (clubMember.getClubJoinStatus() == ClubJoinStatus.PENDING) {
                throw new CustomException(ErrorCode.CLUB_JOIN_ALREADY_PENDING);
            }

            if (clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED) {
                throw new CustomException(ErrorCode.CLUB_MEMBER_ALREADY_JOINED);
            }

            clubMember.reapply(request.joinMessage());
        }

        notifyManagersOfJoinRequest(club, user);

        return ClubJoinResponse.builder()
                .clubId(club.getId())
                .userId(user.getUserId())
                .joinMessage(clubMember.getJoinMessage())
                .clubJoinStatus(clubMember.getClubJoinStatus())
                .requestedAt(clubMember.getRequestedAt())
                .build();
    }

    // 승인·거절은 대표뿐 아니라 운영진도 할 수 있으므로 둘 다에게 알린다.
    private void notifyManagersOfJoinRequest(Club club, User applicant) {
        List<User> managers = clubMemberRepository
                .findByClubAndClubJoinStatus(club, ClubJoinStatus.JOINED)
                .stream()
                .filter(member -> member.getRole() == ClubRole.PRESIDENT
                        || member.getRole() == ClubRole.STAFF)
                .map(ClubMember::getUser)
                .toList();

        notificationService.notifyAll(
                managers,
                applicant,
                NotificationType.CLUB_JOIN_REQUEST,
                applicant.getName() + "님이 " + club.getClubName() + " 동아리에 가입을 신청했습니다.",
                NotificationTargetType.CLUB,
                club.getId()
        );
    }

    @Transactional
    public ClubJoinCancelResponse cancelJoin(Long clubId, Long userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_JOIN_REQUEST_NOT_FOUND
                ));

        if (clubMember.getClubJoinStatus() != ClubJoinStatus.PENDING) {
            throw new CustomException(ErrorCode.CLUB_JOIN_CANCEL_NOT_ALLOWED);
        }

        clubMember.withdraw();

        return ClubJoinCancelResponse.builder()
                .clubId(club.getId())
                .userId(user.getUserId())
                .clubJoinStatus(clubMember.getClubJoinStatus())
                .build();
    }

    public List<ClubMemberListResponse> getClubMembers(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        return clubMemberRepository
                .findByClubAndClubJoinStatus(
                        club,
                        ClubJoinStatus.JOINED
                )
                .stream()
                .map(clubMember -> {

                    Long userId = clubMember.getUser().getUserId();

                    String coffeeChatProfileImageUrl =
                            coffeeChatProfileRepository
                                    .findByUserUserId(userId)
                                    .map(profile ->
                                            profile.getProfileImageUrl() != null
                                                    && !profile.getProfileImageUrl().isBlank()
                                                    ? profile.getProfileImageUrl()
                                                    : clubMember.getUser().getProfileImage()
                                    )
                                    .orElse(clubMember.getUser().getProfileImage());

                    return ClubMemberListResponse.builder()
                            .userId(userId)
                            .name(clubMember.getUser().getName())
                            .profileImage(clubMember.getUser().getProfileImage())
                            .coffeeChatProfileImageUrl(coffeeChatProfileImageUrl)
                            .role(clubMember.getRole())
                            .build();
                })
                .toList();
    }

    public List<MyClubResponse> getMyClubs(Long userId) {
        return clubMemberRepository
                .findByUserUserIdAndClubJoinStatus(
                        userId,
                        ClubJoinStatus.JOINED
                )
                .stream()
                .map(clubMember -> MyClubResponse.builder()
                        .clubId(clubMember.getClub().getId())
                        .clubName(clubMember.getClub().getClubName())
                        .imageUrl(clubMember.getClub().getImageUrl())
                        .category(clubMember.getClub().getCategory())
                        .build())
                .toList();
    }

    public List<MyClubJoinResponse> getMyClubJoins(Long userId) {
        return clubMemberRepository
                .findByUserUserIdAndClubJoinStatusNotOrderByRequestedAtDesc(
                        userId,
                        ClubJoinStatus.JOINED
                )
                .stream()
                .map(clubMember -> MyClubJoinResponse.builder()
                        .clubId(clubMember.getClub().getId())
                        .clubName(clubMember.getClub().getClubName())
                        .category(clubMember.getClub().getCategory())
                        .imageUrl(clubMember.getClub().getImageUrl())
                        .joinMessage(clubMember.getJoinMessage())
                        .clubJoinStatus(clubMember.getClubJoinStatus())
                        .requestedAt(clubMember.getRequestedAt())
                        .build())
                .toList();
    }
}