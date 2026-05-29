package issuetracker.repository.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface StatisticsRepository {

    Map<IssueStatus, Long> countByStatus(Long projectId);

    Map<Priority, Long> countByPriority(Long projectId);

    Map<Long, Long> countFixedIssuesByFixer(Long projectId);

    List<Account> findDevelopers();

    Map<LocalDate, Long> countByDay(Long projectId, YearMonth month);

    Map<YearMonth, Long> countByMonth(Long projectId, int year);
}
