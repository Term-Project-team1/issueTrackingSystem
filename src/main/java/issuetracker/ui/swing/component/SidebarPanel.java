package issuetracker.ui.swing.component;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static issuetracker.ui.swing.layout.MainFrame.*;

public class SidebarPanel extends JPanel {
    private static final Map<String, String> MENUS = new LinkedHashMap<>();

    static {
        MENUS.put("Dashboard", "⌘   대시보드");
        MENUS.put("Projects", "▱   프로젝트");
        MENUS.put("Issues", "⌬   이슈");
        MENUS.put("Statistics", "▣   통계");
        MENUS.put("Accounts", "⚭   사용자");
    }

    private final AppControllers controllers;
    private final UiSession session;
    private final JLabel projectCode = SwingStyles.codeBadge("PROJ-");
    private final JLabel projectName = new JLabel("-");
    private final JLabel userAvatar = SwingStyles.avatar("--");
    private final JLabel userName = new JLabel("-");
    private final JLabel userRole = SwingStyles.roleBadge("-");

    public SidebarPanel(AppControllers controllers, UiSession session, Consumer<String> onSelect) {
        super(new BorderLayout());
        this.controllers = controllers;
        this.session = session;
        setPreferredSize(new Dimension(196, 0));
        setBackground(SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(brand());
        top.add(user());
        top.add(project());
        JLabel nav = section("네비게이션");
        nav.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 0));
        top.add(nav);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 10));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        for (var entry : MENUS.entrySet()) {
            JButton button = new JButton(entry.getValue());
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setBackground(entry.getKey().equals("Dashboard") ? new Color(40, 46, 58) : SIDEBAR);
            button.setForeground(entry.getKey().equals("Dashboard") ? BLUE : new Color(165, 175, 189));
            button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
            button.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            button.addActionListener(event -> {
                for (var component : menu.getComponents()) {
                    component.setBackground(SIDEBAR);
                    component.setForeground(new Color(165, 175, 189));
                }
                button.setBackground(new Color(40, 46, 58));
                button.setForeground(BLUE);
                onSelect.accept(entry.getKey());
            });
            menu.add(button);
        }
        top.add(menu);

        JLabel footer = new JLabel("●  시스템 정상");
        footer.setForeground(new Color(106, 195, 106));
        footer.setBorder(BorderFactory.createEmptyBorder(12, 20, 14, 20));
        add(top, BorderLayout.NORTH);
        add(Box.createVerticalGlue(), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        updateProject();
        updateUser();
        session.addProjectListener(this::updateProject);
        session.addProjectListener(this::updateUser);
    }

    private JPanel brand() {
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(18, 14, 18, 14)
        ));
        JLabel logo = SwingStyles.roundedLabel("<html><center>이슈<br>있슈</center></html>", new Color(255, 105, 100), 18);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 13f));
        logo.setPreferredSize(new Dimension(36, 36));
        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 3));
        texts.setOpaque(false);
        JLabel title = new JLabel("ITS");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel sub = new JLabel("Issue Tracker");
        sub.setForeground(MUTED);
        texts.add(title);
        texts.add(sub);
        brand.add(logo);
        brand.add(texts);
        return brand;
    }

    private JPanel user() {
        JPanel user = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        user.setOpaque(false);
        user.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)
        ));
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setOpaque(false);
        userName.setForeground(TEXT);
        userName.setFont(userName.getFont().deriveFont(Font.BOLD, 15f));
        info.add(userName);
        info.add(userRole);
        user.add(userAvatar);
        user.add(info);
        return user;
    }

    private JPanel project() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 8, 14, 8));
        wrapper.add(section("⌄  프로젝트"), BorderLayout.NORTH);
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pill.setBackground(new Color(40, 46, 58));
        pill.setBorder(BorderFactory.createEmptyBorder(12, 13, 12, 13));
        projectName.setForeground(BLUE);
        projectName.setFont(projectName.getFont().deriveFont(Font.BOLD, 13f));
        pill.add(projectCode);
        pill.add(projectName);
        wrapper.add(pill, BorderLayout.CENTER);
        return wrapper;
    }

    private JLabel section(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    private void updateProject() {
        var project = session.getSelectedProject();
        if (project == null && !controllers.project().findAll().isEmpty()) {
            project = controllers.project().findAll().get(0);
        }
        if (project == null) {
            projectCode.setText("PROJ-");
            projectName.setText("-");
            return;
        }
        projectCode.setText("PROJ" + project.getId());
        projectName.setText(project.getName());
    }

    private void updateUser() {
        Account currentUser = session.getCurrentUser();
        if (currentUser == null) {
            userAvatar.setText("--");
            userName.setText("-");
            userRole.setText("-");
            return;
        }
        userAvatar.setText(avatarText(currentUser));
        userName.setText(currentUser.getUsername());
        userRole.setText(currentUser.getRole().name());
    }

    private String avatarText(Account account) {
        if (account == null || account.getUsername() == null || account.getUsername().isBlank()) {
            return "--";
        }
        String username = account.getUsername();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }
}
