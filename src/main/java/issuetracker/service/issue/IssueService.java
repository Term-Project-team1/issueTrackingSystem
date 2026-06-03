package issuetracker.service.issue;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import java.util.List;

public interface IssueService {

    Issue createIssue(Project project,
                      Account reporter,
                      String title,
                      String description,
                      Priority priority);

    Issue viewIssue(Long issueId);

    List<Issue> findAll();

    void updateIssue(Long issueId,
                     String title,
                     String description,
                     Priority priority);

    void assignIssue(Long issueId, Account assignee, Account pl);

    void markFixed(Long issueId, Account fixer);

    void resolveIssue(Long issueId, Account tester);

    void closeIssue(Long issueId, Account pl);

    void reopenIssue(Long issueId, Account user);
}