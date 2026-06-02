package issuetracker.ui.swing.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.issue.Issue;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RecommendationPanel extends JPanel {
    public RecommendationPanel(AppControllers controllers, UiSession session) {
        JPanel page = SwingViewSupport.page("Recommendation - " + UiFormat.projectWithId(session.getSelectedProject()));
        Long projectId = session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
        List<Issue> issues = controllers.search().searchIssues(projectId, null, null, null, null);
        JTable table = SwingStyles.table(new JTable(DashboardPanel.model(issues)));
        JList<String> list = SwingStyles.list(new JList<>());
        JButton learn = SwingStyles.primaryButton(new JButton("learn now"));
        learn.addActionListener(event -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Issue selected = controllers.issue().viewIssue((Long) table.getValueAt(row, 0));
                list.setListData(controllers.recommendation().recommendAssignees(selected, 3).stream().map(UiFormat::account).toArray(String[]::new));
            }
        });
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.add(learn);
        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(SwingViewSupport.card("이슈 선택", SwingStyles.scrollPane(table)), BorderLayout.CENTER);
        body.add(SwingViewSupport.card("추천 후보 Top3", SwingStyles.scrollPane(list)), BorderLayout.EAST);
        page.add(toolbar, BorderLayout.NORTH);
        page.add(body, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(page, BorderLayout.CENTER);
        if (!issues.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }
}
