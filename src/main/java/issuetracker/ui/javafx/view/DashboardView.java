package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class DashboardView extends VBox {

    private final AppControllers controllers;
    private final UiSession session;

    public DashboardView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;

        getStyleClass().add("content");
        setSpacing(22);
        setPadding(new Insets(28));

        getChildren().addAll(createHeader(), createSummaryCards(), createMainArea());
    }

    private VBox createHeader() {
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        Label description = new Label(createDescriptionText());
        description.getStyleClass().add("page-description");

        return new VBox(6, title, description);
    }

    private String createDescriptionText() {
        if (projectId() == null) {
            return "프로젝트가 없습니다. Projects 화면에서 admin 계정으로 project1을 먼저 생성하세요.";
        }

        return "Issue Tracking System의 이슈 현황과 통계를 확인합니다.";
    }

    private GridPane createSummaryCards() {
        Map<IssueStatus, Long> statusCounts = getStatusCounts();

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createMetricCard("Total Issues", String.valueOf(countAll(statusCounts)), "전체 등록 이슈"), 0, 0);
        grid.add(createMetricCard("NEW", count(statusCounts, IssueStatus.NEW), "생성됨"), 1, 0);
        grid.add(createMetricCard("ASSIGNED", count(statusCounts, IssueStatus.ASSIGNED), "담당자 지정"), 2, 0);
        grid.add(createMetricCard("FIXED", count(statusCounts, IssueStatus.FIXED), "수정 완료"), 3, 0);
        grid.add(createMetricCard("RESOLVED", count(statusCounts, IssueStatus.RESOLVED), "검증 완료"), 4, 0);
        grid.add(createMetricCard("CLOSED", count(statusCounts, IssueStatus.CLOSED), "종료"), 5, 0);

        return grid;
    }

    private VBox createMetricCard(String label, String value, String caption) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("metric-value");

        Label captionNode = new Label(caption);
        captionNode.getStyleClass().add("metric-caption");

        VBox card = new VBox(8, labelNode, valueNode, captionNode);
        card.getStyleClass().add("metric-card");
        card.setMinWidth(150);

        return card;
    }

    private HBox createMainArea() {
        VBox recentPanel = createPanel("최근 이슈 목록", createRecentIssueTable());
        HBox.setHgrow(recentPanel, javafx.scene.layout.Priority.ALWAYS);

        PieChart pieChart = new PieChart(FXCollections.observableArrayList(
                getPriorityCounts()
                        .entrySet()
                        .stream()
                        .map(entry -> new PieChart.Data(entry.getKey().name(), entry.getValue()))
                        .toList()
        ));

        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(false);

        VBox chartPanel = createPanel("우선순위 분포", pieChart);
        chartPanel.setPrefWidth(420);

        HBox box = new HBox(18, recentPanel, chartPanel);
        box.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(box, javafx.scene.layout.Priority.ALWAYS);

        return box;
    }

    private TableView<Issue> createRecentIssueTable() {
        List<Issue> recentIssues = getRecentIssues();

        TableView<Issue> table = new TableView<>(FXCollections.observableArrayList(recentIssues));
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().add(longColumn("ID", issue -> issue.getId() != null ? issue.getId() : 0L, 90));
        table.getColumns().add(column("Title", issue -> safe(issue.getTitle()), 320));
        table.getColumns().add(column("Status", issue -> issue.getStatus() != null ? issue.getStatus().name() : "-", 120));
        table.getColumns().add(column("Priority", issue -> issue.getPriority() != null ? issue.getPriority().name() : "-", 110));
        table.getColumns().add(column("Project", issue -> UiFormat.project(issue.getProject()), 140));
        table.getColumns().add(column("Assignee", issue -> UiFormat.username(issue.getAssignee()), 120));

        return table;
    }

    private Map<IssueStatus, Long> getStatusCounts() {
        Long projectId = projectId();

        if (projectId == null) {
            return Collections.emptyMap();
        }

        try {
            return controllers.statistics().countIssuesByStatus(projectId);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private Map<Priority, Long> getPriorityCounts() {
        Long projectId = projectId();

        if (projectId == null) {
            return Collections.emptyMap();
        }

        try {
            return controllers.statistics().countIssuesByPriority(projectId);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private List<Issue> getRecentIssues() {
        Long projectId = projectId();

        if (projectId == null) {
            return Collections.emptyList();
        }

        try {
            return controllers.search()
                    .searchIssues(projectId, null, null, null, "")
                    .stream()
                    .limit(10)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Long projectId() {
        return session.getSelectedProject() == null
                ? null
                : session.getSelectedProject().getId();
    }

    private TableColumn<Issue, Number> longColumn(String title,
                                                  java.util.function.ToLongFunction<Issue> mapper,
                                                  double width) {
        TableColumn<Issue, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleLongProperty(mapper.applyAsLong(cell.getValue())));
        column.setPrefWidth(width);

        return column;
    }

    private TableColumn<Issue, String> column(String title,
                                              java.util.function.Function<Issue, String> mapper,
                                              double width) {
        TableColumn<Issue, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        column.setPrefWidth(width);

        return column;
    }

    private String count(Map<IssueStatus, Long> counts, IssueStatus status) {
        return String.valueOf(counts.getOrDefault(status, 0L));
    }

    private long countAll(Map<IssueStatus, Long> counts) {
        return counts.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    private VBox createPanel(String title, javafx.scene.Node content) {
        Label titleNode = new Label(title);
        titleNode.getStyleClass().add("panel-title");

        VBox panel = new VBox(14, titleNode, content);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));

        VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);

        return panel;
    }
}