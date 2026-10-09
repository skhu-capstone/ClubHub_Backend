package com.skhu.skhucapstone.common.security;

import com.skhu.skhucapstone.common.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SchoolEmailAuthorizationManager schoolEmailAuthorizationManager;
    private final SchoolEmailAccessDeniedHandler schoolEmailAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 구글 로그인
                        .requestMatchers("/api/auth/google/login").permitAll()

                        // 학교 이메일 인증 관련 API
                        .requestMatchers("/api/auth/email/**").authenticated()

                        // 메인페이지는 누구나 조회 가능
                        .requestMatchers(HttpMethod.GET, "/api/main").permitAll()

                        // 커피챗 조회는 학교 이메일 인증 필수
                        .requestMatchers(HttpMethod.GET,
                                "/api/coffeechat/profiles",
                                "/api/coffeechat/profiles/**"
                        ).access(schoolEmailAuthorizationManager)

                        // 비로그인 사용자도 조회 가능
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts",
                                "/api/posts/**",
                                "/api/clubs/**",
                                "/api/club-collaborations/**",
                                "/api/project-recruitments/**"
                        ).permitAll()

                        // 학교 이메일 인증 후 이용 가능한 개인 기능
                        .requestMatchers(HttpMethod.GET,
                                "/api/chat/**",
                                "/api/users/me/**"
                        ).access(schoolEmailAuthorizationManager)

                        // 기타 공개 경로
                        .requestMatchers(
                                "/error",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/ws/**",
                                "/actuator/**"
                        ).permitAll()

                        // 나머지 API는 학교 이메일 인증 필수
                        .anyRequest().access(schoolEmailAuthorizationManager))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(schoolEmailAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://skhucapstone.duckdns.org",
                "https://capstone-frontend-tawny.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}