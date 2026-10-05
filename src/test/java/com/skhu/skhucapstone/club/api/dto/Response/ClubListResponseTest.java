package com.skhu.skhucapstone.club.api.dto.Response;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 동아리 목록의 가입 상태에 따라 신청 가능 여부가 올바르게 계산되는지 검증한다.
class ClubListResponseTest {

    private Club club() {
        return Club.builder()
                .clubName("멋쟁이 사자처럼")
                .category("IT/프로그래밍")
                .shortDescription("코딩으로 세상을 바꾸는 동아리")
                .build();
    }

    @Test
    @DisplayName("신청한 적이 없으면 상태는 비어 있고 신청할 수 있다")
    void neverAppliedCanApply() {
        ClubListResponse res = ClubListResponse.of(club(), null);

        assertThat(res.myJoinStatus()).isNull();
        assertThat(res.canApply()).isTrue();
    }

    @Test
    @DisplayName("거절 또는 탈퇴 상태에서는 다시 신청할 수 있다")
    void rejectedOrWithdrawnCanReapply() {
        assertThat(ClubListResponse.of(club(), ClubJoinStatus.REJECTED).canApply()).isTrue();
        assertThat(ClubListResponse.of(club(), ClubJoinStatus.WITHDRAWN).canApply()).isTrue();
    }

    @Test
    @DisplayName("신청 대기 중이거나 이미 가입한 동아리에는 신청할 수 없다")
    void pendingOrJoinedCannotApply() {
        ClubListResponse pending = ClubListResponse.of(club(), ClubJoinStatus.PENDING);
        ClubListResponse joined = ClubListResponse.of(club(), ClubJoinStatus.JOINED);

        assertThat(pending.myJoinStatus()).isEqualTo(ClubJoinStatus.PENDING);
        assertThat(pending.canApply()).isFalse();

        assertThat(joined.myJoinStatus()).isEqualTo(ClubJoinStatus.JOINED);
        assertThat(joined.canApply()).isFalse();
    }

    @Test
    @DisplayName("비로그인 조회에 쓰이는 from()은 상태 없이 신청 가능으로 내려간다")
    void anonymousUsesEmptyStatus() {
        ClubListResponse res = ClubListResponse.from(club());

        assertThat(res.myJoinStatus()).isNull();
        assertThat(res.canApply()).isTrue();
    }
}
