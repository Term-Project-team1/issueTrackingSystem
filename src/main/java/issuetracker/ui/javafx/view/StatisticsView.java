package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.LinkedHashMap;
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

        Label title = new Label("Statistics");
        title.getStyleClass().add("page-title");

        Label description = new Label(createDescriptionText());
        description.getStyleClass().add("page-description");
        description.setWrapText(true);

        VBox header = new VBox(6, title, description);

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);

        grid.add(createStatusPiePanel(), 0, 0);
        grid.add(createPriorityPiePanel(), 1, 0);
        grid.add(createDeveloperChartPanel(), 0, 1);
        grid.add(createRecommendationPanel(), 1, 1);

        VBox.setVgrow(grid, Priority.ALWAYS);

        getChildren().addAll(header, grid);
    }

    private String createDescriptionText() {
        if (projectId() == null) {
            return "프로젝트가 없습니다. 그래프는 빈 상태로 표시됩니다. Projects 화면에서 admin 계정으로 project1을 생성하면 통계가 채워집니다.";
        }

        return "선택된 프로젝트의 이슈 통계와 추천 후보를 확인합니다.";
    }

    private VBox createStatusPiePanel() {
        Map<IssueStatus, Long> values = getStatusCounts();

        PieChart chart = new PieChart(FXCollections.observableArrayList(
                values.entrySet()
                        .stream()
                        .map(entry -> new PieChart.Data(entry.getKey().name(), entry.getValue()))
                        .toList()
        ));

        chart.setLabelsVisible(false);
        chart.setLegendVisible(true);

        VBox content = new VBox(10, chart, emptyGuide("상태별 이슈 데이터가 없습니다."));
        VBox.setVgrow(chart, Priority.ALWAYS);

        return createPanel("상태별 이슈 수", content);
    }

    private VBox createPriorityPiePanel() {
        Map<issuetracker.domain.issue.Priority, Long> values = getPriorityCounts();

        PieChart chart = new PieChart(FXCollections.observableArrayList(
                values.entrySet()
                        .stream()
                        .map(entry -> new PieChart.Data(entry.getKey().name(), entry.getValue()))
                        .toList()
        ));

        chart.setLabelsVisible(false);
        chart.setLegendVisible(true);

        VBox content = new VBox(10, chart, emptyGuide("우선순위별 이슈 데이터가 없습니다."));
        VBox.setVgrow(chart, Priority.ALWAYS);

        return createPanel("우선순위별 이슈 수", content);
    }

    private VBox createDeveloperChartPanel() {
        Map<String, Long> fixedCounts = getFixedCountsByDeveloperName();

        VBox barArea = new VBox(12);
        barArea.setPadding(new Insets(8, 0, 8, 0));

        long maxCount = fixedCounts.values()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        if (maxCount == 0) {
            Label empty = emptyGuide("아직 FIXED 처리된 이슈가 없습니다.");
            barArea.getChildren().add(empty);
        } else {
            for (Map.Entry<String, Long> entry : fixedCounts.entrySet()) {
                if (entry.getValue() > 0) {
                    barArea.getChildren().add(createDeveloperBar(entry.getKey(), entry.getValue(), maxCount));
                }
            }

            barArea.getChildren().add(emptyGuide("현재 FIXED 상태인 이슈를 개발자별로 표시합니다."));
        }

        return createPanel("개발자별 FIXED 개수", barArea);
    }

    private HBox createDeveloperBar(String developerName, long count, long maxCount) {
        Label nameLabel = new Label(developerName);
        nameLabel.getStyleClass().add("field-label");
        nameLabel.setMinWidth(80);

        Region bar = new Region();
        bar.getStyleClass().add("fixed-bar");

        double width = 260.0;

        if (maxCount > 0) {
            width = Math.max(80.0, 300.0 * count / maxCount);
        }

        bar.setPrefWidth(width);
        bar.setMinWidth(width);
        bar.setMaxWidth(width);
        bar.setPrefHeight(24);

        StackPane barBox = new StackPane(bar);
        barBox.setAlignment(Pos.CENTER_LEFT);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("detail-value");
        countLabel.setMinWidth(30);

        HBox row = new HBox(10, nameLabel, barBox, countLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    private VBox createRecommendationPanel() {
        List<Issue> allIssues = getIssues();

        ComboBox<Issue> issueBox = new ComboBox<>(FXCollections.observableArrayList(allIssues));
        issueBox.setPromptText("issue");
        issueBox.setMaxWidth(Double.MAX_VALUE);

        issueBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Issue issue) {
                if (issue == null) {
                    return "";
                }

                return "#" + issue.getId() + " " + safe(issue.getTitle());
            }

            @Override
            public Issue fromString(String string) {
                return null;
            }
        });

        allIssues.stream()
                .filter(issue -> issue.getStatus() == IssueStatus.NEW)
                .findFirst()
                .ifPresent(issueBox::setValue);

        if (issueBox.getValue() == null && !allIssues.isEmpty()) {
            issueBox.setValue(allIssues.get(0));
        }

        ListView<String> results = new ListView<>();
        results.getStyleClass().add("data-list");

        Button recommend = new Button("Show Candidates");
        recommend.getStyleClass().add("primary-button");

        recommend.setOnAction(event -> {
            Issue issue = issueBox.getValue();

            if (issue == null) {
                results.getItems().setAll("추천할 이슈가 없습니다.");
                return;
            }

            try {
                List<String> candidateNames = controllers.recommendation()
                        .recommendAssignees(issue, 3)
                        .stream()
                        .map(UiFormat::account)
                        .toList();

                if (candidateNames.isEmpty()) {
                    results.getItems().setAll("추천 후보가 없습니다.");
                    return;
                }

                results.getItems().setAll(candidateNames);

            } catch (Exception e) {
                results.getItems().setAll("추천 조회 실패: " + e.getMessage());
                e.printStackTrace();
            }
        });

        if (!allIssues.isEmpty()) {
            recommend.fire();
        } else if (projectId() == null) {
            results.getItems().setAll("프로젝트 생성 후 이슈를 만들면 추천 후보를 볼 수 있습니다.");
        } else {
            results.getItems().setAll("아직 등록된 이슈가 없습니다.");
        }

        HBox form = new HBox(10, issueBox, recommend);
        form.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(issueBox, Priority.ALWAYS);

        VBox content = new VBox(12, form, results);
        VBox.setVgrow(results, Priority.ALWAYS);

        return createPanel("추천 후보 표시", content);
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

    private Map<issuetracker.domain.issue.Priority, Long> getPriorityCounts() {
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

    private Map<String, Long> getFixedCountsByDeveloperName() {
        Map<String, Long> counts = new LinkedHashMap<>();

        try {
            for (Account developer : controllers.account().findByRole(Role.DEV)) {
                counts.put(UiFormat.username(developer), 0L);
            }

            for (Issue issueSummary : getIssues()) {
                if (issueSummary == null || issueSummary.getId() == null) {
                    continue;
                }

                Issue detailIssue;

                try {
                    detailIssue = controllers.issue().viewIssue(issueSummary.getId());
                } catch (Exception e) {
                    detailIssue = issueSummary;
                }

                if (detailIssue == null) {
                    continue;
                }

                if (detailIssue.getStatus() != IssueStatus.FIXED) {
                    continue;
                }

                Account fixer = detailIssue.getFixer();

                if (fixer == null) {
                    fixer = detailIssue.getAssignee();
                }

                if (fixer == null) {
                    continue;
                }

                String fixerName = UiFormat.username(fixer);
                counts.put(fixerName, counts.getOrDefault(fixerName, 0L) + 1);
            }

            return counts;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private List<Issue> getIssues() {
        Long projectId = projectId();

        if (projectId == null) {
            return Collections.emptyList();
        }

        try {
            return controllers.search().searchIssues(projectId, null, null, null, "");
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

    private Label emptyGuide(String message) {
        Label guide = new Label(message);

        if (projectId() == null) {
            guide.setText(message + " Projects 화면에서 project1을 생성하면 데이터가 표시됩니다.");
        }

        guide.getStyleClass().add("field-label");
        guide.setWrapText(true);

        return guide;
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
        panel.setMinWidth(420);

        VBox.setVgrow(content, Priority.ALWAYS);

        return panel;
    }
}