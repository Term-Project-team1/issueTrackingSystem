package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IssueRepository {

    private final Map<Long, Issue> store = new HashMap<>();

    public Issue save(Issue issue) {
        store.put(issue.getId(), issue);
        return issue;
    }

    public Optional<Issue> findById(Long issueId) {
        return Optional.ofNullable(store.get(issueId));
    }

    public Issue update(Issue issue) {
        store.put(issue.getId(), issue);
        return issue;
    }
}