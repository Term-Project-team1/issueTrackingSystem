package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;
import issuetracker.service.search.SearchService.IssueFilter;
import java.util.Optional;

import java.util.List;

public interface IssueRepository {

    Issue save(Issue issue);

    Optional<Issue> findById(Long issueId);

    Issue update(Issue issue);

    List<Issue> findAll();

    List<Issue> findByFilter(IssueFilter filter);
}