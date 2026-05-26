package issuetracker.model;

import issuetracker.domain.issue.IssueStatus;

public class IssueFilter {
    public IssueStatus status;
    public Long assigneeId;
    public Long reporterId;
    public String keyword;

    public IssueFilter status(IssueStatus status) { this.status = status; return this; }
    public IssueFilter assigneeId(Long id) { this.assigneeId = id; return this; }
    public IssueFilter reporterId(Long id) { this.reporterId = id; return this; }
    public IssueFilter keyword(String kw) { this.keyword = kw; return this; }
}