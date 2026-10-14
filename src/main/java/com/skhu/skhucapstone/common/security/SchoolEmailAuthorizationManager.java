package com.skhu.skhucapstone.common.security;

import com.skhu.skhucapstone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SchoolEmailAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserRepository userRepository;

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context
    ) {
        Authentication auth = authentication.get();

        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof Long userId)) {
            return new AuthorizationDecision(false);
        }

        boolean isVerified = userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getIsVerified()))
                .orElse(false);

        return new AuthorizationDecision(isVerified);
    }
}