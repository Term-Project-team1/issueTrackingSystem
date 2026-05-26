package issuetracker.service.comment;

import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;
import issuetracker.repository.comment.CommentRepository;
import issuetracker.repository.issue.IssueRepository;

import java.time.LocalDateTime;
import java.util.List;

public class CommentService {

    private final CommentRepository commentRepo;
    private final IssueRepository issueRepo;

    public CommentService(CommentRepository commentRepo, IssueRepository issueRepo) {
        this.commentRepo = commentRepo;
        this.issueRepo = issueRepo;
    }

    public Comment addComment(Long issueId, Long authorId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 비어있을 수 없습니다.");
        }
        Issue issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다. id=" + issueId));

        // author는 Account 객체 필요 — id만 세팅한 껍데기로 저장
        issuetracker.domain.account.Account author =
                new issuetracker.domain.account.Account(authorId, null, null);

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedDate(LocalDateTime.now());

        return commentRepo.save(comment);
    }

    public Issue viewIssueDetail(Long issueId) {
        return issueRepo.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다. id=" + issueId));
    }

    public List<Comment> findCommentsByIssue(Long issueId) {
        return commentRepo.findByIssueId(issueId);
    }
}