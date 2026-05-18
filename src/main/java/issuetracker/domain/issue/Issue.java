package issuetracker.domain.issue;

import issuetracker.domain.account.Account;
import issuetracker.domain.project.Project;
import java.time.LocalDateTime;

public class Issue {

    private Long id;

    private Project project;

    private String title;

    private String description;

    private IssueStatus status;

    private Priority priority;

    private Account reporter;

    private Account assignee;

    private Account fixer;

    private LocalDateTime reportedDate;

}