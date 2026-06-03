package issuetracker.controller;

import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.service.search.SearchService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    public List<Issue> searchByStatus(IssueStatus status) {
        return safe(() -> searchService.searchByStatus(status));
    }

    public List<Issue> searchByAssignee(Long assigneeId) {
        return safe(() -> searchService.searchByAssignee(assigneeId));
    }

    public List<Issue> searchByReporter(Long reporterId) {
        return safe(() -> searchService.searchByReporter(reporterId));
    }

    public List<Issue> searchByKeyword(String keyword) {
        return safe(() -> searchService.searchByKeyword(keyword));
    }

    public List<Issue> searchByFilter(SearchService.IssueFilter filter) {
        return safe(() -> searchService.searchByFilter(filter));
    }

    public List<Issue> searchAll() {
        return safe(() -> searchService.searchByFilter(new SearchService.IssueFilter()));
    }

    public List<Issue> searchIssues(IssueStatus status, Long reporterId, Long assigneeId, String keyword) {
        return safe(() -> searchService.searchByFilter(new SearchService.IssueFilter()
                .status(status)
                .reporterId(reporterId)
                .assigneeId(assigneeId)
                .keyword(keyword)));
    }

    public List<Issue> searchIssues(Long projectId, IssueStatus status, Long reporterId, Long assigneeId, String keyword) {
        return searchIssues(status, reporterId, assigneeId, keyword).stream()
                .filter(issue -> projectId == null || Objects.equals(issue.getProject().getId(), projectId))
                .toList();
    }

    private List<Issue> safe(java.util.function.Supplier<List<Issue>> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }
}
