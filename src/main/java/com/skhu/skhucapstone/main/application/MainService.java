package com.skhu.skhucapstone.main.application;

import com.skhu.skhucapstone.clubcollaboration.api.dto.response.ClubCollabResponse;
import com.skhu.skhucapstone.clubcollaboration.application.ClubCollabService;
import com.skhu.skhucapstone.coffeechat.dto.res.CoffeeChatProfileListItemRes;
import com.skhu.skhucapstone.coffeechat.service.CoffeeChatService;
import com.skhu.skhucapstone.main.api.dto.response.ClubCollabRes;
import com.skhu.skhucapstone.main.api.dto.response.ClubFeedRes;
import com.skhu.skhucapstone.main.api.dto.response.CoffeeChatRes;
import com.skhu.skhucapstone.main.api.dto.response.MainResponse;
import com.skhu.skhucapstone.main.api.dto.response.ProjectRecruitRes;
import com.skhu.skhucapstone.post.api.dto.response.PostResponse;
import com.skhu.skhucapstone.post.application.PostService;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentListRes;
import com.skhu.skhucapstone.projectrecruitment.service.ProjectRecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final CoffeeChatService coffeeChatService;
    private final ProjectRecruitmentService projectRecruitmentService;
    private final ClubCollabService clubCollabService;
    private final PostService postService;

    public MainResponse getMainPage() {
        return MainResponse.builder()
                .recommendedCoffeeChats(getCoffeeChats())
                .projectRecruitments(getProjectRecruitments())
                .clubCollaborations(getClubCollaborations())
                .clubFeeds(getClubFeeds())
                .build();
    }

    private List<CoffeeChatRes> getCoffeeChats() {
        return coffeeChatService.getProfiles(null, 0, 998)
                .getContent()
                .stream()
                .map(this::toCoffeeChatRes)
                .toList();
    }

    private List<ProjectRecruitRes> getProjectRecruitments() {
        return projectRecruitmentService.getRecruitments(null, 0, 998)
                .getContent()
                .stream()
                .map(this::toProjectRecruitRes)
                .toList();
    }

    private List<ClubCollabRes> getClubCollaborations() {
        return clubCollabService.getCollabs(null, 0, 998)
                .getContent()
                .stream()
                .map(this::toClubCollabRes)
                .toList();
    }

    private List<ClubFeedRes> getClubFeeds() {
        return postService.getPosts(0, 998)
                .getContent()
                .stream()
                .map(this::toClubFeedRes)
                .toList();
    }

    private CoffeeChatRes toCoffeeChatRes(
            CoffeeChatProfileListItemRes profile
    ) {
        return CoffeeChatRes.builder()
                .coffeeChatProfileId(profile.getCoffeeChatProfileId())
                .userId(profile.getUserId())
                .name(profile.getName())
                .profileImageUrl(profile.getProfileImageUrl())
                .headline(profile.getHeadline())
                .interestTopics(profile.getInterestTopics())
                .clubs(profile.getClubs())
                .meetingType(profile.getMeetingType())
                .build();
    }

    private ProjectRecruitRes toProjectRecruitRes(
            ProjectRecruitmentListRes recruitment
    ) {
        return ProjectRecruitRes.builder()
                .projectRecruitmentId(
                        recruitment.getProjectRecruitmentId()
                )
                .title(recruitment.getTitle())
                .content(recruitment.getContent())
                .imageUrl(recruitment.getImageUrl())
                .deadline(recruitment.getDeadline())
                .dDay(recruitment.getDDay())
                .build();
    }

    private ClubCollabRes toClubCollabRes(
            ClubCollabResponse collab
    ) {
        return ClubCollabRes.builder()
                .collabId(collab.getCollabId())
                .title(collab.getTitle())
                .content(collab.getContent())
                .dDayText(collab.getDDayText())
                .clubName(collab.getClubName())
                .build();
    }

    private ClubFeedRes toClubFeedRes(PostResponse post) {
        return ClubFeedRes.builder()
                .clubName(post.getClubName())
                .postId(post.getPostId())
                .writerName(post.getWriterName())
                .writerCoffeeChatProfileImageUrl(post.getWriterCoffeeChatProfileImageUrl())
                .createdAt(post.getCreatedAt())
                .imageUrls(post.getImageUrls())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .liked(post.isLiked())
                .comments(post.getComments())
                .build();
    }
}