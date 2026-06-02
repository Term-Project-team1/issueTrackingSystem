package issuetracker.ui.swing.component;

import issuetracker.controller.AccountController;
import issuetracker.domain.account.Role;
import issuetracker.ui.swing.SwingStyles;
import issuetracker.ui.swing.view.SwingViewSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AccountCreateDialog extends JDialog {
    public AccountCreateDialog(JFrame owner, AccountController controller, Runnable onCreated) {
        super(owner, "Create Account", true);
        setSize(460, 320);
        setLocationRelativeTo(owner);
        JTextField username = new JTextField(22);
        username.setEditable(true);
        username.setEnabled(true);
        username.setFocusable(true);
        username.setBackground(new Color(19, 24, 32));
        username.setForeground(Color.WHITE);
        username.setCaretColor(Color.WHITE);
        username.setSelectedTextColor(Color.WHITE);
        username.setSelectionColor(new Color(109, 166, 255));
        username.setFont(username.getFont().deriveFont(Font.PLAIN, 15f));
        username.setPreferredSize(new Dimension(340, 48));
        username.setMinimumSize(new Dimension(300, 48));
        username.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        username.setBorder(BorderFactory.createCompoundBorder(
                new SwingStyles.RoundedLineBorder(new Color(88, 100, 122), 10),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JComboBox<Role> role = SwingStyles.comboBox(new JComboBox<>(Role.values()));
        role.setPreferredSize(new Dimension(340, 48));
        role.setMinimumSize(new Dimension(300, 48));

        JPanel fields = SwingViewSupport.strip();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(label("Username"));
        fields.add(Box.createVerticalStrut(6));
        fields.add(username);
        fields.add(Box.createVerticalStrut(14));
        fields.add(label("Role"));
        fields.add(Box.createVerticalStrut(6));
        fields.add(role);

        JButton cancel = SwingStyles.button(new JButton("Cancel"));
        cancel.addActionListener(event -> dispose());
        JButton create = SwingStyles.primaryButton(new JButton("Create Account"));
        create.addActionListener(event -> {
            if (!username.getText().isBlank()) {
                controller.createAccount(username.getText(), (Role) role.getSelectedItem());
                onCreated.run();
                dispose();
            }
        });
        JPanel actions = SwingViewSupport.strip();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(cancel);
        actions.add(create);

        JPanel body = SwingViewSupport.strip();
        body.setLayout(new BorderLayout(0, 18));
        body.add(fields, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);
        setContentPane(SwingViewSupport.card("Create Account", body));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                username.requestFocusInWindow();
            }
        });
    }

    private JLabel label(String text) {
        JLabel label = SwingStyles.mutedLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }
}
