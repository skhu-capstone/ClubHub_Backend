package com.skhu.skhucapstone.post.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record PostPinUpdateRequest(

        @NotNull(message = "게시글 고정 여부는 필수입니다.")
        Boolean isPinned
) {
}