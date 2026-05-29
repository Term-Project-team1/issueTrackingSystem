package issuetracker.service.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.repository.statistics.StatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
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

    private FakeStatisticsRepository query;
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        dev1 = new Account(11L, "dev1", Role.DEV);
        dev2 = new Account(12L, "dev2", Role.DEV);
        tester1 = new Account(21L, "tester1", Role.TESTER);

        query = new FakeStatisticsRepository();
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

    @Test
    @DisplayName("countIssuesByDay: 입력 월의 일별 카운트를 정확히 반환")
    void countIssuesByDay_입력월의일별카운트반환() {
        YearMonth month = YearMonth.of(2026, 5);
        query.dayCounts.put(LocalDate.of(2026, 5, 1), 3L);
        query.dayCounts.put(LocalDate.of(2026, 5, 15), 2L);
        query.dayCounts.put(LocalDate.of(2026, 5, 31), 1L);

        Map<LocalDate, Long> result = statisticsService.countIssuesByDay(PROJECT_ID, month);

        assertEquals(3L, result.get(LocalDate.of(2026, 5, 1)));
        assertEquals(2L, result.get(LocalDate.of(2026, 5, 15)));
        assertEquals(1L, result.get(LocalDate.of(2026, 5, 31)));
    }

    @Test
    @DisplayName("countIssuesByDay: 해당 월 모든 날짜를 0으로 채워 반환")
    void countIssuesByDay_해당월모든날짜를0으로채움() {
        YearMonth month = YearMonth.of(2026, 5);  // 31일
        query.dayCounts.put(LocalDate.of(2026, 5, 10), 5L);

        Map<LocalDate, Long> result = statisticsService.countIssuesByDay(PROJECT_ID, month);

        assertEquals(31, result.size());
        assertEquals(5L, result.get(LocalDate.of(2026, 5, 10)));
        assertEquals(0L, result.get(LocalDate.of(2026, 5, 1)));
        assertEquals(0L, result.get(LocalDate.of(2026, 5, 31)));
        assertTrue(result.containsKey(LocalDate.of(2026, 5, 20)));
    }

    @Test
    @DisplayName("countIssuesByMonth: 입력 연도의 월별 카운트를 정확히 반환")
    void countIssuesByMonth_입력연도의월별카운트반환() {
        int year = 2026;
        query.monthCounts.put(YearMonth.of(2026, 1), 4L);
        query.monthCounts.put(YearMonth.of(2026, 5), 7L);
        query.monthCounts.put(YearMonth.of(2026, 12), 2L);

        Map<YearMonth, Long> result = statisticsService.countIssuesByMonth(PROJECT_ID, year);

        assertEquals(4L, result.get(YearMonth.of(2026, 1)));
        assertEquals(7L, result.get(YearMonth.of(2026, 5)));
        assertEquals(2L, result.get(YearMonth.of(2026, 12)));
    }

    @Test
    @DisplayName("countIssuesByMonth: 12개월 모두 0으로 채워 반환")
    void countIssuesByMonth_12개월모두0으로채움() {
        int year = 2026;
        query.monthCounts.put(YearMonth.of(2026, 6), 3L);

        Map<YearMonth, Long> result = statisticsService.countIssuesByMonth(PROJECT_ID, year);

        assertEquals(12, result.size());
        assertEquals(3L, result.get(YearMonth.of(2026, 6)));
        assertEquals(0L, result.get(YearMonth.of(2026, 1)));
        assertEquals(0L, result.get(YearMonth.of(2026, 12)));
    }

    private static class FakeStatisticsRepository implements StatisticsRepository {

        final List<Account> developers = new ArrayList<>();
        final Map<IssueStatus, Long> statusCounts = new EnumMap<>(IssueStatus.class);
        final Map<Priority, Long> priorityCounts = new EnumMap<>(Priority.class);
        final Map<Long, Long> fixerCounts = new HashMap<>();
        final Map<LocalDate, Long> dayCounts = new HashMap<>();
        final Map<YearMonth, Long> monthCounts = new HashMap<>();

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

        @Override
        public Map<LocalDate, Long> countByDay(Long projectId, YearMonth month) {
            return new HashMap<>(dayCounts);
        }

        @Override
        public Map<YearMonth, Long> countByMonth(Long projectId, int year) {
            return new HashMap<>(monthCounts);
        }
    }
}
