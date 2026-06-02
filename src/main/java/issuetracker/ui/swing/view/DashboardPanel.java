package issuetracker.ui.swing.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    public DashboardPanel(AppControllers controllers, UiSession session) {
        super(new BorderLayout());
        Long projectId = session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
        List<Issue> issues = controllers.search().searchIssues(projectId, null, null, null, null);

        JPanel page = SwingViewSupport.strip();
        page.setLayout(new BorderLayout(0, 14));
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        page.add(header(issues), BorderLayout.NORTH);

        JTable table = SwingStyles.table(new JTable(model(issues)));
        page.add(SwingViewSupport.card("Issue Board", SwingStyles.scrollPane(table)), BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }

    private JPanel header(List<Issue> issues) {
        JPanel header = SwingViewSupport.strip();
        header.setLayout(new BorderLayout(0, 4));
        JLabel title = new JLabel("Issues");
        title.setForeground(issuetracker.ui.swing.layout.MainFrame.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel description = SwingViewSupport.pageDescription("현재 프로젝트의 이슈 " + issues.size() + "개를 테이블 중심으로 확인합니다.");
        header.add(title, BorderLayout.NORTH);
        header.add(description, BorderLayout.CENTER);
        return header;
    }

    static DefaultTableModel model(List<Issue> issues) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "제목", "상태", "우선순위", "보고자", "담당자", "수정자", "등록일"}, 0);
        for (Issue issue : issues) {
            model.addRow(new Object[]{issueId(issue), issue.getTitle(), statusLabel(issue.getStatus()), priorityLabel(issue.getPriority()),
                    UiFormat.username(issue.getReporter()), issue.getAssignee() == null ? "미할당" : UiFormat.username(issue.getAssignee()),
                    UiFormat.username(issue.getFixer()), issue.getReportedDate() == null ? "-" : issue.getReportedDate().toLocalDate()});
        }
        return model;
    }

    static String issueId(Issue issue) {
        return String.format("ISS-%03d", issue.getId());
    }

    static String statusLabel(IssueStatus status) {
        return switch (status) {
            case NEW -> "신규";
            case ASSIGNED -> "할당됨";
            case FIXED -> "수정완료";
            case RESOLVED -> "해결됨";
            case CLOSED -> "종료됨";
            case REOPENED -> "재오픈";
        };
    }

    static String priorityLabel(Priority priority) {
        return switch (priority) {
            case BLOCKER -> "차단";
            case CRITICAL -> "치명";
            case MAJOR -> "주요";
            case MINOR -> "보통";
            case TRIVIAL -> "사소";
        };
    }

    static Color priorityColor(Priority priority) {
        return switch (priority) {
            case BLOCKER -> new Color(239, 119, 114);
            case CRITICAL -> new Color(237, 123, 54);
            case MAJOR -> new Color(255, 208, 82);
            case MINOR -> new Color(154, 196, 255);
            case TRIVIAL -> new Color(116, 125, 137);
        };
    }
}
