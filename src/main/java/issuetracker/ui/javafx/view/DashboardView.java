package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DashboardView extends VBox {
    private final AppControllers controllers;
    private final UiSession session;

    public DashboardView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;

        getStyleClass().add("content");
        setSpacing(22);
        setPadding(new Insets(28));

        getChildren().addAll(header(), summaryCards(), mainArea());
    }

    private VBox header() {
        Label title = label("Dashboard", "page-title");
        Label description = label(
                projectId() == null ? "프로젝트가 없습니다. Projects 화면에서 프로젝트를 생성하세요." : "Issue Tracking System의 이슈 현황과 통계를 확인합니다.", "page-description"
        );
        return new VBox(6, title, description);
    }

    private GridPane summaryCards() {
        Map<IssueStatus, Long> counts = statusCounts();
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        Object[][] cards = {
                {"Total Issues", String.valueOf(total(counts)), "전체 등록 이슈"},
                {"NEW", count(counts, IssueStatus.NEW), "생성됨"},
                {"ASSIGNED", count(counts, IssueStatus.ASSIGNED), "담당자 지정"},
                {"FIXED", count(counts, IssueStatus.FIXED), "수정 완료"},
                {"RESOLVED", count(counts, IssueStatus.RESOLVED), "검증 완료"},
                {"CLOSED", count(counts, IssueStatus.CLOSED), "종료"}
        };

        for (int i = 0; i < cards.length; i++) {
            grid.add(metric((String) cards[i][0], (String) cards[i][1], (String) cards[i][2]), i, 0);
        }
        return grid;
    }

    private VBox metric(String title, String value, String caption) {
        VBox card = new VBox(8, label(title, "metric-label"), label(value, "metric-value"), label(caption, "metric-caption"));
        card.getStyleClass().add("metric-card");
        card.setMinWidth(150);
        return card;
    }

    private HBox mainArea() {
        VBox recent = panel("최근 이슈 목록", recentTable());
        HBox.setHgrow(recent, javafx.scene.layout.Priority.ALWAYS);
        PieChart chart = priorityChart();
        VBox chartPanel = panel("우선순위 분포", chart);
        chartPanel.setPrefWidth(420);
        HBox box = new HBox(18, recent, chartPanel);
        box.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(box, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private TableView<Issue> recentTable() {
        TableView<Issue> table = new TableView<>(FXCollections.observableArrayList(recentIssues()));
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(List.of(
                column("ID", issue -> issue.getId() == null ? "0" : String.valueOf(issue.getId()), 90),
                column("Title", issue -> safe(issue.getTitle()), 320),
                column("Status", issue -> issue.getStatus() == null ? "-" : issue.getStatus().name(), 120),
                column("Priority", issue -> issue.getPriority() == null ? "-" : issue.getPriority().name(), 110),
                column("Project", issue -> UiFormat.project(issue.getProject()), 140),
                column("Assignee", issue -> UiFormat.username(issue.getAssignee()), 120)
        ));
        return table;
    }

    private PieChart priorityChart() {
        PieChart chart = new PieChart(FXCollections.observableArrayList(
                priorityCounts().entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue() > 0).map(entry -> new PieChart.Data(entry.getKey().name(), entry.getValue())).toList()
        ));

        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        return chart;
    }

    private Map<IssueStatus, Long> statusCounts() {
        if (projectId() == null) {
            return Collections.emptyMap();
        }

        try {
            return controllers.statistics().countIssuesByStatus(projectId());
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private Map<Priority, Long> priorityCounts() {
        if (projectId() == null) {
            return Collections.emptyMap();
        }

        try {
            return controllers.statistics().countIssuesByPriority(projectId());
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private List<Issue> recentIssues() {
        if (projectId() == null) {
            return Collections.emptyList();
        }

        try {
            return controllers.search().searchIssues(projectId(), null, null, null, "").stream().limit(10).toList();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private Long projectId() {
        return session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
    }

    private String count(Map<IssueStatus, Long> counts, IssueStatus status) {
        return String.valueOf(counts.getOrDefault(status, 0L));
    }

    private long total(Map<IssueStatus, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }

    private TableColumn<Issue, String> column(String title, Function<Issue, String> text, double width) {
        TableColumn<Issue, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(text.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private VBox panel(String title, Node content) {
        VBox panel = new VBox(14, label(title, "panel-title"), content);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));
        VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);
        return panel;
    }
}