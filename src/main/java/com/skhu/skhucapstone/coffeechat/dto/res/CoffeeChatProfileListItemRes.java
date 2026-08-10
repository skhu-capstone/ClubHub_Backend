package com.skhu.skhucapstone.coffeechat.dto.res;

import com.skhu.skhucapstone.coffeechat.entity.CoffeeChatProfile;
import com.skhu.skhucapstone.coffeechat.entity.MeetingType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CoffeeChatProfileListItemRes {

    private Long coffeeChatProfileId;
    private Long userId;
    private String name;
    private String headline;
    private String interestTopics;
    private List<String> clubs;
    private MeetingType meetingType;
    private Boolean isPublic;
    private String profileImageUrl;

    public static CoffeeChatProfileListItemRes of(
            CoffeeChatProfile profile,
            List<String> clubs
    ) {
        return CoffeeChatProfileListItemRes.builder()
                .coffeeChatProfileId(profile.getId())
                .userId(profile.getUser().getUserId())
                .name(profile.getUser().getName())
                .profileImageUrl(
                        profile.getProfileImageUrl() != null
                                && !profile.getProfileImageUrl().isBlank()
                                ? profile.getProfileImageUrl()
                                : profile.getUser().getProfileImage()
                )
                .headline(profile.getHeadline())
                .interestTopics(profile.getInterestTopics())
                .clubs(clubs)
                .meetingType(profile.getMeetingType())
                .isPublic(profile.getIsPublic())
                .build();
    }
}
