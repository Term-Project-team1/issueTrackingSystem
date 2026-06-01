package issuetracker.ui.swing.view;

import javax.swing.*;
import java.awt.*;

import static issuetracker.ui.swing.layout.MainFrame.*;

public final class SwingViewSupport {
    private SwingViewSupport() {
    }

    public static JPanel page(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));
        JLabel label = new JLabel(title);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    public static JPanel card(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new issuetracker.ui.swing.SwingStyles.RoundedLineBorder(BORDER, 14),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        JLabel label = new JLabel(title);
        label.setForeground(TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 17f));
        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public static JPanel strip() {
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND);
        return panel;
    }

    public static JLabel pageDescription(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(issuetracker.ui.swing.layout.MainFrame.MUTED);
        return label;
    }
}
