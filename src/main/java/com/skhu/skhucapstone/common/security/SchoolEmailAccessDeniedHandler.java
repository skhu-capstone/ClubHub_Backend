package com.skhu.skhucapstone.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.common.response.ApiResponse;
import com.skhu.skhucapstone.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SchoolEmailAccessDeniedHandler implements AccessDeniedHandler {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof Long userId) {

            boolean isUnverified = userRepository.findById(userId)
                    .map(user -> !Boolean.TRUE.equals(user.getIsVerified()))
                    .orElse(false);

            if (isUnverified) {
                ErrorCode errorCode = ErrorCode.SCHOOL_EMAIL_VERIFICATION_REQUIRED;

                sendErrorResponse(
                        response,
                        errorCode.getCode(),
                        errorCode.getMessage()
                );
                return;
            }
        }

        sendErrorResponse(
                response,
                "ACCESS_DENIED",
                "접근 권한이 없습니다."
        );
    }

    private void sendErrorResponse(
            HttpServletResponse response,
            String code,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.fail(code, message)
        );
    }
}