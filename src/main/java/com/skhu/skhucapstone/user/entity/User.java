package com.skhu.skhucapstone.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicInsert
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email; // 구글 이메일

    @Column(nullable = false)
    private String name;

    private String profileImage;

    private String schoolEmail;

    @Builder.Default
    @ColumnDefault("false") // 기본값이 false, @DynamicInsert를 같이 써야함
    @Column(nullable = false)
    private Boolean isVerified = false;

    public void verifySchoolEmail(String schoolEmail) {
        this.schoolEmail = schoolEmail;
        this.isVerified = true;
    }

    public void updateName(String name) {
        this.name = name;
    }
}