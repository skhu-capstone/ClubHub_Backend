package com.skhu.skhucapstone.comment.domain.repository;

import com.skhu.skhucapstone.comment.domain.Comment;
import com.skhu.skhucapstone.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post = :post")
    void deleteByPost(@Param("post") Post post);
}