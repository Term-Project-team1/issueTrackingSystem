package issuetracker.ui.swing.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;

public class StatisticsPanel extends JPanel {
    private final AppControllers controllers;
    private final UiSession session;
    private JPanel page;

    public StatisticsPanel(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;
        setLayout(new BorderLayout());
        rebuild();
    }

    private void rebuild() {
        removeAll();
        page = SwingViewSupport.page("Statistics - " + UiFormat.projectWithId(session.getSelectedProject()));
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        Project project = session.getSelectedProject();
        if (project != null) {
            Long projectId = project.getId();
            grid.add(SwingViewSupport.card("상태 분포", new PieChartPanel(controllers.statistics().countIssuesByStatus(projectId))));
            grid.add(SwingViewSupport.card("우선순위 분포", new PieChartPanel(controllers.statistics().countIssuesByPriority(projectId))));
            grid.add(SwingViewSupport.card("개발자별 Fix", new BarChartPanel(controllers.statistics().countFixedIssuesByDeveloper(projectId).entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(entry -> UiFormat.username(entry.getKey()), Map.Entry::getValue)))));
            grid.add(SwingViewSupport.card("통계 요약", summary(controllers, projectId)));
        }
        JPanel content = SwingViewSupport.strip();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(grid);

        if (project != null) {
            Long projectId = project.getId();
            content.add(Box.createVerticalStrut(18));
            content.add(SwingViewSupport.card("일별 이슈 발생 수", new LineChartPanel(dailyCounts(projectId))));
            content.add(Box.createVerticalStrut(18));
            content.add(SwingViewSupport.card("월별 이슈 발생 수", new LineChartPanel(monthlyCounts(projectId))));
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        page.add(scrollPane, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel summary(AppControllers controllers, Long projectId) {
        JPanel panel = SwingViewSupport.strip();
        panel.setLayout(new GridLayout(0, 1, 0, 10));
        long statusTotal = controllers.statistics().countIssuesByStatus(projectId).values().stream().mapToLong(Long::longValue).sum();
        long priorityTotal = controllers.statistics().countIssuesByPriority(projectId).values().stream().mapToLong(Long::longValue).sum();
        panel.add(label("전체 이슈: " + statusTotal));
        panel.add(label("우선순위 집계: " + priorityTotal));
        panel.add(label("개발자 Fix 집계: " + controllers.statistics().countFixedIssuesByDeveloper(projectId).values().stream().mapToLong(Long::longValue).sum()));
        return panel;
    }

    private Map<String, Long> dailyCounts(Long projectId) {
        YearMonth month = YearMonth.now();
        Map<LocalDate, Long> raw = controllers.statistics().countIssuesByDay(projectId, month);
        Map<String, Long> result = new LinkedHashMap<>();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            result.put(month.getMonthValue() + "/" + day, raw.getOrDefault(date, 0L));
        }
        return result;
    }

    private Map<String, Long> monthlyCounts(Long projectId) {
        int year = YearMonth.now().getYear();
        Map<YearMonth, Long> raw = controllers.statistics().countIssuesByMonth(projectId, year);
        Map<String, Long> result = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth ym = YearMonth.of(year, month);
            result.put(month + "월", raw.getOrDefault(ym, 0L));
        }
        return result;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(issuetracker.ui.swing.layout.MainFrame.TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return label;
    }

    private static final class PieChartPanel extends JPanel {
        private final List<Map.Entry<?, Long>> entries;
        private double progress;
        private final Color[] colors = {
                new Color(109, 166, 255),
                new Color(106, 195, 106),
                new Color(255, 208, 82),
                new Color(239, 119, 114),
                new Color(194, 143, 255),
                new Color(237, 123, 54)
        };

        private PieChartPanel(Map<?, Long> values) {
            this.entries = new ArrayList<>(values.entrySet());
            setOpaque(false);
            setPreferredSize(new Dimension(320, 240));
            animateIn();
        }

        private void animateIn() {
            Timer timer = new Timer(16, null);
            timer.addActionListener(event -> {
                progress = Math.min(1.0, progress + 0.045);
                repaint();
                if (progress >= 1.0) {
                    timer.stop();
                }
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            long total = entries.stream().mapToLong(Map.Entry::getValue).sum();
            int size = Math.min(getWidth() - 150, getHeight() - 30);
            int x = 16;
            int y = 16;
            if (total == 0) {
                g2.setColor(new Color(48, 55, 67));
                g2.fillOval(x, y, size, size);
            } else {
                int start = 90;
                for (int i = 0; i < entries.size(); i++) {
                    int angle = (int) Math.round(entries.get(i).getValue() * 360.0 / total * progress);
                    g2.setColor(colors[i % colors.length]);
                    g2.fillArc(x, y, size, size, start, -angle);
                    start -= angle;
                }
            }
            int legendX = x + size + 24;
            int legendY = y + 8;
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            for (int i = 0; i < entries.size(); i++) {
                g2.setColor(colors[i % colors.length]);
                g2.fillRoundRect(legendX, legendY + i * 24, 10, 10, 8, 8);
                g2.setColor(issuetracker.ui.swing.layout.MainFrame.TEXT);
                g2.drawString(entries.get(i).getKey() + "  " + entries.get(i).getValue(), legendX + 18, legendY + 10 + i * 24);
            }
            g2.dispose();
        }
    }

    private static final class BarChartPanel extends JPanel {
        private final List<Map.Entry<?, Long>> entries;
        private double progress;

        private BarChartPanel(Map<?, Long> values) {
            this.entries = new ArrayList<>(values.entrySet());
            setOpaque(false);
            setPreferredSize(new Dimension(320, 240));
            animateIn();
        }
        private void animateIn() {
            Timer timer = new Timer(16, null);
            timer.addActionListener(event -> {
                progress = Math.min(1.0, progress + 0.05);
                repaint();
                if (progress >= 1.0) {
                    timer.stop();
                }
            });
            timer.start();
        }
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            long max = entries.stream().mapToLong(Map.Entry::getValue).max().orElse(1);
            int left = 42;
            int bottom = getHeight() - 36;
            int chartHeight = Math.max(60, getHeight() - 78);
            int barWidth = entries.isEmpty() ? 28 : Math.max(24, (getWidth() - 80) / Math.max(entries.size(), 1) - 14);
            g2.setColor(new Color(48, 55, 67));
            g2.drawLine(left, bottom - chartHeight, left, bottom);
            g2.drawLine(left, bottom, getWidth() - 24, bottom);
            for (int i = 0; i < entries.size(); i++) {
                long value = entries.get(i).getValue();
                int height = max == 0 ? 0 : (int) Math.round(value * (chartHeight - 12) / (double) max * progress);
                int x = left + 18 + i * (barWidth + 14);
                int y = bottom - height;
                g2.setColor(new Color(109, 166, 255));
                g2.fillRoundRect(x, y, barWidth, height, 8, 8);
                g2.setColor(issuetracker.ui.swing.layout.MainFrame.TEXT);
                String valueText = String.valueOf(value);
                int valueX = x + (barWidth - g2.getFontMetrics().stringWidth(valueText)) / 2;
                g2.drawString(valueText, valueX, y - 6);
                g2.setColor(issuetracker.ui.swing.layout.MainFrame.MUTED);
                String label = String.valueOf(entries.get(i).getKey());
                int labelX = x + (barWidth - g2.getFontMetrics().stringWidth(label)) / 2;
                g2.drawString(label, labelX, bottom + 18);
            }
            g2.dispose();
        }
    }
    private static final class LineChartPanel extends JPanel {
        private final List<Map.Entry<String, Long>> entries;

        private LineChartPanel(Map<String, Long> values) {
            this.entries = new ArrayList<>(values.entrySet());
            setOpaque(false);
            setPreferredSize(new Dimension(900, 360));
        }
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 54, top = 34, right = getWidth() - 28, bottom = getHeight() - 52;
            long max = Math.max(1, entries.stream().mapToLong(Map.Entry::getValue).max().orElse(1));
            int gap = Math.max(1, entries.size() - 1);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
            for (int i = 0; i <= 5; i++) {
                int y = bottom - i * (bottom - top) / 5;
                g2.setColor(new Color(48, 55, 67));
                g2.drawLine(left, y, right, y);
                g2.setColor(issuetracker.ui.swing.layout.MainFrame.TEXT);
                g2.drawString(String.valueOf(max * i / 5), left - 28, y + 4);
            }
            for (int i = 0; i < entries.size(); i++) {
                int x = left + i * (right - left) / gap;
                g2.setColor(new Color(48, 55, 67));
                g2.drawLine(x, top, x, bottom);

                if (entries.size() <= 12 || i % 2 == 0) {
                    drawCentered(g2, entries.get(i).getKey(), x - 24, bottom + 22, 48,
                            issuetracker.ui.swing.layout.MainFrame.MUTED);
                }
            }
            g2.setColor(new Color(210, 216, 226));
            g2.drawLine(left, bottom, right, bottom);
            int prevX = -1;
            int prevY = -1;
            g2.setStroke(new BasicStroke(3f));
            for (int i = 0; i < entries.size(); i++) {
                long value = entries.get(i).getValue();
                int x = left + i * (right - left) / gap;
                int y = bottom - (int) Math.round(value * (bottom - top) / (double) max);
                g2.setColor(new Color(255, 112, 55));
                if (prevX >= 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 6, y - 6, 12, 12);
                g2.setColor(new Color(255, 112, 55));
                g2.fillOval(x - 4, y - 4, 8, 8);
                prevX = x;
                prevY = y;
            }
            g2.dispose();
        }
    }

    private static void drawCentered(Graphics2D g2, String text, int x, int y, int width, Color color) {
        g2.setColor(color);
        int textX = x + (width - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, textX, y);
    }
}