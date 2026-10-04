package com.skhu.skhucapstone.mypage.dto.req;

import jakarta.validation.constraints.NotBlank;

public record MypageNameUpdateReq(

        @NotBlank(message = "닉네임은 필수입니다.")
        String name

) {
}
