package issuetracker.model;

public class IssueFilter {
    public String status;
    public Integer assigneeId;
    public Integer reporterId;
    public String keyword;

    // 빌더 스타일로 편하게 사용
    public IssueFilter status(String status) { this.status = status; return this; }
    public IssueFilter assigneeId(Integer id) { this.assigneeId = id; return this; }
    public IssueFilter reporterId(Integer id) { this.reporterId = id; return this; }
    public IssueFilter keyword(String kw) { this.keyword = kw; return this; }
}