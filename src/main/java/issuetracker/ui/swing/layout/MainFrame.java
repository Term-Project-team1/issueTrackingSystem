package issuetracker.ui.swing.layout;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.component.SidebarPanel;
import issuetracker.ui.swing.view.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public static final Color BACKGROUND = new Color(11, 15, 22);
    public static final Color SIDEBAR = new Color(23, 27, 34);
    public static final Color PANEL = new Color(23, 27, 34);
    public static final Color BORDER = new Color(48, 55, 67);
    public static final Color TEXT = new Color(213, 219, 229);
    public static final Color MUTED = new Color(126, 136, 151);
    public static final Color BLUE = new Color(109, 166, 255);
    public static final Color FIELD = new Color(19, 24, 32);
    public static final Color GREEN = new Color(106, 195, 106);
    public static final Color RED = new Color(239, 119, 114);

    private final AppControllers controllers;
    private final UiSession session = new UiSession();
    private final JPanel content = new JPanel(new BorderLayout());
    private JComboBox<Project> projectBox;
    private JComboBox<Account> userBox;
    private JLabel userAvatar;
    private JLabel roleBadge;
    private boolean syncingProjectBox;
    private boolean syncingUserBox;

    public MainFrame(AppControllers controllers) {
        this.controllers = controllers;
        setTitle("Issue Tracking System - Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setSize(1440, 860);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);
        content.setBackground(BACKGROUND);
        if (!controllers.project().findAll().isEmpty()) {
            session.selectProject(controllers.project().findAll().get(0));
        }
        selectInitialUser();
        getContentPane().add(new SidebarPanel(controllers, session, this::showView), BorderLayout.WEST);
        getContentPane().add(topBar(), BorderLayout.NORTH);
        getContentPane().add(content, BorderLayout.CENTER);
        session.addProjectListener(this::syncProjectBox);
        session.addProjectListener(this::syncUserBox);
        showView("Dashboard");
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0));
        bar.setBackground(new Color(18, 22, 29));
        bar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                javax.swing.BorderFactory.createEmptyBorder(14, 28, 14, 16)
        ));
        projectBox = issuetracker.ui.swing.SwingStyles.comboBox(new JComboBox<>(controllers.project().findAll().toArray(Project[]::new)));
        projectBox.addActionListener(event -> {
            if (!syncingProjectBox && projectBox.getSelectedItem() != null) {
                session.selectProject((Project) projectBox.getSelectedItem());
                refreshCurrent();
            }
        });
        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(projectBox, BorderLayout.CENTER);
        JPanel filters = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        filters.setOpaque(false);
        userAvatar = issuetracker.ui.swing.SwingStyles.avatar(avatarText(session.getCurrentUser()));
        filters.add(userAvatar);
        userBox = issuetracker.ui.swing.SwingStyles.comboBox(new JComboBox<>(controllers.account().findAll().toArray(Account[]::new)));
        userBox.setPreferredSize(new Dimension(170, 42));
        userBox.addActionListener(event -> {
            if (!syncingUserBox && userBox.getSelectedItem() != null) {
                session.selectCurrentUser((Account) userBox.getSelectedItem());
                refreshCurrent();
            }
        });
        filters.add(userBox);
        roleBadge = issuetracker.ui.swing.SwingStyles.roleBadge(roleText(session.getCurrentUser()));
        filters.add(roleBadge);
        javax.swing.JButton create = issuetracker.ui.swing.SwingStyles.primaryButton(new javax.swing.JButton("+  이슈 생성"));
        create.addActionListener(event -> new issuetracker.ui.swing.component.IssueCreateDialog(this, controllers, session, this::showIssuesAfterCreate).setVisible(true));
        filters.add(create);
        syncUserBox();
        bar.add(left, BorderLayout.WEST);
        bar.add(filters, BorderLayout.EAST);
        return bar;
    }

    private void selectInitialUser() {
        controllers.account().findAll().stream()
                .filter(account -> "admin".equals(account.getUsername()))
                .findFirst()
                .or(() -> controllers.account().findAll().stream().findFirst())
                .ifPresent(session::selectCurrentUser);
    }

    private void showIssuesAfterCreate() {
        syncProjectBox();
        showView("Issues");
    }

    private void syncProjectBox() {
        if (projectBox == null || session.getSelectedProject() == null) {
            return;
        }
        syncingProjectBox = true;
        try {
            Long before = projectBox.getSelectedItem() instanceof Project project ? project.getId() : null;
            DefaultComboBoxModel<Project> model = new DefaultComboBoxModel<>(controllers.project().findAll().toArray(Project[]::new));
            projectBox.setModel(model);
            Long selectedProjectId = session.getSelectedProject().getId();
            for (int i = 0; i < projectBox.getItemCount(); i++) {
                Project project = projectBox.getItemAt(i);
                if (project != null && project.getId().equals(selectedProjectId)) {
                    if (!project.getId().equals(before)) {
                        projectBox.setSelectedItem(project);
                    }
                    return;
                }
            }
        } finally {
            syncingProjectBox = false;
        }
    }

    private void syncUserBox() {
        if (userBox == null || session.getCurrentUser() == null) {
            return;
        }
        syncingUserBox = true;
        try {
            userBox.setModel(new DefaultComboBoxModel<>(controllers.account().findAll().toArray(Account[]::new)));
            Long selectedUserId = session.getCurrentUser().getId();
            for (int i = 0; i < userBox.getItemCount(); i++) {
                Account account = userBox.getItemAt(i);
                if (account != null && account.getId().equals(selectedUserId)) {
                    userBox.setSelectedIndex(i);
                    break;
                }
            }
            userAvatar.setText(avatarText(session.getCurrentUser()));
            roleBadge.setText(roleText(session.getCurrentUser()));
        } finally {
            syncingUserBox = false;
        }
    }

    private String avatarText(Account account) {
        if (account == null || account.getUsername() == null || account.getUsername().isBlank()) {
            return "--";
        }
        String username = account.getUsername();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }

    private String roleText(Account account) {
        return account == null ? "-" : account.getRole().name();
    }

    private String currentView = "Dashboard";

    private void refreshCurrent() {
        showView(currentView);
    }

    private void showView(String view) {
        currentView = view;
        content.removeAll();
        content.add(switch (view) {
            case "Projects" -> new ProjectPanel(controllers, session, () -> showView("Issues"));
            case "Issues" -> new IssuePanel(controllers, session);
            case "Accounts" -> new AccountPanel(controllers.account(), session);
            case "Statistics" -> new StatisticsPanel(controllers, session);
            default -> new DashboardPanel(controllers, session);
        }, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
