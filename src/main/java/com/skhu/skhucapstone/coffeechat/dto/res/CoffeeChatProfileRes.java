package com.skhu.skhucapstone.coffeechat.dto.res;

import com.skhu.skhucapstone.coffeechat.entity.CoffeeChatProfile;
import com.skhu.skhucapstone.coffeechat.entity.MeetingType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoffeeChatProfileRes {

    private Long coffeeChatProfileId;
    private String studentId;
    private String headline;
    private String interestTopics;
    private MeetingType meetingType;
    private String contactLink;
    private String introduction;
    private String profileImageUrl;
    private Boolean isPublic;

    public static CoffeeChatProfileRes from(CoffeeChatProfile profile) {
        return CoffeeChatProfileRes.builder()
                .coffeeChatProfileId(profile.getId())
                .studentId(profile.getStudentId())
                .headline(profile.getHeadline())
                .interestTopics(profile.getInterestTopics())
                .meetingType(profile.getMeetingType())
                .contactLink(profile.getContactLink())
                .introduction(profile.getIntroduction())
                .profileImageUrl(
                        profile.getProfileImageUrl() != null
                                && !profile.getProfileImageUrl().isBlank()
                                ? profile.getProfileImageUrl()
                                : profile.getUser().getProfileImage()
                )
                .isPublic(profile.getIsPublic())
                .build();
    }
}
