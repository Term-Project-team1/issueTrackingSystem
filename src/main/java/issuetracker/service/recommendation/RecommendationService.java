package issuetracker.service.recommendation;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import java.util.List;

public interface RecommendationService {

    List<Account> recommendAssignees(Issue issue, int limit);
}
