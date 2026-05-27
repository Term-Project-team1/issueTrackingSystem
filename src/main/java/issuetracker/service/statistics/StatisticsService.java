package issuetracker.service.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public interface StatisticsService {

    Map<IssueStatus, Long> countIssuesByStatus(Long projectId);

    Map<Priority, Long> countIssuesByPriority(Long projectId);

    Map<Account, Long> countFixedIssuesByDeveloper(Long projectId);

    Map<LocalDate, Long> countIssuesByDay(Long projectId, YearMonth month);

    Map<YearMonth, Long> countIssuesByMonth(Long projectId, int year);
}
