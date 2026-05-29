package issuetracker.service.search;

import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.repository.issue.IssueRepository;
import java.util.List;

public class SearchService {

    private final IssueRepository issueRepo;

    public SearchService(IssueRepository issueRepo) {
        this.issueRepo = issueRepo;
    }

    public List<Issue> searchByStatus(IssueStatus status) {
        return issueRepo.findByFilter(new IssueFilter().status(status));
    }

    public List<Issue> searchByAssignee(Long assigneeId) {
        return issueRepo.findByFilter(new IssueFilter().assigneeId(assigneeId));
    }

    public List<Issue> searchByReporter(Long reporterId) {
        return issueRepo.findByFilter(new IssueFilter().reporterId(reporterId));
    }

    public List<Issue> searchByKeyword(String keyword) {
        return issueRepo.findByFilter(new IssueFilter().keyword(keyword));
    }

    public List<Issue> searchByFilter(IssueFilter filter) {
        return issueRepo.findByFilter(filter);
    }

    public static class IssueFilter {
        public IssueStatus status;
        public Long assigneeId;
        public Long reporterId;
        public String keyword;

        public IssueFilter status(IssueStatus status) { this.status = status; return this; }
        public IssueFilter assigneeId(Long id) { this.assigneeId = id; return this; }
        public IssueFilter reporterId(Long id) { this.reporterId = id; return this; }
        public IssueFilter keyword(String kw) { this.keyword = kw; return this; }
    }
}