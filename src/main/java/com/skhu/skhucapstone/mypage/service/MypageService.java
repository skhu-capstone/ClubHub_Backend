package com.skhu.skhucapstone.mypage.service;

import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import com.skhu.skhucapstone.clubmember.domain.ClubMember;
import com.skhu.skhucapstone.clubmember.domain.repository.ClubMemberRepository;
import com.skhu.skhucapstone.coffeechat.dto.res.CoffeeChatProfileRes;
import com.skhu.skhucapstone.coffeechat.repository.CoffeeChatProfileRepository;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.mypage.dto.res.MypageRes;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skhu.skhucapstone.mypage.dto.req.MypageNameUpdateReq;
import com.skhu.skhucapstone.mypage.dto.res.MypageNameUpdateRes;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
    private final CoffeeChatProfileRepository coffeeChatProfileRepository;
    private final ClubMemberRepository clubMemberRepository;

    // 마이페이지 조회 (유저 정보 + 동아리 + 커피챗 프로필)
    @Transactional(readOnly = true)
    public MypageRes getMypage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<String> clubs = getClubs(userId);

        // 커피챗 프로필 없으면 null
        CoffeeChatProfileRes coffeeChatProfileRes = coffeeChatProfileRepository.findByUserUserId(userId)
                .map(CoffeeChatProfileRes::from)
                .orElse(null);

        return MypageRes.of(user, clubs, coffeeChatProfileRes);
    }


    // 유저의 동아리 목록 조회 (동아리명 / 역할)
    private List<String> getClubs(Long userId) {
        List<ClubMember> clubMembers = clubMemberRepository
                .findByUserUserIdAndClubJoinStatus(userId, ClubJoinStatus.JOINED);

        // 가입한 동아리 없으면 null
        if (clubMembers.isEmpty()) return null;

        return clubMembers.stream()
                .map(cm -> cm.getClub().getClubName() + " / " + cm.getRole().name())
                .collect(Collectors.toList());
    }

    @Transactional
    public MypageNameUpdateRes updateName(
            Long userId,
            MypageNameUpdateReq req
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updateName(req.name());

        return MypageNameUpdateRes.builder().name(user.getName()).build();
    }
}
