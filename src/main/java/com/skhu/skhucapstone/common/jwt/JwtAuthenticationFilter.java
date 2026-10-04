package com.skhu.skhucapstone.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (request.getRequestURI().startsWith("/api/clubs")) {
            System.out.println("=== CLUB JWT CHECK ===");
            System.out.println("method = " + request.getMethod());
            System.out.println("uri = " + request.getRequestURI());
            System.out.println("Authorization header 존재 = "
                    + (request.getHeader("Authorization") != null));
            System.out.println("token 존재 = " + (token != null));
        }

        if (token != null && jwtUtil.isTokenValid(token)) {
            Long userId = jwtUtil.getUserId(token);

            if (request.getRequestURI().startsWith("/api/clubs")) {
                System.out.println("JWT 인증 성공 userId = " + userId);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else if (token != null) {

            if (request.getRequestURI().startsWith("/api/clubs")) {
                System.out.println("JWT 검증 실패");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}