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

    public IssueServiceImpl() {
        this.issueRepository = new IssueRepository();
    }

    @Override
    public Issue createIssue(Project project,
                             Account reporter,
                             String title,
                             String description,
                             Priority priority) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("이슈 제목은 비어 있을 수 없습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이슈입니다."));
    }

    @Override
    public void assignIssue(Long issueId, Account assignee, Account pl) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.NEW) {
            throw new IllegalStateException("NEW 상태에서만 담당자를 지정할 수 있습니다.");
        }

        issue.setAssignee(assignee);
        issue.setStatus(IssueStatus.ASSIGNED);

        issueRepository.update(issue);
    }

    @Override
    public void markFixed(Long issueId, Account fixer) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.ASSIGNED) {
            throw new IllegalStateException("ASSIGNED 상태에서만 FIXED로 변경할 수 있습니다.");
        }

        issue.setFixer(fixer);
        issue.setStatus(IssueStatus.FIXED);

        issueRepository.update(issue);
    }

    @Override
    public void resolveIssue(Long issueId, Account tester) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.FIXED) {
            throw new IllegalStateException("FIXED 상태에서만 RESOLVED로 변경할 수 있습니다.");
        }

        issue.setStatus(IssueStatus.RESOLVED);

        issueRepository.update(issue);
    }

    @Override
    public void closeIssue(Long issueId, Account pl) {
        Issue issue = viewIssue(issueId);

        if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new IllegalStateException("RESOLVED 상태에서만 CLOSED로 변경할 수 있습니다.");
        }

        issue.setStatus(IssueStatus.CLOSED);

        issueRepository.update(issue);
    }
}