package issuetracker.service.comment;

import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;

import java.util.List;

public interface CommentService {

    Comment addComment(Long issueId, Long authorId, String content);

    Issue viewIssueDetail(Long issueId);

    List<Comment> findCommentsByIssue(Long issueId);
}