package issuetracker.domain.issue;

import issuetracker.domain.account.Account;
import issuetracker.domain.project.Project;
import java.time.LocalDateTime;
import java.util.Objects;

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

    public Issue() {
    }

    public Issue(Long id,
                 Project project,
                 String title,
                 String description,
                 IssueStatus status,
                 Priority priority,
                 Account reporter,
                 Account assignee,
                 Account fixer,
                 LocalDateTime reportedDate) {
        this.id = id;
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.reporter = reporter;
        this.assignee = assignee;
        this.fixer = fixer;
        this.reportedDate = reportedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Account getReporter() {
        return reporter;
    }

    public void setReporter(Account reporter) {
        this.reporter = reporter;
    }

    public Account getAssignee() {
        return assignee;
    }

    public void setAssignee(Account assignee) {
        this.assignee = assignee;
    }

    public Account getFixer() {
        return fixer;
    }

    public void setFixer(Account fixer) {
        this.fixer = fixer;
    }

    public LocalDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Issue)) return false;
        Issue issue = (Issue) o;
        return Objects.equals(id, issue.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
