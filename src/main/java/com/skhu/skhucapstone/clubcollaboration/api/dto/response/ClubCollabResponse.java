package com.skhu.skhucapstone.clubcollaboration.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClubCollabResponse {

    private Long collabId;

    private Long clubId;

    private String clubName;

    private String title;

    private String imageUrl;

    private String contestName;

    private LocalDate contestDate;

    private String content;

    private LocalDate deadline;

    private String dDayText;

    private String writerName;

    private LocalDateTime createdAt;

    // 요청한 사용자가 이 협업 모집글을 수정할 수 있는지 (작성자 본인만)
    private boolean canUpdate;

    // 요청한 사용자가 이 협업 모집글을 삭제할 수 있는지 (작성자 본인만)
    private boolean canDelete;
}