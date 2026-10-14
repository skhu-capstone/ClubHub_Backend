package com.skhu.skhucapstone.auth.repository;

import com.skhu.skhucapstone.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EmailVerification v set v.attemptCount = v.attemptCount + 1 where v.id = :id")
    void increaseAttemptCount(@Param("id") Long id);

    boolean existsByEmail(String email); // 재발송 요청 시에 기존 거가 있는지 확인

    void deleteByEmail(String email); // 재발송 시 인증번호 삭제
}
