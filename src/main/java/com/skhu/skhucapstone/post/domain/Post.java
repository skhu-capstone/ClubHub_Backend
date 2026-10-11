package com.skhu.skhucapstone.post.domain;

import com.skhu.skhucapstone.club.domain.Club;
import com.skhu.skhucapstone.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    @Column(name = "display_order")
    private Long displayOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    public void updatePost(
            String title,
            String content,
            PostType postType
    ) {
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

}