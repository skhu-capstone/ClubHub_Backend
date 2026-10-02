package com.skhu.skhucapstone.projectrecruitment.dto.res;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Redis 캐시에 JSON으로 저장되므로 Jackson이 기본 지원하는 record로 정의한다.
// dDay는 조회 시점마다 달라지는 값이라 캐시에는 null로 저장하고,
// 서비스에서 toBuilder()로 매번 새로 계산해 채운다.
@Builder(toBuilder = true)
public record ProjectRecruitmentDetailRes(
        Long projectRecruitmentId,
        Long writerId,
        String title,
        String imageUrl,
        String writerName,
        String writerStack,
        String positions,
        String content,
        LocalDate deadline,
        String dDay,
        LocalDateTime createdAt,

        // 요청한 사용자에 따라 달라지는 값이라 dDay와 마찬가지로 캐시에 저장하지 않고
        // 서비스에서 매 요청마다 계산해 채운다. (수정·삭제 모두 작성자 본인만 가능)
        boolean canUpdate,
        boolean canDelete
) {
}
