package issuetracker.controller;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.service.issue.IssueService;

public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    public Issue createIssue(Project project, Account reporter, String title, String description, Priority priority) {
        return issueService.createIssue(project, reporter, title, description, priority);
    }

    public Issue viewIssue(Long issueId) {
        return issueService.viewIssue(issueId);
    }

    public void updateIssue(Long issueId, String title, String description, Priority priority) {
        issueService.updateIssue(issueId, title, description, priority);
    }

    public void assignIssue(Long issueId, Account assignee, Account pl) {
        issueService.assignIssue(issueId, assignee, pl);
    }

    public void markFixed(Long issueId, Account fixer) {
        issueService.markFixed(issueId, fixer);
    }

    public void resolveIssue(Long issueId, Account tester) {
        issueService.resolveIssue(issueId, tester);
    }

    public void closeIssue(Long issueId, Account pl) {
        issueService.closeIssue(issueId, pl);
    }

    public void reopenIssue(Long issueId, Account user) {
        issueService.reopenIssue(issueId, user);
    }
}
