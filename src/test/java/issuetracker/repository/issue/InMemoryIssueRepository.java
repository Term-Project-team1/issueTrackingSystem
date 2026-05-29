package issuetracker.repository.issue;

import issuetracker.domain.issue.Issue;
import issuetracker.service.search.SearchService.IssueFilter;

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
    public List<Issue> findByFilter(IssueFilter filter) {
        return store.values().stream()
                .filter(issue -> filter.status == null
                        || issue.getStatus() == filter.status)

                .filter(issue -> filter.assigneeId == null
                        || (issue.getAssignee() != null
                        && issue.getAssignee().getId().equals(filter.assigneeId)))

                .filter(issue -> filter.reporterId == null
                        || (issue.getReporter() != null
                        && issue.getReporter().getId().equals(filter.reporterId)))

                .filter(issue -> filter.keyword == null
                        || filter.keyword.isBlank()
                        || containsIgnoreCase(issue.getTitle(), filter.keyword)
                        || containsIgnoreCase(issue.getDescription(), filter.keyword))

                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }

        return text.toLowerCase().contains(keyword.toLowerCase());
    }
}