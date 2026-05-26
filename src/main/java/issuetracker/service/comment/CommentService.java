package issuetracker.service.comment;

import issuetracker.repository.comment.CommentRepository;
import issuetracker.repository.issue.IssueRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentService {

    private final CommentRepository commentRepo;
    private final IssueRepository issueRepo;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ← Connection 아닌 Repository 인터페이스에 의존 (DIP)
    public CommentService(CommentRepository commentRepo, IssueRepository issueRepo) {
        this.commentRepo = commentRepo;
        this.issueRepo = issueRepo;
    }

    public Comment addComment(int issueId, int authorId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다.");
        }
        String createdDate = LocalDateTime.now().format(FORMATTER);
        Comment comment = new Comment(issueId, authorId, content, createdDate);
        return commentRepo.save(comment);
    }

    public Issue1 viewIssueDetail(int issueId) {
        return issueRepo.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다. id=" + issueId));
    }

    public List<Comment> findCommentsByIssue(int issueId) {
        return commentRepo.findByIssueId(issueId);
    }
}