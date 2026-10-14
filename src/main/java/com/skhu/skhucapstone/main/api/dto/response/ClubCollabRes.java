package com.skhu.skhucapstone.main.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClubCollabRes {

    private Long collabId;

    private String title;

    private String content;

    private String dDayText;

    private String clubName;

    private LocalDateTime createdAt;
}