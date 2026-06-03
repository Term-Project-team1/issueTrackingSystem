package issuetracker.repository.recommendation;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;

import java.util.List;
import java.util.Map;

public interface RecommendationRepository {

    List<Account> findDevelopers();

    Map<Long, Long> countFixedIssuesByDeveloper(Long projectId);

    List<Issue> findSolvedIssues(Long projectId);
}