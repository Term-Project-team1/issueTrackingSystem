package issuetracker.service.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsQuery statisticsQuery;

    public StatisticsServiceImpl(StatisticsQuery statisticsQuery) {
        this.statisticsQuery = statisticsQuery;
    }

    @Override
    public Map<IssueStatus, Long> countIssuesByStatus(Long projectId) {
        Map<IssueStatus, Long> raw = statisticsQuery.countByStatus(projectId);
        Map<IssueStatus, Long> result = new EnumMap<>(IssueStatus.class);
        for (IssueStatus status : IssueStatus.values()) {
            result.put(status, raw.getOrDefault(status, 0L));
        }
        return result;
    }

    @Override
    public Map<Priority, Long> countIssuesByPriority(Long projectId) {
        Map<Priority, Long> raw = statisticsQuery.countByPriority(projectId);
        Map<Priority, Long> result = new EnumMap<>(Priority.class);
        for (Priority priority : Priority.values()) {
            result.put(priority, raw.getOrDefault(priority, 0L));
        }
        return result;
    }

    @Override
    public Map<LocalDate, Long> countIssuesByDay(Long projectId, YearMonth month) {
        Map<LocalDate, Long> raw = statisticsQuery.countByDay(projectId, month);
        Map<LocalDate, Long> result = new TreeMap<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            result.put(date, raw.getOrDefault(date, 0L));
        }
        return result;
    }

    @Override
    public Map<YearMonth, Long> countIssuesByMonth(Long projectId, int year) {
        Map<YearMonth, Long> raw = statisticsQuery.countByMonth(projectId, year);
        Map<YearMonth, Long> result = new TreeMap<>();
        for (Month m : Month.values()) {
            YearMonth ym = YearMonth.of(year, m);
            result.put(ym, raw.getOrDefault(ym, 0L));
        }
        return result;
    }

    @Override
    public Map<Account, Long> countFixedIssuesByDeveloper(Long projectId) {
        List<Account> developers = statisticsQuery.findDevelopers();
        Map<Long, Long> fixerCounts = statisticsQuery.countFixedIssuesByFixer(projectId);

        Map<Account, Long> result = new LinkedHashMap<>();
        for (Account developer : developers) {
            if (developer.getRole() != Role.DEV) {
                continue;
            }
            result.put(developer, fixerCounts.getOrDefault(developer.getId(), 0L));
        }
        return result;
    }
}
