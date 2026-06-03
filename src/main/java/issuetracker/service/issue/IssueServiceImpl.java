package issuetracker.service.issue;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.repository.issue.IssueRepository;
import issuetracker.repository.issue.IssueRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private Long sequence = 1L;

    public IssueServiceImpl() {
        this.issueRepository = new IssueRepositoryImpl();
    }

    public IssueServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public Issue createIssue(Project project,
                             Account reporter,
                             String title,
                             String description,
                             Priority priority) {

        validateTitle(title);

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
    public List<Issue> findAll() {
        return issueRepository.findAll();
    }

    @Override
    public void updateIssue(Long issueId,
                            String title,
                            String description,
                            Priority priority) {
        Issue issue = viewIssue(issueId);

        validateTitle(title);

        issue.setTitle(title);
        issue.setDescription(description);
        issue.setPriority(priority);

        issueRepository.update(issue);
    }

    @Override
    public void assignIssue(Long issueId, Account assignee, Account pl) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.NEW
                && issue.getStatus() != IssueStatus.REOPENED) {
            throw new IllegalStateException("Issue can only be assigned from NEW or REOPENED status.");
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

    @Override
    public void reopenIssue(Long issueId, Account user) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.RESOLVED
                && issue.getStatus() != IssueStatus.CLOSED) {
            throw new IllegalStateException("Issue can only be reopened from RESOLVED or CLOSED status.");
        }

        issue.setStatus(IssueStatus.REOPENED);

        issueRepository.update(issue);
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue title cannot be empty.");
        }
    }
}