package issuetracker.ui;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.project.Project;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UiSession {
    private final List<Runnable> listeners = new ArrayList<>();
    private final List<Issue> recentCreatedIssues = new ArrayList<>();
    private Project selectedProject;
    private Account currentUser;

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void selectProject(Project project) {
        this.selectedProject = project;
        listeners.forEach(Runnable::run);
    }

    public Account getCurrentUser() {
        return currentUser;
    }

    public void selectCurrentUser(Account currentUser) {
        this.currentUser = currentUser;
        listeners.forEach(Runnable::run);
    }

    public void addProjectListener(Runnable listener) {
        listeners.add(listener);
    }

    public void rememberCreatedIssue(Issue issue) {
        if (issue == null) {
            return;
        }
        recentCreatedIssues.removeIf(existing -> Objects.equals(existing.getId(), issue.getId()));
        recentCreatedIssues.add(0, issue);
    }

    public List<Issue> mergeVisibleIssues(List<Issue> issues,
                                          Long projectId,
                                          IssueStatus status,
                                          Long reporterId,
                                          Long assigneeId,
                                          String keyword) {
        List<Issue> merged = new ArrayList<>(issues);
        for (Issue issue : recentCreatedIssues) {
            if (merged.stream().noneMatch(existing -> Objects.equals(existing.getId(), issue.getId()))
                    && matches(issue, projectId, status, reporterId, assigneeId, keyword)) {
                merged.add(0, issue);
            }
        }
        return merged;
    }

    private boolean matches(Issue issue,
                            Long projectId,
                            IssueStatus status,
                            Long reporterId,
                            Long assigneeId,
                            String keyword) {
        if (projectId != null && (issue.getProject() == null || !Objects.equals(issue.getProject().getId(), projectId))) {
            return false;
        }
        if (status != null && issue.getStatus() != status) {
            return false;
        }
        if (reporterId != null && (issue.getReporter() == null || !Objects.equals(issue.getReporter().getId(), reporterId))) {
            return false;
        }
        if (assigneeId != null && (issue.getAssignee() == null || !Objects.equals(issue.getAssignee().getId(), assigneeId))) {
            return false;
        }
        if (keyword != null && !keyword.isBlank()) {
            String text = (Objects.toString(issue.getTitle(), "") + " " + Objects.toString(issue.getDescription(), "")).toLowerCase();
            return text.contains(keyword.toLowerCase());
        }
        return true;
    }
}