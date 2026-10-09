package com.skhu.skhucapstone.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 알림은 계속 쌓이기만 하므로 보관 기간이 지난 것은 주기적으로 지운다.
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private static final int RETENTION_DAYS = 30;

    private final NotificationService notificationService;

    // 사용자가 가장 적은 새벽 4시에 하루 한 번 정리한다.
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void deleteExpiredNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);

        int deleted = notificationService.deleteOlderThan(threshold);

        log.info("보관 기간이 지난 알림 {}건을 정리했다. 기준 시각: {}", deleted, threshold);
    }
}
