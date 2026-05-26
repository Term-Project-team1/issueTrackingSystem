package issuetracker.repository.sql;

import issuetracker.repository.comment.CommentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlCommentRepository implements CommentRepository {

    private final Connection connection;

    public SqlCommentRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comment(issue_id, author_id, content, created_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, comment.getIssueId());
            pstmt.setInt(2, comment.getAuthorId());
            pstmt.setString(3, comment.getContent());
            pstmt.setString(4, comment.getCreatedDate());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) comment.setId(keys.getInt(1));
            return comment;

        } catch (SQLException e) {
            throw new RuntimeException("댓글 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> findByIssueId(int issueId) {
        String sql = "SELECT * FROM comment WHERE issue_id = ? ORDER BY id ASC";
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, issueId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Comment c = new Comment(
                        rs.getInt("issue_id"),
                        rs.getInt("author_id"),
                        rs.getString("content"),
                        rs.getString("created_date")
                );
                c.setId(rs.getInt("id"));
                comments.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("댓글 조회 실패: " + e.getMessage(), e);
        }
        return comments;
    }
}