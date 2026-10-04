package com.skhu.skhucapstone.mypage.dto.res;

import com.skhu.skhucapstone.coffeechat.dto.res.CoffeeChatProfileRes;
import com.skhu.skhucapstone.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MypageRes {

    private String name;
    private String email;
    private String schoolEmail;
    private List<String> clubs;
    private CoffeeChatProfileRes coffeeChatProfile;

    public static MypageRes of(User user, List<String> clubs, CoffeeChatProfileRes coffeeChatProfile) {
        return MypageRes.builder()
                .name(user.getName())
                .email(user.getEmail())
                .schoolEmail(user.getSchoolEmail())
                .clubs(clubs)
                .coffeeChatProfile(coffeeChatProfile)
                .build();
    }
}
