package com.skhu.skhucapstone.post.domain.repository;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByClub(Club club);

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByClubOrderByCreatedAtDesc(Club club);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByClubOrderByCreatedAtDesc(Club club, Pageable pageable);

    @Query("""
    SELECT p
    FROM Post p
    LEFT JOIN Likes l ON l.post = p
    GROUP BY p
    ORDER BY COUNT(l) DESC, p.createdAt DESC
    """)
    Page<Post> findAllOrderByLikeCountDesc(Pageable pageable);

    @Query(
            value = """
            SELECT p
            FROM Post p
            WHERE p.club = :club
            ORDER BY
                CASE WHEN p.isPinned = true THEN 0 ELSE 1 END ASC,
                CASE WHEN p.displayOrder IS NULL THEN 0 ELSE 1 END ASC,
                p.displayOrder ASC,
                p.createdAt DESC,
                p.postId DESC
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM Post p
            WHERE p.club = :club
            """
    )
    Page<Post> findByClubOrderByDisplayOrder(
            @Param("club") Club club,
            Pageable pageable
    );

}