package issuetracker.service.comment;

import issuetracker.domain.account.Account;
import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;
import issuetracker.repository.comment.CommentRepository;
import issuetracker.repository.comment.CommentRepositoryImpl;
import issuetracker.repository.issue.IssueRepository;
import issuetracker.repository.issue.IssueRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;

public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepo;
    private final IssueRepository issueRepo;

    public CommentServiceImpl() {
        this.commentRepo = new CommentRepositoryImpl(null);
        this.issueRepo = new IssueRepositoryImpl();
    }

    public CommentServiceImpl(CommentRepository commentRepo, IssueRepository issueRepo) {
        this.commentRepo = commentRepo;
        this.issueRepo = issueRepo;
    }

    @Override
    public Comment addComment(Long issueId, Long authorId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다.");
        }

        Issue issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다. id=" + issueId));

        Account author = new Account(authorId, null, null);

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedDate(LocalDateTime.now());

        return commentRepo.save(comment);
    }

    @Override
    public Issue viewIssueDetail(Long issueId) {
        return issueRepo.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다. id=" + issueId));
    }

    @Override
    public List<Comment> findCommentsByIssue(Long issueId) {
        return commentRepo.findByIssueId(issueId);
    }
}