package issuetracker.ui.swing.view;

import issuetracker.controller.AccountController;
import issuetracker.domain.account.Role;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;
import issuetracker.ui.swing.component.AccountCreateDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.stream.Collectors;

public class AccountPanel extends JPanel {
    private final AccountController controller;
    private final UiSession session;
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Username", "Role"}, 0);
    private final JPanel summary = SwingViewSupport.strip();

    public AccountPanel(AccountController controller, UiSession session) {
        this.controller = controller;
        this.session = session;
        JPanel page = SwingViewSupport.page("Accounts");
        JButton create = SwingStyles.primaryButton(new JButton("Create Account"));
        create.addActionListener(event -> {
            new AccountCreateDialog((javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), controller, () -> {
                refresh();
                session.selectCurrentUser(session.getCurrentUser());
            }).setVisible(true);
        });
        JPanel form = SwingViewSupport.strip();
        form.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.add(SwingStyles.mutedLabel("Manage roles and team members"));
        form.add(create);
        JTable table = SwingStyles.table(new JTable(model));
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        JPanel top = SwingViewSupport.strip();
        top.setLayout(new BorderLayout(0, 18));
        summary.setLayout(new GridLayout(1, Role.values().length, 16, 0));
        top.add(summary, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        center.add(top, BorderLayout.NORTH);
        center.add(SwingViewSupport.card("계정 목록", SwingStyles.scrollPane(table)), BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(page, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        controller.findAll().forEach(account -> model.addRow(new Object[]{account.getId(), account.getUsername(), account.getRole()}));
        refreshSummary();
    }

    private void refreshSummary() {
        var counts = controller.findAll().stream().collect(Collectors.groupingBy(account -> account.getRole(), Collectors.counting()));
        summary.removeAll();
        for (Role role : Role.values()) {
            summary.add(SwingViewSupport.card(role.name(), new JLabel(String.valueOf(counts.getOrDefault(role, 0L)))));
        }
        summary.revalidate();
        summary.repaint();
    }
}
