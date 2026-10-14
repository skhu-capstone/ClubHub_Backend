package com.skhu.skhucapstone.projectrecruitment.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectRecruitmentListRes {

    private Long projectRecruitmentId;

    private String title;

    private String content;

    private String imageUrl;

    private LocalDate deadline;

    private String dDay;

    private LocalDateTime createdAt;
}