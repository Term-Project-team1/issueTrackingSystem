package issuetracker.service.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticsServiceTest {

    private static final Long PROJECT_ID = 1L;

    private Account dev1;
    private Account dev2;
    private Account tester1;

    private FakeStatisticsQuery query;
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        dev1 = new Account(11L, "dev1", Role.DEV);
        dev2 = new Account(12L, "dev2", Role.DEV);
        tester1 = new Account(21L, "tester1", Role.TESTER);

        query = new FakeStatisticsQuery();
        statisticsService = new StatisticsServiceImpl(query);
    }

    @Test
    @DisplayName("countIssuesByStatus: 상태별 개수 집계")
    void countIssuesByStatus_상태별개수집계() {
        query.statusCounts.put(IssueStatus.NEW, 3L);
        query.statusCounts.put(IssueStatus.ASSIGNED, 2L);
        query.statusCounts.put(IssueStatus.FIXED, 1L);

        Map<IssueStatus, Long> result = statisticsService.countIssuesByStatus(PROJECT_ID);

        assertEquals(3L, result.get(IssueStatus.NEW));
        assertEquals(2L, result.get(IssueStatus.ASSIGNED));
        assertEquals(1L, result.get(IssueStatus.FIXED));
        assertEquals(0L, result.get(IssueStatus.RESOLVED));
        assertEquals(0L, result.get(IssueStatus.CLOSED));
        assertEquals(0L, result.get(IssueStatus.REOPENED));
    }

    @Test
    @DisplayName("countIssuesByPriority: 우선순위별 개수 집계")
    void countIssuesByPriority_우선순위별개수집계() {
        query.priorityCounts.put(Priority.BLOCKER, 1L);
        query.priorityCounts.put(Priority.CRITICAL, 2L);
        query.priorityCounts.put(Priority.MAJOR, 4L);

        Map<Priority, Long> result = statisticsService.countIssuesByPriority(PROJECT_ID);

        assertEquals(1L, result.get(Priority.BLOCKER));
        assertEquals(2L, result.get(Priority.CRITICAL));
        assertEquals(4L, result.get(Priority.MAJOR));
        assertEquals(0L, result.get(Priority.MINOR));
        assertEquals(0L, result.get(Priority.TRIVIAL));
    }

    @Test
    @DisplayName("countFixedIssuesByDeveloper: 개발자별 FIXED 개수 집계")
    void countFixedIssuesByDeveloper_개발자별_FIXED개수집계() {
        query.developers.addAll(List.of(dev1, dev2, tester1));
        query.fixerCounts.put(dev1.getId(), 4L);
        query.fixerCounts.put(dev2.getId(), 2L);
        query.fixerCounts.put(tester1.getId(), 99L);

        Map<Account, Long> result = statisticsService.countFixedIssuesByDeveloper(PROJECT_ID);

        assertEquals(2, result.size());
        assertEquals(4L, result.get(dev1));
        assertEquals(2L, result.get(dev2));
        assertFalse(result.containsKey(tester1));
    }

    @Test
    @DisplayName("statistics: 이슈가 없으면 0 또는 빈 결과 반환")
    void statistics_이슈가없으면_0또는_빈결과반환() {
        query.developers.addAll(List.of(dev1, dev2));

        Map<IssueStatus, Long> statusResult = statisticsService.countIssuesByStatus(PROJECT_ID);
        Map<Priority, Long> priorityResult = statisticsService.countIssuesByPriority(PROJECT_ID);
        Map<Account, Long> developerResult = statisticsService.countFixedIssuesByDeveloper(PROJECT_ID);

        assertEquals(IssueStatus.values().length, statusResult.size());
        assertTrue(statusResult.values().stream().allMatch(v -> v == 0L));

        assertEquals(Priority.values().length, priorityResult.size());
        assertTrue(priorityResult.values().stream().allMatch(v -> v == 0L));

        assertEquals(2, developerResult.size());
        assertTrue(developerResult.values().stream().allMatch(v -> v == 0L));
    }

    private static class FakeStatisticsQuery implements StatisticsQuery {

        final List<Account> developers = new ArrayList<>();
        final Map<IssueStatus, Long> statusCounts = new EnumMap<>(IssueStatus.class);
        final Map<Priority, Long> priorityCounts = new EnumMap<>(Priority.class);
        final Map<Long, Long> fixerCounts = new HashMap<>();

        @Override
        public Map<IssueStatus, Long> countByStatus(Long projectId) {
            return new EnumMap<>(statusCounts.isEmpty()
                    ? new EnumMap<>(IssueStatus.class)
                    : statusCounts);
        }

        @Override
        public Map<Priority, Long> countByPriority(Long projectId) {
            return new EnumMap<>(priorityCounts.isEmpty()
                    ? new EnumMap<>(Priority.class)
                    : priorityCounts);
        }

        @Override
        public Map<Long, Long> countFixedIssuesByFixer(Long projectId) {
            return new HashMap<>(fixerCounts);
        }

        @Override
        public List<Account> findDevelopers() {
            return List.copyOf(developers);
        }
    }
}
