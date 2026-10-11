package com.skhu.skhucapstone.post.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostOrderUpdateRequest(

        @NotEmpty(message = "게시글 순서 목록은 필수입니다.")
        List<@NotNull(message = "게시글 ID는 null일 수 없습니다.") Long> postIds

) {
}