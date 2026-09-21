package com.skhu.skhucapstone.clubmember.domain.repository;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.clubmember.domain.ClubJoinStatus;
import com.skhu.skhucapstone.clubmember.domain.ClubMember;
import com.skhu.skhucapstone.clubmember.domain.ClubRole;
import com.skhu.skhucapstone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    List<ClubMember> findByUserUserIdAndClubJoinStatus(Long userId, ClubJoinStatus clubJoinStatus);
    boolean existsByUserUserIdAndRoleAndClubJoinStatus(Long userId, ClubRole role, ClubJoinStatus clubJoinStatus);
    boolean existsByClubAndUser(Club club, User user);
    Optional<ClubMember> findByClubAndUser(Club club, User user);
    List<ClubMember> findByClubAndClubJoinStatus(Club club, ClubJoinStatus clubJoinStatus);
    long countByClubAndClubJoinStatus(Club club, ClubJoinStatus clubJoinStatus);
    List<ClubMember> findByUserUserIdAndClubJoinStatusNotOrderByRequestedAtDesc(Long userId, ClubJoinStatus clubJoinStatus);
    List<ClubMember> findByClubAndClubJoinStatusOrderByRequestedAtDesc(Club club, ClubJoinStatus clubJoinStatus);
    long countByUserUserIdAndRoleAndClubJoinStatus(Long userId, ClubRole role, ClubJoinStatus clubJoinStatus);
}