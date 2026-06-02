package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatisticsView extends VBox {
    private final AppControllers controllers;
    private final UiSession session;

    public StatisticsView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;

        getStyleClass().add("content");
        setSpacing(18);
        setPadding(new Insets(28));

        Label title = label("Statistics", "page-title");
        Label description = label(descriptionText(), "page-description");
        description.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.add(piePanel("상태별 이슈 수", statusCounts()), 0, 0);
        grid.add(piePanel("우선순위별 이슈 수", priorityCounts()), 1, 0);
        grid.add(developerPanel(), 0, 1);
        grid.add(recommendPanel(), 1, 1);
        grid.add(linePanel("일별 이슈 발생 수", dailyCounts()), 0, 2, 2, 1);
        grid.add(monthlyPanel(), 0, 3, 2, 1);
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(new VBox(6, title, description), scroll);
    }

    private String descriptionText() {
        return projectId() == null ? "프로젝트가 없습니다. 그래프는 빈 상태로 표시됩니다." : "선택한 프로젝트의 이슈 통계와 추천 후보를 확인합니다.";
    }

    private VBox piePanel(String title, Map<? extends Enum<?>, Long> values) {
        PieChart chart = new PieChart(FXCollections.observableArrayList(values.entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue() > 0).map(entry -> new PieChart.Data(entry.getKey().name() + " (" + entry.getValue() + ")", entry.getValue())).toList()));
        chart.setLabelsVisible(false);
        chart.setLegendVisible(false);

        VBox content = new VBox(10, chart, customLegend(values));
        VBox.setVgrow(chart, Priority.ALWAYS);

        if (empty(values)) {
            content.getChildren().add(guide("표시할 이슈 데이터가 없습니다."));
        }
        return panel(title, content);
    }

    private VBox developerPanel() {
        Map<Account, Long> values = projectId() == null ? Collections.emptyMap() : controllers.statistics().countFixedIssuesByDeveloper(projectId());

        VBox rows = new VBox(12);
        rows.setPadding(new Insets(8, 0, 8, 0));
        long max = max(values);

        if (max == 0) {
            rows.getChildren().add(guide("아직 FIXED 처리된 이슈가 없습니다."));
        } else {
            values.entrySet().stream().filter(entry -> entry.getValue() != null && entry.getValue() > 0).forEach(entry -> rows.getChildren().add(developerBar(UiFormat.username(entry.getKey()), entry.getValue(), max)));
        }
        return panel("개발자별 FIXED 개수", rows);
    }

    private HBox developerBar(String name, long count, long max) {
        Label nameLabel = label(name, "field-label");
        nameLabel.setMinWidth(80);

        Region bar = new Region();
        bar.getStyleClass().add("fixed-bar");
        double width = Math.max(80.0, 300.0 * count / max);
        bar.setPrefWidth(width);
        bar.setMinWidth(width);
        bar.setMaxWidth(width);
        bar.setPrefHeight(24);

        StackPane barBox = new StackPane(bar);
        barBox.setAlignment(Pos.CENTER_LEFT);

        Label countLabel = label(String.valueOf(count), "detail-value");
        countLabel.setMinWidth(30);

        HBox row = new HBox(10, nameLabel, barBox, countLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox recommendPanel() {
        List<Issue> issues = issues();
        ComboBox<Issue> issueBox = new ComboBox<>(FXCollections.observableArrayList(issues));
        issueBox.setPromptText("issue");
        issueBox.setMaxWidth(Double.MAX_VALUE);
        issueBox.setConverter(issueConverter());
        issues.stream().filter(issue -> issue.getStatus() == IssueStatus.NEW).findFirst().or(() -> issues.stream().findFirst()).ifPresent(issueBox::setValue);
        ListView<String> results = new ListView<>();
        results.getStyleClass().add("data-list");
        Button button = new Button("Show Candidates");
        button.getStyleClass().add("primary-button");
        button.setOnAction(event -> {
            Issue issue = issueBox.getValue();

            if (issue == null) {
                results.getItems().setAll("추천할 이슈가 없습니다.");
                return;
            }

            try {
                List<String> names = controllers.recommendation().recommendAssignees(issue, 3).stream().map(UiFormat::account).toList();results.getItems().setAll(names.isEmpty() ? List.of("추천 후보가 없습니다.") : names);
            } catch (Exception ignored) {
            }
        });

        if (!issues.isEmpty()) {
            button.fire();
        } else {
            results.getItems().setAll("등록된 이슈가 없습니다.");
        }

        HBox form = new HBox(10, issueBox, button);
        form.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(issueBox, Priority.ALWAYS);
        VBox content = new VBox(12, form, results);
        VBox.setVgrow(results, Priority.ALWAYS);
        return panel("추천 후보 표시", content);
    }

    private VBox linePanel(String title, Map<LocalDate, Long> values) {
        LineChart<String, Number> chart = lineChart();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        values.entrySet().stream().sorted(Map.Entry.comparingByKey()).filter(entry -> entry.getValue() != null).forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey().getMonthValue() + "/" + entry.getKey().getDayOfMonth(), entry.getValue())));
        chart.getData().add(series);
        adjustAxis((NumberAxis) chart.getYAxis(), max(values));
        VBox content = new VBox(10, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);

        if (empty(values)) {
            content.getChildren().add(guide("표시할 일별 데이터가 없습니다."));
        }
        return panel(title, content);
    }

    private VBox monthlyPanel() {
        Map<YearMonth, Long> values = monthlyCounts();
        LineChart<String, Number> chart = lineChart();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        values.entrySet().stream().sorted(Map.Entry.comparingByKey()).filter(entry -> entry.getValue() != null).forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey().getMonthValue() + "월", entry.getValue())));
        chart.getData().add(series);
        adjustAxis((NumberAxis) chart.getYAxis(), max(values));
        VBox content = new VBox(10, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);

        if (empty(values)) {
            content.getChildren().add(guide("표시할 월별 데이터가 없습니다."));
        }
        return panel("월별 이슈 발생 수", content);
    }

    private LineChart<String, Number> lineChart() {
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        return chart;
    }

    private Map<IssueStatus, Long> statusCounts() {
        return projectId() == null ? Collections.emptyMap() : controllers.statistics().countIssuesByStatus(projectId());
    }

    private Map<issuetracker.domain.issue.Priority, Long> priorityCounts() {
        return projectId() == null ? Collections.emptyMap() : controllers.statistics().countIssuesByPriority(projectId());
    }

    private Map<LocalDate, Long> dailyCounts() {
        return projectId() == null ? Collections.emptyMap() : controllers.statistics().countIssuesByDay(projectId(), YearMonth.now());
    }

    private Map<YearMonth, Long> monthlyCounts() {
        return projectId() == null ? Collections.emptyMap() : controllers.statistics().countIssuesByMonth(projectId(), YearMonth.now().getYear());
    }

    private List<Issue> issues() {
        return projectId() == null ? Collections.emptyList() : controllers.search().searchIssues(projectId(), null, null, null, "");
    }

    private Long projectId() {
        return session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
    }

    private void adjustAxis(NumberAxis axis, long max) {
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(Math.max(1, max) + 1);
        axis.setTickUnit(1);
        axis.setMinorTickVisible(false);
    }

    private long max(Map<?, Long> values) {
        return values.values().stream().filter(value -> value != null).mapToLong(Long::longValue).max().orElse(0);
    }

    private boolean empty(Map<?, Long> values) {
        return values.isEmpty() || values.values().stream().allMatch(value -> value == null || value == 0);
    }

    private Label guide(String text) {
        Label label = label(text, "field-label");
        label.setWrapText(true);
        return label;
    }

    private Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }

    private VBox panel(String title, Node content) {
        VBox panel = new VBox(14, label(title, "panel-title"), content);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));
        panel.setMinWidth(420);
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private javafx.util.StringConverter<Issue> issueConverter() {
        return new javafx.util.StringConverter<>() {
            public String toString(Issue issue) {
                return issue == null ? "" : "#" + issue.getId() + " " + (issue.getTitle() == null ? "-" : issue.getTitle());
            }
            public Issue fromString(String string) {
                return null;
            }
        };
    }

    private HBox customLegend(Map<? extends Enum<?>, Long> values) {
        HBox legend = new HBox(16);
        legend.setAlignment(Pos.CENTER);
        int index = 0;
        for (Map.Entry<? extends Enum<?>, Long> entry : values.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            Region dot = new Region();
            dot.getStyleClass().add("legend-dot");
            dot.getStyleClass().add("legend-color" + index);
            Label text = label(entry.getKey().name() + " (" + entry.getValue() + ")", "legend-text");
            HBox item = new HBox(6, dot, text);
            item.setAlignment(Pos.CENTER);
            legend.getChildren().add(item);
            index++;
        }
        return legend;
    }

    private String legendColor(String name) {
        return switch (name) {
            case "NEW", "BLOCKER" -> "legend-blue";
            case "ASSIGNED", "CRITICAL" -> "legend-green";
            case "FIXED", "MAJOR" -> "legend-yellow";
            case "RESOLVED", "MINOR" -> "legend-red";
            case "CLOSED", "TRIVIAL" -> "legend-purple";
            default -> "legend-cyan";
        };
    }
}