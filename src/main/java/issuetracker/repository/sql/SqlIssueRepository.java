package issuetracker.repository.sql;

import issuetracker.model.IssueFilter;
import issuetracker.repository.issue.IssueRepository;

import java.sql.*;
import java.util.*;

public class SqlIssueRepository implements IssueRepository {

    private final Connection connection;

    public SqlIssueRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Issue1> findById(int issueId) {
        String sql = "SELECT * FROM issue WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, issueId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapIssue(rs));
        } catch (SQLException e) {
            throw new RuntimeException("이슈 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Issue1> findByFilter(IssueFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM issue WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filter.status != null && !filter.status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(filter.status);
        }
        if (filter.assigneeId != null) {
            sql.append(" AND assignee_id = ?");
            params.add(filter.assigneeId);
        }
        if (filter.reporterId != null) {
            sql.append(" AND reporter_id = ?");
            params.add(filter.reporterId);
        }
        if (filter.keyword != null && !filter.keyword.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            params.add("%" + filter.keyword + "%");
            params.add("%" + filter.keyword + "%");
        }

        List<Issue1> issues = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) issues.add(mapIssue(rs));
        } catch (SQLException e) {
            throw new RuntimeException("필터 검색 실패", e);
        }
        return issues;
    }

    private Issue1 mapIssue(ResultSet rs) throws SQLException {
        Issue1 issue = new Issue1();
        issue.setId(rs.getInt("id"));
        issue.setProjectId(rs.getInt("project_id"));
        issue.setTitle(rs.getString("title"));
        issue.setDescription(rs.getString("description"));
        issue.setStatus(rs.getString("status"));
        issue.setPriority(rs.getString("priority"));
        issue.setReporterId(rs.getInt("reporter_id"));
        issue.setAssigneeId(rs.getObject("assignee_id") != null ? rs.getInt("assignee_id") : null);
        issue.setFixerId(rs.getObject("fixer_id") != null ? rs.getInt("fixer_id") : null);
        issue.setReportedDate(rs.getString("reported_date"));
        return issue;
    }
}