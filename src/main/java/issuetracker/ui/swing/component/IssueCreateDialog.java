package issuetracker.ui.swing.component;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiSession;
import issuetracker.ui.swing.SwingStyles;
import issuetracker.ui.swing.view.SwingViewSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class IssueCreateDialog extends JDialog {
    public IssueCreateDialog(JFrame owner, AppControllers controllers, UiSession session, Runnable onCreated) {
        super(owner, "Create Issue", true);
        JPanel root = SwingViewSupport.card("Create Issue", form(controllers, session, onCreated));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(root);
        setMinimumSize(new Dimension(560, 560));
        setSize(580, 600);
        setLocationRelativeTo(owner);
    }

    private JPanel form(AppControllers controllers, UiSession session, Runnable onCreated) {
        JTextField title = SwingStyles.textField(new JTextField());
        title.setPreferredSize(new Dimension(440, 42));
        title.setMinimumSize(new Dimension(360, 42));
        JTextArea description = SwingStyles.textArea(new JTextArea(4, 30));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JScrollPane descriptionScroll = SwingStyles.scrollPane(description);
        descriptionScroll.setPreferredSize(new Dimension(440, 126));
        descriptionScroll.setMinimumSize(new Dimension(360, 110));
        JComboBox<Priority> priority = SwingStyles.comboBox(new JComboBox<>(Priority.values()));
        JComboBox<Project> project = SwingStyles.comboBox(new JComboBox<>(controllers.project().findAll().toArray(Project[]::new)));
        selectSessionProject(project, session.getSelectedProject());
        Account currentUser = session.getCurrentUser();
        JLabel reporter = SwingStyles.label(issuetracker.ui.UiFormat.account(currentUser));

        JPanel fields = SwingViewSupport.strip();
        fields.setLayout(new GridBagLayout());
        addField(fields, 0, "Title", title, 42);
        addField(fields, 1, "Description", descriptionScroll, 126);
        addField(fields, 2, "Priority", priority, 42);
        addField(fields, 3, "Project", project, 42);
        addField(fields, 4, "Reporter", reporter, 42);

        JButton cancel = SwingStyles.button(new JButton("Cancel"));
        cancel.addActionListener(event -> dispose());
        JButton create = SwingStyles.primaryButton(new JButton("Create Issue"));
        create.addActionListener(event -> {
            Account selectedReporter = session.getCurrentUser();
            if (selectedReporter == null || selectedReporter.getRole() != Role.TESTER) {
                JOptionPane.showMessageDialog(this, "TESTER 계정으로 전환한 뒤 이슈를 생성해 주세요.", "권한 확인", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!title.getText().isBlank() && project.getSelectedItem() != null) {
                Project selectedProject = (Project) project.getSelectedItem();
                var created = controllers.issue().createIssue(selectedProject, selectedReporter,
                        title.getText(), description.getText(), (Priority) priority.getSelectedItem());
                session.rememberCreatedIssue(created);
                session.selectProject(selectedProject);
                onCreated.run();
                dispose();
            }
        });
        JPanel actions = SwingViewSupport.strip();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(cancel);
        actions.add(create);

        JPanel panel = SwingViewSupport.strip();
        panel.setLayout(new BorderLayout(0, 18));
        panel.add(fields, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                title.requestFocusInWindow();
            }
        });
        return panel;
    }

    private void addField(JPanel parent, int row, String labelText, java.awt.Component input, int height) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row * 2;
        labelConstraints.weightx = 1;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.insets = new Insets(row == 0 ? 0 : 10, 0, 5, 0);
        parent.add(label(labelText), labelConstraints);

        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 0;
        inputConstraints.gridy = row * 2 + 1;
        inputConstraints.weightx = 1;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputConstraints.insets = new Insets(0, 0, 0, 0);
        input.setPreferredSize(new Dimension(440, height));
        parent.add(input, inputConstraints);
    }

    private JLabel label(String text) {
        JLabel label = SwingStyles.mutedLabel(text);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        return label;
    }

    private void selectSessionProject(JComboBox<Project> projectBox, Project selectedProject) {
        if (selectedProject == null) {
            return;
        }
        for (int i = 0; i < projectBox.getItemCount(); i++) {
            Project project = projectBox.getItemAt(i);
            if (project != null && Objects.equals(project.getId(), selectedProject.getId())) {
                projectBox.setSelectedIndex(i);
                return;
            }
        }
    }
}
