package issuetracker.service.search;

import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import java.util.List;

public interface SearchService {
    List<Issue> searchByStatus(IssueStatus status);
    List<Issue> searchByAssignee(Long assigneeId);
    List<Issue> searchByReporter(Long reporterId);
    List<Issue> searchByKeyword(String keyword);
    List<Issue> searchByFilter(IssueFilter filter);

    class IssueFilter {
        public IssueStatus status;
        public Long assigneeId;
        public Long reporterId;
        public String keyword;

        public IssueFilter status(IssueStatus status) { this.status = status; return this; }
        public IssueFilter assigneeId(Long id) { this.assigneeId = id; return this; }
        public IssueFilter reporterId(Long id) { this.reporterId = id; return this; }
        public IssueFilter keyword(String kw) { this.keyword = kw; return this; }
    }
}