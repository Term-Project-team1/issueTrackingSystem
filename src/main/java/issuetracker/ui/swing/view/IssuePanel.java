package issuetracker.ui.swing.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IssuePanel extends JPanel {
    private final AppControllers controllers;
    private final UiSession session;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "제목", "상태", "우선순위", "보고자", "담당자", "수정자", "등록일"}, 0);
    private final JTable table = SwingStyles.table(new JTable(model));
    private final JTextArea detail = SwingStyles.textArea(new JTextArea(8, 34));
    private final JLabel emptyDetail = new JLabel("<html><div style='text-align:center;'>이슈를 선택하여 상세 정보를 확인하세요<br>목록에서 이슈를 클릭하면 상세 내용이 표시됩니다</div></html>", SwingConstants.CENTER);
    private final JList<String> comments = SwingStyles.list(new JList<>());
    private final JList<String> recommendations = SwingStyles.list(new JList<>());
    private JPanel actionsPanel;
    private JPanel lowerPanel;
    private JPanel commentPanel;
    private List<Issue> displayedIssues = new ArrayList<>();
    private Issue selected;

    public IssuePanel(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;
        setLayout(new BorderLayout());
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(issuetracker.ui.swing.layout.MainFrame.BACKGROUND);
        body.add(createTop(), BorderLayout.NORTH);
        body.add(SwingStyles.scrollPane(table), BorderLayout.CENTER);
        JPanel detailCard = SwingViewSupport.card("", detailPanel());
        detailCard.setPreferredSize(new java.awt.Dimension(460, 0));
        body.add(detailCard, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(event -> selectCurrent());
        refresh();
    }

    private JPanel createTop() {
        JComboBox<String> status = SwingStyles.comboBox(new JComboBox<>(statusOptions()));
        status.setSelectedItem("ALL");
        JComboBox<Account> reporter = accountFilter(controllers.account().findByRole(Role.TESTER));
        JComboBox<Account> assignee = accountFilter(controllers.account().findByRole(Role.DEV));
        JTextField keyword = SwingStyles.textField(new JTextField(22));
        JButton search = SwingStyles.button(new JButton("Search"));
        search.addActionListener(event -> {
            Long projectId = session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
            IssueStatus selectedStatus = "ALL".equals(status.getSelectedItem()) ? null : IssueStatus.valueOf((String) status.getSelectedItem());
            Account selectedReporter = (Account) reporter.getSelectedItem();
            Account selectedAssignee = (Account) assignee.getSelectedItem();
            setIssues(session.mergeVisibleIssues(controllers.search().searchIssues(
                    projectId,
                    selectedStatus,
                    selectedReporter == null ? null : selectedReporter.getId(),
                    selectedAssignee == null ? null : selectedAssignee.getId(),
                    keyword.getText()),
                    projectId,
                    selectedStatus,
                    selectedReporter == null ? null : selectedReporter.getId(),
                    selectedAssignee == null ? null : selectedAssignee.getId(),
                    keyword.getText()));
        });
        JButton reload = SwingStyles.button(new JButton("Reload"));
        reload.addActionListener(event -> refresh());
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        JPanel searchRow = SwingViewSupport.strip();
        searchRow.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 4));
        searchRow.add(SwingStyles.pillButton("Status"));
        searchRow.add(status);
        searchRow.add(SwingStyles.pillButton("Reporter"));
        searchRow.add(reporter);
        searchRow.add(SwingStyles.pillButton("Assignee"));
        searchRow.add(assignee);
        searchRow.add(keyword);
        searchRow.add(search);
        searchRow.add(reload);
        top.add(new JLabel(""), BorderLayout.NORTH);
        top.add(searchRow, BorderLayout.CENTER);
        return top;
    }

    private JComboBox<Account> accountFilter(List<Account> values) {
        DefaultComboBoxModel<Account> model = new DefaultComboBoxModel<>();
        model.addElement(null);
        values.forEach(model::addElement);
        JComboBox<Account> comboBox = SwingStyles.comboBox(new JComboBox<>(model));
        comboBox.setSelectedItem(null);
        return comboBox;
    }

    private JPanel detailPanel() {
        detail.setEditable(false);
        detail.setRows(6);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setLayout(new BorderLayout());
        emptyDetail.setForeground(issuetracker.ui.swing.layout.MainFrame.MUTED);
        emptyDetail.setFont(emptyDetail.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        detail.add(emptyDetail, BorderLayout.CENTER);
        JComboBox<Account> assignee = SwingStyles.comboBox(new JComboBox<>(controllers.account().findByRole(Role.DEV).toArray(Account[]::new)));
        selectRole(assignee, Role.DEV, "dev1");
        JLabel currentUser = SwingStyles.label(UiFormat.account(session.getCurrentUser()));
        JButton learn = SwingStyles.button(new JButton("learn now"));
        learn.addActionListener(event -> {
            if (selected != null) {
                recommendations.setListData(controllers.recommendation().recommendAssignees(selected, 3).stream().map(UiFormat::account).toArray(String[]::new));
            }
        });
        JButton assign = action("Assign", Role.PL, () -> {
            if (assignee.getSelectedItem() == null) {
                throw new IllegalArgumentException("담당 개발자를 선택해 주세요.");
            }
            controllers.issue().assignIssue(selected.getId(), (Account) assignee.getSelectedItem(), session.getCurrentUser());
        });
        JButton fixed = action("Mark Fixed", Role.DEV, () -> controllers.issue().markFixed(selected.getId(), session.getCurrentUser()));
        JButton resolve = action("Resolve", Role.TESTER, () -> controllers.issue().resolveIssue(selected.getId(), session.getCurrentUser()));
        JButton close = action("Close", Role.PL, () -> controllers.issue().closeIssue(selected.getId(), session.getCurrentUser()));
        JButton reopen = action("Reopen", null, () -> controllers.issue().reopenIssue(selected.getId(), session.getCurrentUser()));
        JTextField comment = SwingStyles.textField(new JTextField(20));
        comment.setPreferredSize(new java.awt.Dimension(0, 42));
        comment.setToolTipText("선택한 이슈에 현재 사용자로 코멘트를 추가합니다.");
        JButton addComment = SwingStyles.primaryButton(new JButton("Add Comment"));
        addComment.addActionListener(event -> {
            if (selected != null && requireCurrentUser(null) && !comment.getText().isBlank()) {
                try {
                    controllers.comment().addComment(selected.getId(), session.getCurrentUser().getId(), comment.getText());
                    comment.setText("");
                    refreshDetail();
                } catch (RuntimeException exception) {
                    JOptionPane.showMessageDialog(this, exception.getMessage(), "코멘트 추가 실패", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JPanel panel = SwingViewSupport.strip();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.add(detail);
        panel.add(javax.swing.Box.createVerticalStrut(12));
        commentPanel = SwingViewSupport.strip();
        commentPanel.setLayout(new BorderLayout(8, 6));
        JLabel commentLabel = SwingStyles.mutedLabel("코멘트 추가");
        commentLabel.setFont(commentLabel.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        commentPanel.add(commentLabel, BorderLayout.NORTH);
        commentPanel.add(comment, BorderLayout.CENTER);
        commentPanel.add(addComment, BorderLayout.EAST);
        panel.add(commentPanel);
        panel.add(javax.swing.Box.createVerticalStrut(12));
        actionsPanel = SwingViewSupport.strip();
        actionsPanel.setLayout(new GridLayout(0, 2, 8, 8));
        actionsPanel.add(labeled("현재 사용자", currentUser));
        actionsPanel.add(labeled("담당 개발자", assignee));
        actionsPanel.add(learn);
        actionsPanel.add(assign);
        actionsPanel.add(fixed);
        actionsPanel.add(resolve);
        actionsPanel.add(close);
        actionsPanel.add(reopen);
        panel.add(actionsPanel);
        panel.add(javax.swing.Box.createVerticalStrut(12));
        lowerPanel = SwingViewSupport.strip();
        lowerPanel.setLayout(new GridLayout(2, 1, 0, 10));
        lowerPanel.add(listBlock("추천 후보 Top3", recommendations));
        lowerPanel.add(listBlock("Comments", comments));
        panel.add(lowerPanel);
        return panel;
    }

    private JPanel listBlock(String title, JList<String> list) {
        JPanel block = SwingViewSupport.strip();
        block.setLayout(new BorderLayout(0, 6));
        block.add(SwingStyles.mutedLabel(title), BorderLayout.NORTH);
        JScrollPane scrollPane = SwingStyles.scrollPane(list);
        scrollPane.setPreferredSize(new java.awt.Dimension(0, 112));
        block.add(scrollPane, BorderLayout.CENTER);
        return block;
    }

    private JPanel labeled(String label, java.awt.Component input) {
        JPanel panel = SwingViewSupport.strip();
        panel.setLayout(new BorderLayout(0, 5));
        panel.add(SwingStyles.mutedLabel(label), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JButton action(String text, Role requiredRole, Runnable action) {
        JButton button = SwingStyles.button(new JButton(text));
        button.addActionListener(event -> {
            if (selected == null || !requireCurrentUser(requiredRole)) {
                return;
            }
            try {
                Long selectedId = selected.getId();
                action.run();
                selected = controllers.issue().viewIssue(selectedId);
                session.rememberCreatedIssue(selected);
                refreshKeepingSelection(selectedId);
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "작업 실패", JOptionPane.WARNING_MESSAGE);
            }
        });
        return button;
    }

    private boolean requireCurrentUser(Role requiredRole) {
        Account currentUser = session.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "현재 사용자를 선택해 주세요.", "작업 실패", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (requiredRole != null && currentUser.getRole() != requiredRole) {
            JOptionPane.showMessageDialog(this,
                    requiredRole + " 계정으로 전환한 뒤 작업해 주세요.",
                    "권한 확인",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void selectRole(JComboBox<Account> comboBox, Role role, String preferredUsername) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Account account = comboBox.getItemAt(i);
            if (account != null && preferredUsername.equals(account.getUsername())) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Account account = comboBox.getItemAt(i);
            if (account != null && account.getRole() == role) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void refresh() {
        Long projectId = session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
        setIssues(session.mergeVisibleIssues(controllers.search().searchIssues(projectId, null, null, null, null),
                projectId, null, null, null, null));
    }

    private void refreshKeepingSelection(Long issueId) {
        refresh();
        for (int i = 0; i < displayedIssues.size(); i++) {
            if (displayedIssues.get(i).getId().equals(issueId)) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void setIssues(List<Issue> issues) {
        displayedIssues = new ArrayList<>(issues);
        model.setRowCount(0);
        for (Issue issue : issues) {
            model.addRow(new Object[]{
                    DashboardPanel.issueId(issue),
                    ellipsis(issue.getTitle()),
                    DashboardPanel.statusLabel(issue.getStatus()),
                    DashboardPanel.priorityLabel(issue.getPriority()),
                    UiFormat.username(issue.getReporter()),
                    issue.getAssignee() == null ? "미할당" : UiFormat.username(issue.getAssignee()),
                    UiFormat.username(issue.getFixer()),
                    issue.getReportedDate() == null ? "-" : issue.getReportedDate().toLocalDate().toString()
            });
        }
        table.clearSelection();
        selected = null;
        refreshDetail();
    }

    private void selectCurrent() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < displayedIssues.size()) {
            selected = displayedIssues.get(row);
            refreshDetail();
        }
    }

    private void refreshDetail() {
        if (selected == null) {
            detail.setText("");
            emptyDetail.setVisible(true);
            comments.setListData(new String[0]);
            recommendations.setListData(new String[0]);
            if (actionsPanel != null) {
                actionsPanel.setVisible(false);
            }
            if (commentPanel != null) {
                commentPanel.setVisible(false);
            }
            if (lowerPanel != null) {
                lowerPanel.setVisible(false);
            }
            return;
        }
        emptyDetail.setVisible(false);
        if (actionsPanel != null) {
            actionsPanel.setVisible(true);
        }
        if (commentPanel != null) {
            commentPanel.setVisible(true);
        }
        if (lowerPanel != null) {
            lowerPanel.setVisible(true);
        }
        try {
            selected = controllers.comment().viewIssueDetail(selected.getId());
        } catch (RuntimeException ignored) {
            // Keep the just-created issue visible even if the existing repository cannot re-read seed date formats.
        }
        detail.setText("#" + selected.getId() + " " + selected.getTitle() + "\n"
                + selected.getStatus() + " / " + selected.getPriority() + "\n"
                + selected.getDescription() + "\n"
                + "reporter=" + UiFormat.username(selected.getReporter()) + ", assignee=" + UiFormat.username(selected.getAssignee()) + ", fixer=" + UiFormat.username(selected.getFixer()));
        try {
            comments.setListData(controllers.comment().findCommentsByIssue(selected.getId()).stream().map(UiFormat::comment).toArray(String[]::new));
        } catch (RuntimeException ignored) {
            comments.setListData(new String[0]);
        }
        refreshRecommendations();
    }

    private void refreshRecommendations() {
        try {
            recommendations.setListData(controllers.recommendation().recommendAssignees(selected, 3).stream().map(UiFormat::account).toArray(String[]::new));
        } catch (RuntimeException ignored) {
            recommendations.setListData(new String[0]);
        }
    }

    private String ellipsis(String text) {
        if (text == null || text.length() <= 16) {
            return text;
        }
        return text.substring(0, 14) + "...";
    }

    private String[] statusOptions() {
        String[] values = new String[IssueStatus.values().length + 1];
        values[0] = "ALL";
        for (int i = 0; i < IssueStatus.values().length; i++) {
            values[i + 1] = IssueStatus.values()[i].name();
        }
        return values;
    }
}
