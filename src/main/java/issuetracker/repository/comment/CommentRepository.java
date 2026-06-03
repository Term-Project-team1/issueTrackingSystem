package issuetracker.repository.comment;

import issuetracker.domain.comment.Comment;
import java.util.List;

public interface CommentRepository {
    Comment save(Comment comment);
    List<Comment> findByIssueId(Long issueId);
}


