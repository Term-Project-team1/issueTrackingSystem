package issuetracker.service.search;

import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.repository.issue.IssueRepository;
import java.util.List;

public class SearchServiceImpl implements SearchService {

    private final IssueRepository issueRepo;

    public SearchServiceImpl(IssueRepository issueRepo) {
        this.issueRepo = issueRepo;
    }

    @Override
    public List<Issue> searchByStatus(IssueStatus status) {
        return issueRepo.findByFilter(new IssueFilter().status(status));
    }

    @Override
    public List<Issue> searchByAssignee(Long assigneeId) {
        return issueRepo.findByFilter(new IssueFilter().assigneeId(assigneeId));
    }

    @Override
    public List<Issue> searchByReporter(Long reporterId) {
        return issueRepo.findByFilter(new IssueFilter().reporterId(reporterId));
    }

    @Override
    public List<Issue> searchByKeyword(String keyword) {
        return issueRepo.findByFilter(new IssueFilter().keyword(keyword));
    }

    @Override
    public List<Issue> searchByFilter(IssueFilter filter) {
        return issueRepo.findByFilter(filter);
    }
}