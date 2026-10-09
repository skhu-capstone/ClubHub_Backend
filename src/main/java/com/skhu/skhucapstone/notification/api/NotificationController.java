package com.skhu.skhucapstone.notification.api;

import com.skhu.skhucapstone.common.exception.SuccessCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import com.skhu.skhucapstone.notification.api.dto.response.NotificationPageResponse;
import com.skhu.skhucapstone.notification.api.dto.response.NotificationUnreadCountResponse;
import com.skhu.skhucapstone.notification.application.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 사용자의 알림을 최신순으로 조회합니다. "
                    + "안 읽은 알림 개수(unreadCount)를 함께 반환하므로 목록 화면에서 뱃지를 따로 조회하지 않아도 됩니다."
    )
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        NotificationPageResponse response =
                notificationService.getNotifications(userId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.NOTIFICATION_LIST_GET_SUCCESS, response));
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "안 읽은 알림 개수 조회",
            description = "헤더 뱃지에 사용합니다. 목록을 내려받지 않고 개수만 확인하므로 주기적으로 호출해도 부담이 적습니다."
    )
    public ResponseEntity<ApiResponse<NotificationUnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal Long userId) {

        long unreadCount = notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(ApiResponse.success(
                SuccessCode.NOTIFICATION_UNREAD_COUNT_GET_SUCCESS,
                NotificationUnreadCountResponse.of(unreadCount)));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(
            summary = "알림 개별 읽음 처리",
            description = "알림 하나를 읽음으로 바꿉니다. 본인에게 온 알림만 처리할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId) {

        notificationService.markAsRead(notificationId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.NOTIFICATION_READ_SUCCESS, null));
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "알림 전체 읽음 처리",
            description = "로그인한 사용자의 안 읽은 알림을 모두 읽음으로 바꿉니다."
    )
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal Long userId) {

        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.NOTIFICATION_READ_ALL_SUCCESS, null));
    }
}
