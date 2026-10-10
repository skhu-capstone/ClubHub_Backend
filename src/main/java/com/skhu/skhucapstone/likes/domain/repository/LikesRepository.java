package com.skhu.skhucapstone.likes.domain.repository;

import com.skhu.skhucapstone.likes.domain.Likes;
import com.skhu.skhucapstone.post.domain.Post;
import com.skhu.skhucapstone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByPostAndUser(Post post, User user);

    boolean existsByPostAndUser_UserId(Post post, Long userId);

    long countByPost(Post post);

    @Modifying
    @Query("DELETE FROM Likes l WHERE l.post = :post")
    void deleteByPost(@Param("post") Post post);
}