package issuetracker.service.search;

import issuetracker.model.IssueFilter;
import issuetracker.repository.issue.IssueRepository;

import java.util.List;

public class SearchService {

    private final IssueRepository issueRepo;

    // ← Connection 아닌 Repository 인터페이스에 의존 (DIP)
    public SearchService(IssueRepository issueRepo) {
        this.issueRepo = issueRepo;
    }

    public List<Issue1> searchByStatus(String status) {
        return issueRepo.findByFilter(new IssueFilter().status(status));
    }

    public List<Issue1> searchByAssignee(int assigneeId) {
        return issueRepo.findByFilter(new IssueFilter().assigneeId(assigneeId));
    }

    public List<Issue1> searchByReporter(int reporterId) {
        return issueRepo.findByFilter(new IssueFilter().reporterId(reporterId));
    }

    public List<Issue1> searchByKeyword(String keyword) {
        return issueRepo.findByFilter(new IssueFilter().keyword(keyword));
    }

    public List<Issue1> searchByFilter(IssueFilter filter) {
        return issueRepo.findByFilter(filter);
    }
}