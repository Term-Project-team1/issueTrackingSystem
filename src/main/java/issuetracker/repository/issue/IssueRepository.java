package issuetracker.repository.issue;

import issuetracker.model.IssueFilter;
import java.util.List;
import java.util.Optional;

public interface IssueRepository {
    Optional<Issue1> findById(int issueId);
    List<Issue1> findByFilter(IssueFilter filter);
}