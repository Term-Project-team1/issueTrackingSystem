package issuetracker.repository.comment;
import java.util.List;

public interface CommentRepository {
    Comment save(Comment comment);
    List<Comment> findByIssueId(int issueId);
}