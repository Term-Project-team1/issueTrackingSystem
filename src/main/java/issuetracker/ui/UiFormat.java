package issuetracker.ui;

import issuetracker.domain.account.Account;
import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.project.Project;

import java.time.format.DateTimeFormatter;

public final class UiFormat {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UiFormat() {
    }

    public static String account(Account account) {
        return account == null ? "-" : account.getUsername() + " (" + account.getRole() + ")";
    }

    public static String username(Account account) {
        return account == null || account.getUsername() == null ? "-" : account.getUsername();
    }

    public static String project(Project project) {
        return project == null ? "-" : project.getName();
    }

    public static String projectWithId(Project project) {
        return project == null ? "No Project" : "#" + project.getId() + " " + project.getName();
    }

    public static String display(Object value) {
        if (value instanceof Account account) {
            return account(account);
        }
        if (value instanceof Project project) {
            return projectWithId(project);
        }
        return value == null ? "" : String.valueOf(value);
    }

    public static String issueDate(Issue issue) {
        return issue == null || issue.getReportedDate() == null ? "-" : issue.getReportedDate().format(DATE_TIME);
    }

    public static String comment(Comment comment) {
        String author = comment.getAuthor() == null ? "-" : comment.getAuthor().getUsername();
        return author + ": " + comment.getContent();
    }
}
