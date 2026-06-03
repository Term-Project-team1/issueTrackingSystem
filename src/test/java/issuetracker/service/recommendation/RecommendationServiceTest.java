package issuetracker.service.recommendation;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.repository.recommendation.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationServiceTest {

    private static final Long PROJECT_ID = 1L;

    private Project project;
    private Issue issue;

    private Account dev1;
    private Account dev2;
    private Account dev3;
    private Account dev4;
    private Account tester1;
    private Account pl1;
    private Account admin;

    private FakeRecommendationRepository query;
    private RecommendationService recommendationService;

    @BeforeEach // 각각의 @Test 메서드가 실행되기 직전마다 매번 새로 실행
    void setUp() {
        project = new Project(PROJECT_ID, "project1", LocalDateTime.now());
        issue = new Issue(
                100L, project, "title", "desc",
                IssueStatus.NEW, Priority.MAJOR,
                null, LocalDateTime.now()
        );

        dev1 = new Account(11L, "dev1", Role.DEV);
        dev2 = new Account(12L, "dev2", Role.DEV);
        dev3 = new Account(13L, "dev3", Role.DEV);
        dev4 = new Account(14L, "dev4", Role.DEV);
        tester1 = new Account(21L, "tester1", Role.TESTER);
        pl1 = new Account(31L, "PL1", Role.PL);
        admin = new Account(41L, "admin", Role.ADMIN);

        query = new FakeRecommendationRepository();
        recommendationService = new RecommendationServiceImpl(query);
    }

    @Test
    @DisplayName("recommendAssignees: FIXED 이력 기반 top3 추천")
    void recommendAssignees_FIXED이력기반_top3추천() {
        query.developers.addAll(List.of(dev1, dev2, dev3, dev4));
        query.fixedCounts.put(dev1.getId(), 1L); // 1건 해결
        query.fixedCounts.put(dev2.getId(), 5L); // 5건 해결
        query.fixedCounts.put(dev3.getId(), 3L);
        query.fixedCounts.put(dev4.getId(), 0L);

        List<Account> result = recommendationService.recommendAssignees(issue, 3);

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(a -> a.getRole() == Role.DEV));
        assertEquals(List.of(dev2, dev3, dev1), result); // 추천 순서 (해결 건수 순) 맞는지 확인
    }

    @Test
    @DisplayName("recommendAssignees: 이력이 없으면 DEV 목록 기반 추천") // 해결 이력 없을 때, 목록 dev 그대로 반환
    void recommendAssignees_이력이없으면_DEV목록기반추천() {
        query.developers.addAll(List.of(dev1, dev2, dev3));

        List<Account> result = recommendationService.recommendAssignees(issue, 3);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(a -> a.getRole() == Role.DEV));
        assertEquals(List.of(dev1, dev2, dev3), result);
    }

    @Test
    @DisplayName("recommendAssignees: 추천 결과는 limit 개수 이하")
    void recommendAssignees_추천결과가_limit개수이하인지() {
        query.developers.addAll(List.of(dev1, dev2, dev3, dev4));
        query.fixedCounts.put(dev1.getId(), 2L);
        query.fixedCounts.put(dev2.getId(), 4L);

        List<Account> result = recommendationService.recommendAssignees(issue, 3);

        assertTrue(result.size() <= 3);
    }

    @Test
    @DisplayName("recommendAssignees: TESTER나 PL은 추천 대상에서 제외") // 추천 대상은 dev이어야만.
    void recommendAssignees_TESTER나_PL은_추천대상에서_제외되는지() {
        query.developers.addAll(List.of(dev1, dev2, tester1, pl1, admin));
        query.fixedCounts.put(tester1.getId(), 99L);
        query.fixedCounts.put(pl1.getId(), 50L);
        query.fixedCounts.put(admin.getId(), 50L);

        List<Account> result = recommendationService.recommendAssignees(issue, 5);

        assertTrue(result.stream().noneMatch(a -> a.getRole() == Role.TESTER));
        assertTrue(result.stream().noneMatch(a -> a.getRole() == Role.PL));
        assertTrue(result.stream().noneMatch(a -> a.getRole() == Role.ADMIN));
        assertTrue(result.stream().allMatch(a -> a.getRole() == Role.DEV));
    }

    @Test
    @DisplayName("recommendAssignees: 유사한 해결 이슈의 fixer가 우선 추천된다")
    void recommendAssignees_유사도기반_fixer우선추천() {
        issue = new Issue(
                100L,
                project,
                "ui button color issue",
                "button layout is broken",
                IssueStatus.NEW,
                Priority.MAJOR,
                tester1,
                LocalDateTime.now()
        );

        query.developers.addAll(List.of(dev1, dev2, dev3));

        Issue solvedByDev2 = new Issue(
                200L,
                project,
                "login token error",
                "session token problem during login",
                IssueStatus.RESOLVED,
                Priority.MAJOR,
                tester1,
                LocalDateTime.now()
        );
        solvedByDev2.setFixer(dev2);

        Issue solvedByDev3 = new Issue(
                201L,
                project,
                "ui button color issue",
                "button layout is broken",
                IssueStatus.CLOSED,
                Priority.MINOR,
                tester1,
                LocalDateTime.now()
        );
        solvedByDev3.setFixer(dev3);

        query.solvedIssues.add(solvedByDev2);
        query.solvedIssues.add(solvedByDev3);

        List<Account> result = recommendationService.recommendAssignees(issue, 3);

        assertFalse(result.isEmpty());
        assertEquals(dev3, result.get(0));
    }

    private static class FakeRecommendationRepository implements RecommendationRepository {

        final List<Account> developers = new ArrayList<>();
        final Map<Long, Long> fixedCounts = new HashMap<>();
        final List<Issue> solvedIssues = new ArrayList<>();

        @Override
        public List<Account> findDevelopers() {
            List<Account> devsOnly = new ArrayList<>();
            for (Account account : developers) {
                if (account.getRole() == Role.DEV) {
                    devsOnly.add(account);
                }
            }
            return devsOnly;
        }

        @Override
        public Map<Long, Long> countFixedIssuesByDeveloper(Long projectId) {
            return new HashMap<>(fixedCounts);
        }

        @Override
        public List<Issue> findSolvedIssues(Long projectId) {
            return new ArrayList<>(solvedIssues);
        }
    }
}
