package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.function.Function;

public class IssueView extends VBox {
    private final AppControllers controllers;
    private final UiSession session;
    private final ObservableList<Issue> issues = FXCollections.observableArrayList();
    private final IssueDetailView detailView;
    private TableView<Issue> table;
    private TextField titleField, descriptionField, keywordField;
    private ComboBox<Priority> priorityBox;
    private ComboBox<String> statusBox;
    private ComboBox<Account> reporterBox, assigneeBox;
    private Button createButton;
    private Label message;

    public IssueView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;
        detailView = new IssueDetailView(controllers, session, this::refreshSelected);
        getStyleClass().add("content");
        setSpacing(18);
        setPadding(new Insets(28));

        HBox main = new HBox(20, issueTable(), detailPane());
        VBox.setVgrow(main, javafx.scene.layout.Priority.ALWAYS);
        message = label("", "field-label");
        getChildren().addAll(label("Issues", "page-title"), issueForm(), searchBar(), main, message);

        session.addProjectListener(() -> {
            loadFilters();
            detailView.loadAssignees();
            refreshIssues();
            updateCreateButton();
        });
        loadFilters();
        refreshIssues();
        updateCreateButton();
    }

    private VBox issueForm() {
        titleField = textField("issue title");
        descriptionField = textField("description");
        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(Priority.MAJOR);
        createButton = button("Create Issue", "primary-button", e -> createIssue());
        HBox row = row(titleField, descriptionField, priorityBox, createButton);
        HBox.setHgrow(titleField, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(descriptionField, javafx.scene.layout.Priority.ALWAYS);
        return new VBox(row);
    }

    private VBox searchBar() {
        statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ALL", "NEW", "ASSIGNED", "FIXED", "RESOLVED", "CLOSED", "REOPENED");
        statusBox.setValue("ALL");
        reporterBox = accountBox("ALL");
        assigneeBox = accountBox("ALL");
        keywordField = textField("keyword");
        HBox row = row(statusBox, reporterBox, assigneeBox, keywordField,
                button("Search", "secondary-button", e -> refreshIssues()),
                button("Reload", "secondary-button", e -> refreshIssues()));
        HBox.setHgrow(keywordField, javafx.scene.layout.Priority.ALWAYS);
        return new VBox(row);
    }

    private VBox issueTable() {
        table = new TableView<>(issues);
        table.getStyleClass().add("data-table");
        table.getColumns().addAll(List.of(
                column("ID", 80, issue -> String.valueOf(issue.getId())),
                column("Title", 220, issue -> safe(issue.getTitle())),
                column("Status", 120, issue -> name(issue.getStatus())),
                column("Priority", 120, issue -> name(issue.getPriority())),
                column("Reporter", 140, issue -> UiFormat.username(issue.getReporter())),
                column("Assignee", 140, issue -> UiFormat.username(issue.getAssignee())),
                column("Fixer", 140, issue -> UiFormat.username(issue.getFixer()))));
        table.getSelectionModel().selectedItemProperty().addListener((o, old, issue) -> detailView.showIssue(issue));
        VBox panel = new VBox(14, label("Issue List", "panel-title"), table);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));
        HBox.setHgrow(panel, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);
        return panel;
    }

    private ScrollPane detailPane() {
        ScrollPane pane = new ScrollPane(detailView);
        pane.setFitToWidth(true);
        pane.setPrefWidth(380);
        pane.setMinWidth(360);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return pane;
    }

    private void loadFilters() {
        reporterBox.getItems().clear();
        assigneeBox.getItems().clear();
        reporterBox.getItems().add(null);
        assigneeBox.getItems().add(null);
        try {
            reporterBox.getItems().addAll(controllers.account().findByRole(Role.TESTER));
            assigneeBox.getItems().addAll(controllers.account().findByRole(Role.DEV));
            reporterBox.setValue(null);
            assigneeBox.setValue(null);
        } catch (Exception e) {
            show("Failed to load accounts: " + e.getMessage());
        }
    }

    private void createIssue() {
        Account reporter = session.getCurrentUser();
        String title = titleField.getText();
        if (title == null || title.isBlank() || reporter == null) return;

        if (!canCreate(reporter)) {
            show("이슈 생성은 TESTER 또는 ADMIN만 가능합니다.");
            return;
        }
        Project project = selectedProject();

        if (project == null) {
            show("프로젝트가 없습니다. 먼저 Project 페이지에서 프로젝트를 생성하세요.");
            return;
        }
        try {
            Issue issue = controllers.issue().createIssue(project, reporter, title, descriptionField.getText(), priorityBox.getValue());
            session.rememberCreatedIssue(issue);
            session.selectProject(project);
            titleField.clear();
            descriptionField.clear();
            refreshIssues(issue.getId());
            show("Issue created.");
        } catch (Exception e) {
            show("Failed to create issue: " + e.getMessage());
        }
    }

    private void refreshIssues() {
        refreshIssues(null);
    }

    private void refreshSelected() {
        Issue issue = detailView.getIssue();
        refreshIssues(issue == null ? null : issue.getId());
    }

    private void refreshIssues(Long selectedId) {
        try {
            Long projectId = session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
            IssueStatus status = parseStatus(statusBox == null ? null : statusBox.getValue());
            Long reporterId = id(reporterBox == null ? null : reporterBox.getValue());
            Long assigneeId = id(assigneeBox == null ? null : assigneeBox.getValue());
            String keyword = keywordField == null ? "" : keywordField.getText();
            List<Issue> searched = controllers.search().searchIssues(projectId, status, reporterId, assigneeId, keyword);
            issues.setAll(session.mergeVisibleIssues(searched, projectId, status, reporterId, assigneeId, keyword));
            table.refresh();
            selectIssue(selectedId);
            show("Loaded " + issues.size() + " issue(s).");
        } catch (Exception e) {
            show("Failed to load issues: " + e.getMessage());
        }
    }

    private void selectIssue(Long id) {
        if (issues.isEmpty()) {
            detailView.showIssue(null);
            return;
        }
        Issue issue = issues.stream().filter(it -> id != null && id.equals(it.getId())).findFirst().orElse(issues.get(0));
        table.getSelectionModel().select(issue);
        detailView.showIssue(issue);
    }

    private Project selectedProject() {
        if (session.getSelectedProject() != null) return session.getSelectedProject();
        List<Project> projects = controllers.project().findAll();
        if (projects.isEmpty()) return null;
        session.selectProject(projects.get(0));
        return projects.get(0);
    }

    private void updateCreateButton() {
        if (createButton != null) createButton.setDisable(!canCreate(session.getCurrentUser()));
    }

    private boolean canCreate(Account account) {
        return account != null && (account.getRole() == Role.TESTER || account.getRole() == Role.ADMIN);
    }

    private Long id(Account account) {
        return account == null ? null : account.getId();
    }

    private IssueStatus parseStatus(String value) {
        return value == null || value.isBlank() || "ALL".equalsIgnoreCase(value) ? null : IssueStatus.valueOf(value);
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }

    private String name(Enum<?> value) {
        return value == null ? "-" : value.name();
    }

    private TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private ComboBox<Account> accountBox(String nullText) {
        ComboBox<Account> box = new ComboBox<>();
        box.setMaxWidth(Double.MAX_VALUE);
        box.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Account account) { return account == null ? nullText : UiFormat.account(account); }
            public Account fromString(String string) { return null; }
        });
        return box;
    }

    private Button button(String text, String style, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add(style);
        button.setOnAction(action);
        return button;
    }

    private Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }

    private HBox row(javafx.scene.Node... children) {
        HBox row = new HBox(12, children);
        row.getStyleClass().add("panel");
        row.setPadding(new Insets(16));
        return row;
    }

    private TableColumn<Issue, String> column(String title, int width, Function<Issue, String> text) {
        TableColumn<Issue, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(text.apply(data.getValue())));
        return column;
    }

    private void show(String text) {
        if (message != null) message.setText(text);
        System.out.println(text);
    }
}