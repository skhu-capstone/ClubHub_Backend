package com.skhu.skhucapstone.projectrecruitment.service;

import com.skhu.skhucapstone.common.config.CacheConfig;
import com.skhu.skhucapstone.common.exception.CustomException;
import com.skhu.skhucapstone.common.exception.ErrorCode;
import com.skhu.skhucapstone.common.file.ImageUploadService;
import com.skhu.skhucapstone.projectrecruitment.dto.req.ProjectRecruitmentUpdateReq;
import com.skhu.skhucapstone.projectrecruitment.dto.res.ProjectRecruitmentDetailRes;
import com.skhu.skhucapstone.projectrecruitment.entity.ProjectRecruitment;
import com.skhu.skhucapstone.projectrecruitment.repository.ProjectRecruitmentRepository;
import com.skhu.skhucapstone.user.entity.User;
import com.skhu.skhucapstone.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testcontainers로 띄운 실제 Redis를 통해 캐시 저장·조회·무효화 동작을 검증한다.
// Repository는 Mock으로 대체해 DB 없이 캐시 계층만 검증한다.
@Testcontainers
@SpringBootTest(classes = ProjectRecruitmentCacheIntegrationTest.TestApp.class)
class ProjectRecruitmentCacheIntegrationTest {

    @Container
    @ServiceConnection
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Configuration
    @ImportAutoConfiguration({DataRedisAutoConfiguration.class, CacheAutoConfiguration.class})
    @Import({CacheConfig.class, ProjectRecruitmentCacheService.class, ProjectRecruitmentService.class})
    static class TestApp {
    }

    @MockitoBean
    private ProjectRecruitmentRepository projectRecruitmentRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @Autowired
    private ProjectRecruitmentService projectRecruitmentService;

    @Autowired
    private CacheManager cacheManager;

    // 모집글 작성자. 조회 시 권한 플래그 계산에도 쓰인다.
    private static final Long OWNER_ID = 1L;

    private final User writer = User.builder().userId(OWNER_ID).name("정다운").build();

    private ProjectRecruitment recruitment(Long id, String title) {
        return ProjectRecruitment.builder()
                .projectRecruitmentId(id)
                .title(title)
                .writerStack("Spring")
                .positions("백엔드")
                .content("내용")
                .deadline(LocalDate.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(writer)
                .build();
    }

    @BeforeEach
    void clearCache() {
        cacheManager.getCache(CacheConfig.PROJECT_RECRUITMENT_CACHE).clear();
        awaitEvicted(1L);
        awaitEvicted(2L);
    }

    // 이 스택에서는 캐시 put/evict/clear가 메서드 리턴 직후 약 1~2ms 늦게 Redis에 반영된다.
    // 반영 전에 다음 조회가 실행되면 결과가 타이밍에 따라 달라져 테스트가 간헐적으로 실패하므로,
    // 기대 상태가 실제로 보일 때까지 기다린 뒤 다음 단계를 진행한다.
    private void awaitCached(Long projectRecruitmentId) {
        awaitCacheState(projectRecruitmentId, true);
    }

    private void awaitEvicted(Long projectRecruitmentId) {
        awaitCacheState(projectRecruitmentId, false);
    }

    private void awaitCacheState(Long projectRecruitmentId, boolean expectPresent) {
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while ((cacheManager.getCache(CacheConfig.PROJECT_RECRUITMENT_CACHE).get(projectRecruitmentId) != null)
                != expectPresent) {
            if (System.nanoTime() > deadline) {
                throw new IllegalStateException("2초가 지나도 캐시가 기대 상태가 아님: key="
                        + projectRecruitmentId + ", expectPresent=" + expectPresent);
            }
            Thread.onSpinWait();
        }
    }

    @Test
    @DisplayName("권한 플래그는 캐시되지 않고 요청한 사용자에 따라 매번 계산된다")
    void permissionFlagsAreNotCached() {
        when(projectRecruitmentRepository.findById(1L))
                .thenReturn(Optional.of(recruitment(1L, "권한 확인")));

        // 작성자가 먼저 조회해 캐시를 채운다
        ProjectRecruitmentDetailRes byOwner = projectRecruitmentService.getRecruitment(1L, OWNER_ID);
        awaitCached(1L);

        // 같은 글을 다른 사용자와 비로그인 상태로 조회한다
        ProjectRecruitmentDetailRes byOther = projectRecruitmentService.getRecruitment(1L, 999L);
        ProjectRecruitmentDetailRes byAnonymous = projectRecruitmentService.getRecruitment(1L, null);

        assertThat(byOwner.canUpdate()).isTrue();
        assertThat(byOwner.canDelete()).isTrue();

        // 캐시에 작성자의 권한이 저장돼 그대로 재사용되면 안 된다
        assertThat(byOther.canUpdate()).isFalse();
        assertThat(byOther.canDelete()).isFalse();
        assertThat(byAnonymous.canUpdate()).isFalse();
        assertThat(byAnonymous.canDelete()).isFalse();

        // 캐시는 정상 동작해야 한다 (DB 조회는 한 번뿐)
        verify(projectRecruitmentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("첫 번째 조회에서는 Repository가 호출된다")
    void firstReadHitsRepository() {
        when(projectRecruitmentRepository.findById(1L))
                .thenReturn(Optional.of(recruitment(1L, "첫 조회")));

        ProjectRecruitmentDetailRes res = projectRecruitmentService.getRecruitment(1L, OWNER_ID);

        assertThat(res.title()).isEqualTo("첫 조회");
        verify(projectRecruitmentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("동일 프로젝트를 다시 조회하면 캐시가 사용되어 Repository가 호출되지 않는다")
    void secondReadUsesCache() {
        when(projectRecruitmentRepository.findById(1L))
                .thenReturn(Optional.of(recruitment(1L, "캐시 대상")));

        projectRecruitmentService.getRecruitment(1L, OWNER_ID);
        awaitCached(1L); // 캐시 저장이 Redis에 반영된 뒤 재조회
        ProjectRecruitmentDetailRes second = projectRecruitmentService.getRecruitment(1L, OWNER_ID);

        assertThat(second.title()).isEqualTo("캐시 대상");
        assertThat(second.dDay()).isNotNull(); // dDay는 캐시 히트 시에도 계산되어 채워진다
        verify(projectRecruitmentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("프로젝트 수정 후에는 캐시가 제거되어 다시 Repository를 조회한다")
    void updateEvictsCache() {
        ProjectRecruitment entity = recruitment(1L, "수정 전");
        when(projectRecruitmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        projectRecruitmentService.getRecruitment(1L, OWNER_ID); // 캐시 저장
        awaitCached(1L); // 저장이 반영된 뒤 수정 (저장과 무효화의 순서 역전 방지)
        projectRecruitmentService.updateRecruitment(1L, writer.getUserId(), updateReq("수정 후"));
        awaitEvicted(1L); // 무효화가 Redis에 반영된 뒤 재조회
        projectRecruitmentService.getRecruitment(1L, OWNER_ID); // 캐시가 지워졌으므로 다시 DB 조회

        // 첫 조회 1회 + 수정 시 1회 + 무효화 후 재조회 1회 = 3회
        verify(projectRecruitmentRepository, times(3)).findById(1L);
    }

    @Test
    @DisplayName("수정 후 다시 조회하면 변경된 데이터가 반환된다")
    void readAfterUpdateReturnsFreshData() {
        ProjectRecruitment entity = recruitment(1L, "수정 전");
        when(projectRecruitmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        ProjectRecruitmentDetailRes before = projectRecruitmentService.getRecruitment(1L, OWNER_ID);
        awaitCached(1L); // 저장이 반영된 뒤 수정 (저장과 무효화의 순서 역전 방지)
        projectRecruitmentService.updateRecruitment(1L, writer.getUserId(), updateReq("수정 후"));
        awaitEvicted(1L); // 무효화가 Redis에 반영된 뒤 재조회
        ProjectRecruitmentDetailRes after = projectRecruitmentService.getRecruitment(1L, OWNER_ID);

        assertThat(before.title()).isEqualTo("수정 전");
        assertThat(after.title()).isEqualTo("수정 후");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트는 캐싱되지 않는다")
    void missingProjectIsNotCached() {
        when(projectRecruitmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectRecruitmentService.getRecruitment(999L, OWNER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_RECRUITMENT_NOT_FOUND);
        assertThatThrownBy(() -> projectRecruitmentService.getRecruitment(999L, OWNER_ID))
                .isInstanceOf(CustomException.class);

        // 예외가 캐싱되지 않고 매번 Repository를 조회해야 한다
        verify(projectRecruitmentRepository, times(2)).findById(999L);
    }

    @Test
    @DisplayName("서로 다른 프로젝트 ID는 서로 다른 캐시 값을 사용한다")
    void differentIdsUseDifferentCacheEntries() {
        when(projectRecruitmentRepository.findById(1L))
                .thenReturn(Optional.of(recruitment(1L, "프로젝트1")));
        when(projectRecruitmentRepository.findById(2L))
                .thenReturn(Optional.of(recruitment(2L, "프로젝트2")));

        projectRecruitmentService.getRecruitment(1L, OWNER_ID);
        ProjectRecruitmentDetailRes res2 = projectRecruitmentService.getRecruitment(2L, OWNER_ID);
        awaitCached(1L); // 캐시 저장이 Redis에 반영된 뒤 재조회
        ProjectRecruitmentDetailRes res1Again = projectRecruitmentService.getRecruitment(1L, OWNER_ID);

        assertThat(res1Again.title()).isEqualTo("프로젝트1");
        assertThat(res2.title()).isEqualTo("프로젝트2");
        verify(projectRecruitmentRepository, times(1)).findById(1L);
        verify(projectRecruitmentRepository, times(1)).findById(2L);
    }

    private ProjectRecruitmentUpdateReq updateReq(String title) {
        return new ProjectRecruitmentUpdateReq(
                title, null, "Spring", "백엔드", "내용", LocalDate.now().plusDays(7));
    }
}
