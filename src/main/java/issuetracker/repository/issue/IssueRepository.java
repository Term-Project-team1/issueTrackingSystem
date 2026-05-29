package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;
import issuetracker.service.search.SearchService.IssueFilter;
import java.util.List;
import java.util.Optional;

public interface IssueRepository {
    Optional<Issue> findById(Long issueId);
    List<Issue> findByFilter(IssueFilter filter);
}