package com.skhu.skhucapstone.notification.domain.repository;

import com.skhu.skhucapstone.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverUserIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverUserIdAndIsReadFalse(Long receiverId);

    // 알림을 하나씩 불러와 바꾸면 쌓인 만큼 쿼리가 늘어나므로 한 번에 갱신한다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.isRead = true "
            + "where n.receiver.userId = :receiverId and n.isRead = false")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);

    // 보관 기간이 지난 알림을 정리할 때 쓴다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Notification n where n.createdAt < :threshold")
    int deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
