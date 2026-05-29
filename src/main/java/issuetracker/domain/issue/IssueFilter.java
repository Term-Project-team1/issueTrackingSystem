package issuetracker.domain.issue;

/*
이슈 검색 조건을 하나로 묶는 클래스
조건마다 메서드 따로 만들필요없이 어떤조건의 조합이던 하나의 메서드로 가능
ex]
IssueFilter filter = new IssueFilter()
    .status(IssueStatus.NEW)
    .assigneeId(2L)
    .keyword("로그인");

searchService.searchByFilter(filter);
*/

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