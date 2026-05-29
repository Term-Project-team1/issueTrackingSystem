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
}