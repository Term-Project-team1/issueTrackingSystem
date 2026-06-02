package issuetracker.controller;

import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;
import issuetracker.service.comment.CommentService;

import java.util.List;

public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    public Comment addComment(Long issueId, Long authorId, String content) {
        return commentService.addComment(issueId, authorId, content);
    }

    public Issue viewIssueDetail(Long issueId) {
        return commentService.viewIssueDetail(issueId);
    }

    public List<Comment> findCommentsByIssue(Long issueId) {
        return commentService.findCommentsByIssue(issueId);
    }
}

