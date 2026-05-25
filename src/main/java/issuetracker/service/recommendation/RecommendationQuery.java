package issuetracker.service.recommendation;

import issuetracker.domain.account.Account;
import java.util.List;
import java.util.Map;

public interface RecommendationQuery {

    List<Account> findDevelopers();

    Map<Long, Long> countFixedIssuesByDeveloper(Long projectId);
}
