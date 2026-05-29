package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryIssueRepository implements IssueRepository {

    private final Map<Long, Issue> store = new HashMap<>();

    @Override
    public Issue save(Issue issue) {
        store.put(issue.getId(), issue);
        return issue;
    }

    @Override
    public Optional<Issue> findById(Long issueId) {
        return Optional.ofNullable(store.get(issueId));
    }

    @Override
    public Issue update(Issue issue) {
        store.put(issue.getId(), issue);
        return issue;
    }

    @Override
    public List<Issue> findAll() {
        return new ArrayList<>(store.values());
    }
}