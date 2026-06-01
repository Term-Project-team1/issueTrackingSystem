package issuetracker.ui.swing;

import issuetracker.ui.UiFormat;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static issuetracker.ui.swing.layout.MainFrame.*;

public final class SwingStyles {
    private SwingStyles() {
    }

    public static void installDefaults() {
        UIManager.put("Panel.background", PANEL);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", new Color(38, 49, 68));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("TextField.background", FIELD);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.background", FIELD);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("ComboBox.background", FIELD);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.buttonBackground", FIELD);
        UIManager.put("ComboBox.buttonDarkShadow", BORDER);
        UIManager.put("ComboBox.buttonHighlight", FIELD);
        UIManager.put("ComboBox.buttonShadow", BORDER);
        UIManager.put("ComboBox.selectionBackground", BLUE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("List.background", FIELD);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("Table.background", FIELD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", new Color(17, 22, 29));
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("ScrollPane.background", FIELD);
        UIManager.put("Viewport.background", FIELD);
    }

    public static <T extends JTextField> T textField(T field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(66, 76, 94), 18),
                BorderFactory.createEmptyBorder(11, 15, 11, 15)
        ));
        field.setOpaque(true);
        field.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
        return field;
    }

    public static JTextArea textArea(JTextArea area) {
        area.setBackground(FIELD);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setSelectionColor(BLUE);
        area.setSelectedTextColor(Color.WHITE);
        area.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(66, 76, 94), 18),
                BorderFactory.createEmptyBorder(11, 15, 11, 15)
        ));
        area.setOpaque(true);
        area.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 76));
        return area;
    }

    public static JButton button(JButton button) {
        return button(button, new Color(31, 41, 55));
    }

    public static JButton primaryButton(JButton button) {
        return button(button, BLUE);
    }

    public static JButton dangerButton(JButton button) {
        return button(button, RED);
    }

    private static JButton button(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBackground(background);
        button.setForeground(background.equals(BLUE) ? new Color(7, 16, 30) : TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 13f));
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = button.getModel().isRollover() ? background.brighter() : background;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), 28, 28);
                g2.dispose();
                super.paint(graphics, component);
            }
        });
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(background.darker(), 28),
                BorderFactory.createEmptyBorder(11, 17, 11, 17)
        ));
        button.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
        return button;
    }

    public static JButton pillButton(String text) {
        JButton button = button(new JButton(text), FIELD);
        button.setForeground(MUTED);
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(61, 71, 89), 20),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        return button;
    }

    public static <T> JComboBox<T> comboBox(JComboBox<T> comboBox) {
        comboBox.setBackground(FIELD);
        comboBox.setForeground(TEXT);
        comboBox.setOpaque(true);
        comboBox.setBorder(new RoundedLineBorder(new Color(66, 76, 94), 18));
        comboBox.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 44));
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▾");
                button.setForeground(TEXT);
                button.setBackground(FIELD);
                button.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER));
                button.setFocusPainted(false);
                return button;
            }
        });
        ListCellRenderer<? super T> original = comboBox.getRenderer();
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            Component component = original.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (component instanceof JComponent jComponent) {
                jComponent.setOpaque(true);
                jComponent.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            }
            if (component instanceof JLabel label) {
                label.setText(UiFormat.display(value));
            }
            component.setBackground(isSelected ? BLUE : FIELD);
            component.setForeground(TEXT);
            return component;
        });
        return comboBox;
    }

    public static <T> JList<T> list(JList<T> list) {
        list.setBackground(FIELD);
        list.setForeground(TEXT);
        list.setSelectionBackground(BLUE);
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(32);
        list.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        list.setCellRenderer((source, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(UiFormat.display(value));
            label.setOpaque(true);
            label.setForeground(isSelected ? Color.WHITE : TEXT);
            label.setBackground(isSelected ? BLUE : FIELD);
            label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            return label;
        });
        return list;
    }

    public static JTable table(JTable table) {
        table.setRowHeight(56);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(14, 19, 27));
        table.setForeground(TEXT);
        table.setGridColor(FIELD);
        table.setSelectionBackground(new Color(23, 36, 56));
        table.setSelectionForeground(TEXT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setBorder(BorderFactory.createEmptyBorder());
        table.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 46));
        table.getTableHeader().setBackground(new Color(17, 22, 29));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setOpaque(true);
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, (source, value, isSelected, hasFocus, row, column) -> {
            JLabel label = (JLabel) renderer.getTableCellRendererComponent(source, value, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            label.setForeground(isSelected ? TEXT : TEXT);
            label.setBackground(isSelected ? new Color(31, 47, 70) : new Color(14, 19, 27));
            label.setFont(label.getFont().deriveFont(column == 1 ? Font.BOLD : Font.PLAIN, 13f));
            if ("ID".equals(source.getColumnName(column))) {
                label.setForeground(BLUE);
                label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
            }
            if ("Status".equals(source.getColumnName(column)) || "Priority".equals(source.getColumnName(column))
                    || "상태".equals(source.getColumnName(column)) || "우선순위".equals(source.getColumnName(column))) {
                label.setText("  " + String.valueOf(value) + "  ");
                label.setForeground(priorityOrStatusColor(source.getColumnName(column), String.valueOf(value)));
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return label;
        });
        table.getTableHeader().setDefaultRenderer((source, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(String.valueOf(value).toUpperCase());
            label.setOpaque(true);
            label.setBackground(new Color(17, 22, 29));
            label.setForeground(MUTED);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
            label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            return label;
        });
        return table;
    }

    public static JScrollPane scrollPane(Component content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBackground(FIELD);
        scrollPane.getViewport().setBackground(FIELD);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(scrollBarUi());
        scrollPane.getHorizontalScrollBar().setUI(scrollBarUi());
        scrollPane.getVerticalScrollBar().setBackground(FIELD);
        scrollPane.getHorizontalScrollBar().setBackground(FIELD);
        return scrollPane;
    }

    private static BasicScrollBarUI scrollBarUi() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(48, 58, 76);
                trackColor = FIELD;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return scrollButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return scrollButton();
            }

            private JButton scrollButton() {
                JButton button = new JButton();
                button.setPreferredSize(new java.awt.Dimension(0, 0));
                button.setMinimumSize(new java.awt.Dimension(0, 0));
                button.setMaximumSize(new java.awt.Dimension(0, 0));
                button.setBackground(FIELD);
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }
        };
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel badge(String text, Color color) {
        JLabel label = roundedLabel(text, color, 14);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setBorder(BorderFactory.createEmptyBorder(6, 11, 6, 11));
        return label;
    }

    public static JLabel avatar(String text) {
        JLabel label = roundedLabel(text, new Color(37, 43, 54), 34);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(new Color(199, 206, 217));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setPreferredSize(new java.awt.Dimension(34, 34));
        return label;
    }

    public static JLabel roleBadge(String text) {
        JLabel label = roundedLabel(text, new Color(49, 28, 31), 16);
        label.setForeground(new Color(255, 116, 111));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(147, 65, 63), 16),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return label;
    }

    private static Color priorityOrStatusColor(String column, String value) {
        if ("상태".equals(column) || "Status".equals(column)) {
            return new Color(195, 202, 214);
        }
        return switch (value) {
            case "차단" -> new Color(239, 119, 114);
            case "치명" -> new Color(237, 123, 54);
            case "주요" -> new Color(255, 208, 82);
            case "보통" -> new Color(154, 196, 255);
            default -> new Color(116, 125, 137);
        };
    }

    public static JLabel codeBadge(String text) {
        JLabel label = roundedLabel(text, new Color(32, 38, 50), 14);
        label.setForeground(BLUE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(55, 68, 92), 14),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return label;
    }

    public static JLabel roundedLabel(String text, Color background, int radius) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        label.setOpaque(false);
        label.setBackground(background);
        return label;
    }

    public static final class RoundedLineBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;

        public RoundedLineBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(Component component) {
            return new java.awt.Insets(1, 1, 1, 1);
        }
    }
}
