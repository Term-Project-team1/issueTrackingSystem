package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;
import issuetracker.service.search.SearchService.IssueFilter;

import java.util.List;
import java.util.Optional;

public interface IssueRepository {

    Issue save(Issue issue);

    Optional<Issue> findById(Long issueId);

    Issue update(Issue issue);

    List<Issue> findByFilter(IssueFilter filter);
}