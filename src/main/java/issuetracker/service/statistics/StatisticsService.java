package issuetracker.service.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {

    private final StatisticsQuery statisticsQuery;

    public StatisticsService(StatisticsQuery statisticsQuery) {
        this.statisticsQuery = statisticsQuery;
    }

    public Map<IssueStatus, Long> countIssuesByStatus(Long projectId) {
        Map<IssueStatus, Long> raw = statisticsQuery.countByStatus(projectId);
        Map<IssueStatus, Long> result = new EnumMap<>(IssueStatus.class);
        for (IssueStatus status : IssueStatus.values()) {
            result.put(status, raw.getOrDefault(status, 0L));
        }
        return result;
    }

    public Map<Priority, Long> countIssuesByPriority(Long projectId) {
        Map<Priority, Long> raw = statisticsQuery.countByPriority(projectId);
        Map<Priority, Long> result = new EnumMap<>(Priority.class);
        for (Priority priority : Priority.values()) {
            result.put(priority, raw.getOrDefault(priority, 0L));
        }
        return result;
    }

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
