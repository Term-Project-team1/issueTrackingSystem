package issuetracker.domain.comment;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private Issue issue;
    private Account author;
    private String content;
    private LocalDateTime createdDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Issue getIssue() { return issue; }
    public void setIssue(Issue issue) { this.issue = issue; }
    public Account getAuthor() { return author; }
    public void setAuthor(Account author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}