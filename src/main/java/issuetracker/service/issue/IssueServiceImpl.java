package issuetracker.service.issue;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.repository.issue.IssueRepository;

import java.time.LocalDateTime;

public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private Long sequence = 1L;

    public IssueServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public Issue createIssue(Project project,
                             Account reporter,
                             String title,
                             String description,
                             Priority priority) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue title cannot be empty.");
        }

        Long issueId = sequence++;

        Issue issue = new Issue(
                issueId,
                project,
                title,
                description,
                IssueStatus.NEW,
                priority,
                reporter,
                LocalDateTime.now()
        );

        return issueRepository.save(issue);
    }

    @Override
    public Issue viewIssue(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found."));
    }

    @Override
    public void updateIssue(Long issueId,
                            String title,
                            String description,
                            Priority priority) {
        Issue issue = viewIssue(issueId);

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue title cannot be empty.");
        }

        issue.setTitle(title);
        issue.setDescription(description);
        issue.setPriority(priority);

        issueRepository.update(issue);
    }

    @Override
    public void assignIssue(Long issueId, Account assignee, Account pl) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.NEW) {
            throw new IllegalStateException("Issue can only be assigned from NEW status.");
        }

        issue.setAssignee(assignee);
        issue.setStatus(IssueStatus.ASSIGNED);

        issueRepository.update(issue);
    }

    @Override
    public void markFixed(Long issueId, Account fixer) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.ASSIGNED) {
            throw new IllegalStateException("Issue can only be marked as FIXED from ASSIGNED status.");
        }

        issue.setFixer(fixer);
        issue.setStatus(IssueStatus.FIXED);

        issueRepository.update(issue);
    }

    @Override
    public void resolveIssue(Long issueId, Account tester) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.FIXED) {
            throw new IllegalStateException("Issue can only be resolved from FIXED status.");
        }

        issue.setStatus(IssueStatus.RESOLVED);

        issueRepository.update(issue);
    }

    @Override
    public void closeIssue(Long issueId, Account pl) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new IllegalStateException("Issue can only be closed from RESOLVED status.");
        }

        issue.setStatus(IssueStatus.CLOSED);

        issueRepository.update(issue);
    }
}