package issuetracker.ui.swing;

import issuetracker.AppControllerFactory;
import issuetracker.ui.swing.layout.MainFrame;

import javax.swing.*;

public class SwingMainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingStyles.installDefaults();
            new MainFrame(AppControllerFactory.createWithRealServices()).setVisible(true);
        });
    }
}
