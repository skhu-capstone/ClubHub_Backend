package com.skhu.skhucapstone.club.api.dto.Response;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;

public record ClubListResponse(
        Long id,
        String clubName,
        String category,
        String shortDescription,
        String imageUrl,

        // 요청한 사용자의 가입 상태. 신청한 적이 없거나 비로그인이면 null이다.
        ClubJoinStatus myJoinStatus,

        // 지금 가입 신청을 할 수 있는지. 거절·탈퇴 상태에서는 재신청할 수 있다.
        boolean canApply
) {
    public static ClubListResponse from(Club club) {
        return of(club, null);
    }

    public static ClubListResponse of(Club club, ClubJoinStatus myJoinStatus) {
        return new ClubListResponse(
                club.getId(),
                club.getClubName(),
                club.getCategory(),
                club.getShortDescription(),
                club.getImageUrl(),
                myJoinStatus,
                canApply(myJoinStatus)
        );
    }

    private static boolean canApply(ClubJoinStatus myJoinStatus) {
        return myJoinStatus == null
                || myJoinStatus == ClubJoinStatus.REJECTED
                || myJoinStatus == ClubJoinStatus.WITHDRAWN;
    }
}
