package com.skhu.skhucapstone.clubevent.application;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.club.domain.repository.ClubRepository;
import com.skhu.skhucapstone.clubevent.api.dto.request.ClubEventCreateRequest;
import com.skhu.skhucapstone.clubevent.api.dto.response.ClubEventListResponse;
import com.skhu.skhucapstone.clubevent.api.dto.response.ClubEventResponse;
import com.skhu.skhucapstone.clubevent.domain.ClubEvent;
import com.skhu.skhucapstone.clubevent.domain.repository.ClubEventRepository;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import com.skhu.skhucapstone.clubmember.domain.ClubMember;
import com.skhu.skhucapstone.clubmember.domain.ClubRole;
import com.skhu.skhucapstone.clubmember.domain.repository.ClubMemberRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.notification.application.NotificationService;
import com.skhu.skhucapstone.notification.domain.NotificationTargetType;
import com.skhu.skhucapstone.notification.domain.NotificationType;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import com.skhu.skhucapstone.clubevent.api.dto.request.ClubEventUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubEventService {

    private final ClubRepository clubRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ClubEventResponse createEvent(
            Long clubId,
            Long userId,
            ClubEventCreateRequest request
    ) {
        Club club = findClub(clubId);
        User user = findUser(userId);

        validateEventManagePermission(club, user);
        validateEventTime(request.startAt(), request.endAt());

        ClubEvent clubEvent = ClubEvent.builder()
                .club(club)
                .creator(user)
                .title(request.title())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .location(request.location())
                .build();

        ClubEvent savedEvent = clubEventRepository.save(clubEvent);

        List<User> members = clubMemberRepository
                .findByClubAndClubJoinStatus(club, ClubJoinStatus.JOINED)
                .stream()
                .map(ClubMember::getUser)
                .toList();

        notificationService.notifyAll(
                members,
                user,
                NotificationType.CLUB_EVENT_CREATED,
                club.getClubName() + " 동아리에 새 일정이 등록되었습니다.",
                NotificationTargetType.CLUB_EVENT,
                savedEvent.getId()
        );

        return ClubEventResponse.from(savedEvent);
    }

    public List<ClubEventListResponse> getMonthlyEvents(
            Long clubId,
            Long userId,
            int year,
            int month
    ) {
        Club club = findClub(clubId);
        User user = findUser(userId);

        validateEventViewPermission(club, user);

        YearMonth yearMonth = validateAndCreateYearMonth(year, month);

        LocalDateTime startAt = yearMonth.atDay(1).atStartOfDay();

        LocalDateTime endAt = yearMonth.plusMonths(1)
                .atDay(1)
                .atStartOfDay();

        return clubEventRepository
                .findByClubAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
                        club,
                        startAt,
                        endAt
                )
                .stream()
                .map(ClubEventListResponse::from)
                .toList();
    }

    public ClubEventResponse getEvent(
            Long clubId,
            Long eventId,
            Long userId
    ) {
        Club club = findClub(clubId);
        User user = findUser(userId);

        validateEventViewPermission(club, user);

        ClubEvent clubEvent = findEvent(eventId, club);

        return ClubEventResponse.from(clubEvent);
    }

    @Transactional
    public ClubEventResponse updateEvent(
            Long clubId,
            Long eventId,
            Long userId,
            ClubEventUpdateRequest request
    ) {
        Club club = findClub(clubId);
        User user = findUser(userId);

        validateEventManagePermission(club, user);

        ClubEvent clubEvent = findEvent(eventId, club);

        validateEventTime(
                request.startAt(),
                request.endAt()
        );

        clubEvent.updateEvent(
                request.title(),
                request.description(),
                request.startAt(),
                request.endAt(),
                request.location()
        );

        return ClubEventResponse.from(clubEvent);
    }

    @Transactional
    public void deleteEvent(
            Long clubId,
            Long eventId,
            Long userId
    ) {
        Club club = findClub(clubId);
        User user = findUser(userId);

        validateEventManagePermission(club, user);

        ClubEvent clubEvent = findEvent(eventId, club);

        clubEventRepository.delete(clubEvent);
    }

    private Club findClub(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private ClubEvent findEvent(Long eventId, Club club) {
        return clubEventRepository.findByIdAndClub(eventId, club)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_EVENT_NOT_FOUND
                ));
    }

    private void validateEventManagePermission(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_EVENT_MANAGE_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;
        boolean manager = clubMember.getRole() == ClubRole.PRESIDENT
                || clubMember.getRole() == ClubRole.STAFF;

        if (!joined || !manager) {
            throw new CustomException(ErrorCode.CLUB_EVENT_MANAGE_FORBIDDEN);
        }
    }

    private void validateEventViewPermission(Club club, User user) {
        ClubMember clubMember = clubMemberRepository.findByClubAndUser(club, user)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.CLUB_EVENT_VIEW_FORBIDDEN
                ));

        boolean joined = clubMember.getClubJoinStatus() == ClubJoinStatus.JOINED;

        if (!joined) {
            throw new CustomException(ErrorCode.CLUB_EVENT_VIEW_FORBIDDEN);
        }
    }

    private void validateEventTime(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        if (!endAt.isAfter(startAt)) {
            throw new CustomException(ErrorCode.CLUB_EVENT_INVALID_TIME);
        }
    }

    private YearMonth validateAndCreateYearMonth(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.INVALID_SEARCH_CONDITION);
        }
    }
}