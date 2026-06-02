package issuetracker.ui.swing.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProjectPanel extends JPanel {
    private final AppControllers controllers;
    private final UiSession session;
    private final Runnable openIssues;
    private final JPanel cards = new JPanel();

    public ProjectPanel(AppControllers controllers, UiSession session, Runnable openIssues) {
        this.controllers = controllers;
        this.session = session;
        this.openIssues = openIssues;
        setLayout(new BorderLayout());

        JPanel page = SwingViewSupport.strip();
        page.setLayout(new BorderLayout(0, 26));
        page.setBorder(BorderFactory.createEmptyBorder(34, 24, 24, 24));
        page.add(header(), BorderLayout.NORTH);
        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = SwingStyles.scrollPane(cards);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        page.add(scrollPane, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
        refresh();
    }

    private JPanel header() {
        JPanel header = SwingViewSupport.strip();
        header.setLayout(new BorderLayout());
        JPanel text = SwingViewSupport.strip();
        text.setLayout(new GridLayout(2, 1, 0, 8));
        JLabel title = new JLabel("프로젝트");
        title.setForeground(issuetracker.ui.swing.layout.MainFrame.TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        text.add(title);
        text.add(SwingViewSupport.pageDescription("프로젝트를 관리하고 이슈 분포를 확인합니다"));

        JTextField name = SwingStyles.textField(new JTextField(18));
        JButton create = SwingStyles.primaryButton(new JButton("+  새 프로젝트"));
        create.addActionListener(event -> {
            if (session.getCurrentUser() == null || session.getCurrentUser().getRole() != Role.ADMIN) {
                JOptionPane.showMessageDialog(this, "ADMIN 계정으로 전환한 뒤 프로젝트를 생성해 주세요.", "권한 확인", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!name.getText().isBlank()) {
                Project created = controllers.project().createProject(name.getText());
                session.selectProject(created);
                name.setText("");
                refresh();
            }
        });
        JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        form.setOpaque(false);
        form.add(name);
        form.add(create);
        header.add(text, BorderLayout.WEST);
        header.add(form, BorderLayout.EAST);
        return header;
    }

    private void refresh() {
        cards.removeAll();
        for (Project project : controllers.project().findAll()) {
            cards.add(card(project));
        }
        if (session.getSelectedProject() == null && !controllers.project().findAll().isEmpty()) {
            session.selectProject(controllers.project().findAll().get(0));
        }
        revalidate();
        repaint();
    }

    private JPanel card(Project project) {
        JPanel card = SwingViewSupport.card("", body(project));
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 190));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
                new SwingStyles.RoundedLineBorder(isSelected(project)
                        ? issuetracker.ui.swing.layout.MainFrame.BLUE
                        : issuetracker.ui.swing.layout.MainFrame.BORDER, 14),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                session.selectProject(project);
                if (event.getClickCount() == 2) {
                    openIssues.run();
                }
            }
        });
        return card;
    }

    private JPanel body(Project project) {
        JPanel body = SwingViewSupport.strip();
        body.setLayout(new BorderLayout(18, 0));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(SwingStyles.codeBadge("PROJ" + project.getId()), BorderLayout.WEST);
        List<Issue> issues = issues(project);
        top.add(SwingStyles.mutedLabel(issues.size() + "개 이슈"), BorderLayout.CENTER);
        if (isSelected(project)) {
            top.add(SwingStyles.badge("선택됨", issuetracker.ui.swing.layout.MainFrame.BLUE), BorderLayout.EAST);
        }

        JPanel center = SwingViewSupport.strip();
        center.setLayout(new GridLayout(2, 1, 0, 8));
        JLabel name = new JLabel(project.getName());
        name.setForeground(issuetracker.ui.swing.layout.MainFrame.TEXT);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 17f));
        center.add(name);
        center.add(SwingViewSupport.pageDescription("생성일 " + project.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        JPanel metrics = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        metrics.setOpaque(false);
        long open = issues.stream().filter(issue -> issue.getStatus() != IssueStatus.CLOSED && issue.getStatus() != IssueStatus.RESOLVED).count();
        long fixed = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.FIXED).count();
        long done = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.CLOSED || issue.getStatus() == IssueStatus.RESOLVED).count();
        metrics.add(dot("●  " + open + "개 미해결", issuetracker.ui.swing.layout.MainFrame.BLUE));
        metrics.add(dot("●  " + fixed + "개 수정완료", new Color(194, 143, 255)));
        metrics.add(dot("●  " + done + "개 완료", issuetracker.ui.swing.layout.MainFrame.GREEN));
        JPanel main = SwingViewSupport.strip();
        main.setLayout(new BorderLayout(0, 13));
        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(metrics, BorderLayout.SOUTH);

        JButton openButton = SwingStyles.primaryButton(new JButton("이슈 보기"));
        openButton.addActionListener(event -> {
            session.selectProject(project);
            openIssues.run();
        });
        JPanel actions = SwingViewSupport.strip();
        actions.setLayout(new BorderLayout());
        actions.add(openButton, BorderLayout.NORTH);

        body.add(main, BorderLayout.CENTER);
        body.add(actions, BorderLayout.EAST);
        return body;
    }

    private boolean isSelected(Project project) {
        return session.getSelectedProject() != null
                && project != null
                && project.getId().equals(session.getSelectedProject().getId());
    }

    private JLabel dot(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        return label;
    }

    private List<Issue> issues(Project project) {
        return controllers.search().searchIssues(project.getId(), null, null, null, null);
    }
}
