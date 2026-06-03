package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;
import issuetracker.service.search.SearchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public List<Issue> findByFilter(SearchService.IssueFilter filter) {
        return store.values().stream()
                .filter(i -> filter.status == null || i.getStatus() == filter.status)
                .filter(i -> filter.assigneeId == null ||
                        (i.getAssignee() != null && i.getAssignee().getId().equals(filter.assigneeId)))
                .filter(i -> filter.reporterId == null ||
                        i.getReporter().getId().equals(filter.reporterId))
                .filter(i -> filter.keyword == null ||
                        i.getTitle().contains(filter.keyword) ||
                        (i.getDescription() != null && i.getDescription().contains(filter.keyword)))
                .collect(Collectors.toList());
    }
}