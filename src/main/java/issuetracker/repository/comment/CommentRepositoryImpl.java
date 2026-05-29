package issuetracker.repository.comment;

import issuetracker.domain.account.Account;
import issuetracker.domain.comment.Comment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentRepositoryImpl implements CommentRepository {

    private final Connection connection;

    public CommentRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comment(issue_id, author_id, content, created_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, comment.getIssue().getId());
            pstmt.setLong(2, comment.getAuthor().getId());
            pstmt.setString(3, comment.getContent());
            pstmt.setString(4, comment.getCreatedDate().toString());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) comment.setId(keys.getLong(1));
            return comment;

        } catch (SQLException e) {
            throw new RuntimeException("댓글 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> findByIssueId(Long issueId) {
        String sql = """
            SELECT c.id, c.content, c.created_date,
                   a.id as author_id, a.username, a.role
            FROM comment c
            JOIN account a ON c.author_id = a.id
            WHERE c.issue_id = ?
            ORDER BY c.id ASC
            """;
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, issueId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Account author = new Account(
                        rs.getLong("author_id"),
                        rs.getString("username"),
                        null
                );

                Comment c = new Comment();
                c.setId(rs.getLong("id"));
                c.setAuthor(author);
                c.setContent(rs.getString("content"));
                c.setCreatedDate(LocalDateTime.parse(
                        rs.getString("created_date").replace(" ", "T")));
                comments.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("댓글 조회 실패: " + e.getMessage(), e);
        }
        return comments;
    }
}