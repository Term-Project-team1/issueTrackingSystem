package issuetracker.service.statistics;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.util.List;
import java.util.Map;

public interface StatisticsQuery {

    Map<IssueStatus, Long> countByStatus(Long projectId);

    Map<Priority, Long> countByPriority(Long projectId);

    Map<Long, Long> countFixedIssuesByFixer(Long projectId);

    List<Account> findDevelopers();
}
