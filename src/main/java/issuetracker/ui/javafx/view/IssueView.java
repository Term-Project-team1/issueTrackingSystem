package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.comment.Comment;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IssueView extends VBox {

    private final AppControllers controllers;
    private final UiSession session;
    private final ObservableList<Issue> issues = FXCollections.observableArrayList();

    private TableView<Issue> issueTable;

    private TextField titleField;
    private TextField descriptionField;
    private ComboBox<Priority> priorityBox;

    private ComboBox<String> statusFilterBox;
    private ComboBox<Account> reporterFilterBox;
    private ComboBox<Account> assigneeFilterBox;
    private TextField keywordField;

    private Label detailTitle;
    private Label detailDescription;
    private Label detailStatus;
    private Label detailPriority;
    private Label detailReporter;
    private Label detailAssignee;
    private Label detailFixer;

    private ComboBox<Account> assigneeBox;
    private TextArea commentArea;
    private Label commentsLabel;
    private Label recommendationLabel;
    private Label messageLabel;
    private Label availableActionsLabel;

    private Button createButton;
    private Button recommendButton;
    private Button assignButton;
    private Button markFixedButton;
    private Button resolveButton;
    private Button closeButton;
    private Button reopenButton;
    private Button addCommentButton;

    private Issue selectedIssue;

    public IssueView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;

        getStyleClass().add("content");
        setSpacing(18);
        setPadding(new Insets(28));

        Label title = new Label("Issues");
        title.getStyleClass().add("page-title");

        VBox createArea = createIssueForm();
        VBox searchArea = createSearchArea();

        HBox mainArea = new HBox(20, createIssueTablePanel(), createDetailPanel());
        VBox.setVgrow(mainArea, javafx.scene.layout.Priority.ALWAYS);

        messageLabel = new Label("");
        messageLabel.getStyleClass().add("field-label");

        getChildren().addAll(title, createArea, searchArea, mainArea, messageLabel);

        session.addProjectListener(() -> {
            loadComboBoxData();
            refreshIssues();
            updateAvailableActions();
        });

        loadComboBoxData();
        refreshIssues();
    }

    private VBox createIssueForm() {
        titleField = new TextField();
        titleField.setPromptText("issue title");

        descriptionField = new TextField();
        descriptionField.setPromptText("description");

        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(Priority.values());
        priorityBox.setValue(Priority.MAJOR);

        createButton = new Button("Create Issue");
        createButton.getStyleClass().add("primary-button");
        createButton.setOnAction(event -> {
            createButton.setDisable(true);

            try {
                createIssue();
            } finally {
                updateAvailableActions();
            }
        });

        HBox row = new HBox(12, titleField, descriptionField, priorityBox, createButton);
        row.getStyleClass().add("panel");
        row.setPadding(new Insets(16));

        HBox.setHgrow(titleField, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(descriptionField, javafx.scene.layout.Priority.ALWAYS);

        return new VBox(row);
    }

    private VBox createSearchArea() {
        statusFilterBox = new ComboBox<>();
        statusFilterBox.getItems().addAll("ALL", "NEW", "ASSIGNED", "FIXED", "RESOLVED", "CLOSED", "REOPENED");
        statusFilterBox.setValue("ALL");

        reporterFilterBox = new ComboBox<>();
        reporterFilterBox.setMaxWidth(Double.MAX_VALUE);
        reporterFilterBox.setConverter(accountConverter("ALL"));

        assigneeFilterBox = new ComboBox<>();
        assigneeFilterBox.setMaxWidth(Double.MAX_VALUE);
        assigneeFilterBox.setConverter(accountConverter("ALL"));

        keywordField = new TextField();
        keywordField.setPromptText("keyword");

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("secondary-button");
        searchButton.setOnAction(event -> searchIssues());

        Button reloadButton = new Button("Reload");
        reloadButton.getStyleClass().add("secondary-button");
        reloadButton.setOnAction(event -> refreshIssues());

        HBox row = new HBox(
                12,
                statusFilterBox,
                reporterFilterBox,
                assigneeFilterBox,
                keywordField,
                searchButton,
                reloadButton
        );

        row.getStyleClass().add("panel");
        row.setPadding(new Insets(16));

        HBox.setHgrow(keywordField, javafx.scene.layout.Priority.ALWAYS);

        return new VBox(row);
    }

    private VBox createIssueTablePanel() {
        issueTable = new TableView<>(issues);
        issueTable.getStyleClass().add("data-table");

        TableColumn<Issue, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getId()))
        );
        idColumn.setPrefWidth(80);

        TableColumn<Issue, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(safe(data.getValue().getTitle()))
        );
        titleColumn.setPrefWidth(220);

        TableColumn<Issue, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getStatus() != null ? data.getValue().getStatus().name() : "-")
        );
        statusColumn.setPrefWidth(120);

        TableColumn<Issue, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getPriority() != null ? data.getValue().getPriority().name() : "-")
        );
        priorityColumn.setPrefWidth(120);

        TableColumn<Issue, String> reporterColumn = new TableColumn<>("Reporter");
        reporterColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(UiFormat.username(data.getValue().getReporter()))
        );
        reporterColumn.setPrefWidth(140);

        TableColumn<Issue, String> assigneeColumn = new TableColumn<>("Assignee");
        assigneeColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(UiFormat.username(data.getValue().getAssignee()))
        );
        assigneeColumn.setPrefWidth(140);

        TableColumn<Issue, String> fixerColumn = new TableColumn<>("Fixer");
        fixerColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(UiFormat.username(data.getValue().getFixer()))
        );
        fixerColumn.setPrefWidth(140);

        issueTable.getColumns().setAll(
                idColumn,
                titleColumn,
                statusColumn,
                priorityColumn,
                reporterColumn,
                assigneeColumn,
                fixerColumn
        );

        issueTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showIssueDetail(newValue)
        );

        Label panelTitle = new Label("Issue List");
        panelTitle.getStyleClass().add("panel-title");

        VBox panel = new VBox(14, panelTitle, issueTable);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));

        HBox.setHgrow(panel, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(issueTable, javafx.scene.layout.Priority.ALWAYS);

        return panel;
    }

    private ScrollPane createDetailPanel() {
        Label title = new Label("Issue Detail");
        title.getStyleClass().add("panel-title");

        availableActionsLabel = new Label("Available actions: -");
        availableActionsLabel.getStyleClass().add("field-label");

        detailTitle = new Label("No issue selected");
        detailTitle.getStyleClass().add("detail-title");

        detailDescription = new Label("-");
        detailStatus = new Label("-");
        detailPriority = new Label("-");
        detailReporter = new Label("-");
        detailAssignee = new Label("-");
        detailFixer = new Label("-");

        detailDescription.getStyleClass().add("detail-value");
        detailStatus.getStyleClass().add("detail-value");
        detailPriority.getStyleClass().add("detail-value");
        detailReporter.getStyleClass().add("detail-value");
        detailAssignee.getStyleClass().add("detail-value");
        detailFixer.getStyleClass().add("detail-value");

        assigneeBox = new ComboBox<>();
        assigneeBox.setMaxWidth(Double.MAX_VALUE);
        assigneeBox.setConverter(accountConverter("-"));

        recommendButton = new Button("Recommend Assignee");
        recommendButton.getStyleClass().add("secondary-button");
        recommendButton.setMaxWidth(Double.MAX_VALUE);
        recommendButton.setOnAction(event -> recommendAssignee());

        assignButton = new Button("Assign");
        assignButton.getStyleClass().add("primary-button");
        assignButton.setOnAction(event -> assignIssue());

        markFixedButton = new Button("Mark Fixed");
        markFixedButton.getStyleClass().add("secondary-button");
        markFixedButton.setOnAction(event -> markFixed());

        resolveButton = new Button("Resolve");
        resolveButton.getStyleClass().add("secondary-button");
        resolveButton.setOnAction(event -> resolveIssue());

        closeButton = new Button("Close");
        closeButton.getStyleClass().add("danger-button");
        closeButton.setOnAction(event -> closeIssue());

        reopenButton = new Button("Reopen");
        reopenButton.getStyleClass().add("secondary-button");
        reopenButton.setOnAction(event -> reopenIssue());

        recommendationLabel = new Label("아직 추천 후보를 조회하지 않았습니다.");
        recommendationLabel.setWrapText(true);
        recommendationLabel.getStyleClass().add("detail-value");

        commentsLabel = new Label("No comments");
        commentsLabel.setWrapText(true);
        commentsLabel.getStyleClass().add("detail-value");

        commentArea = new TextArea();
        commentArea.setPromptText("comment");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        addCommentButton = new Button("Add Comment");
        addCommentButton.getStyleClass().add("primary-button");
        addCommentButton.setMaxWidth(Double.MAX_VALUE);
        addCommentButton.setOnAction(event -> addComment());

        HBox assignRow = new HBox(10, assigneeBox, assignButton);
        HBox actionRow = new HBox(10, markFixedButton, resolveButton, closeButton, reopenButton);

        VBox panel = new VBox(
                12,
                title,
                availableActionsLabel,
                detailTitle,
                fieldLabelBox("Description", detailDescription),
                fieldLabelBox("Status", detailStatus),
                fieldLabelBox("Priority", detailPriority),
                fieldLabelBox("Reporter", detailReporter),
                fieldLabelBox("Assignee", detailAssignee),
                fieldLabelBox("Fixer", detailFixer),
                recommendButton,
                fieldLabel("추천 후보 Top3"),
                recommendationLabel,
                assignRow,
                actionRow,
                fieldLabel("Comments"),
                commentsLabel,
                commentArea,
                addCommentButton
        );

        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(360);

        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(380);
        scrollPane.setMinWidth(360);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private void loadComboBoxData() {
        reporterFilterBox.getItems().clear();
        assigneeFilterBox.getItems().clear();
        assigneeBox.getItems().clear();

        reporterFilterBox.getItems().add(null);
        assigneeFilterBox.getItems().add(null);

        try {
            List<Account> testers = controllers.account().findByRole(Role.TESTER);
            List<Account> developers = controllers.account().findByRole(Role.DEV);

            reporterFilterBox.getItems().addAll(testers);
            assigneeFilterBox.getItems().addAll(developers);
            assigneeBox.getItems().addAll(developers);

            reporterFilterBox.setValue(null);
            assigneeFilterBox.setValue(null);

            if (!assigneeBox.getItems().isEmpty()) {
                assigneeBox.setValue(assigneeBox.getItems().get(0));
            }

            updateAvailableActions();

        } catch (Exception e) {
            showMessage("Failed to load accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createIssue() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        Priority priority = priorityBox.getValue();
        Account reporter = session.getCurrentUser();

        if (title == null || title.isBlank()) {
            showMessage("Please enter a title.");
            return;
        }

        if (reporter == null) {
            showMessage("Please select a current user.");
            return;
        }

        if (!hasRole(reporter, Role.TESTER) && !hasRole(reporter, Role.ADMIN)) {
            showMessage("이슈 생성은 TESTER 또는 ADMIN만 가능합니다.");
            return;
        }

        Project project = getSelectedOrDefaultProject();

        if (project == null) {
            showMessage("프로젝트가 없습니다. 먼저 Project 페이지에서 프로젝트를 생성하세요.");
            return;
        }

        try {
            Issue created = controllers.issue().createIssue(
                    project,
                    reporter,
                    title,
                    description,
                    priority
            );

            session.rememberCreatedIssue(created);
            session.selectProject(project);

            titleField.clear();
            descriptionField.clear();

            refreshIssues();
            selectIssueById(created.getId());

            showMessage("Issue created.");

        } catch (Exception e) {
            showMessage("Failed to create issue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshIssues() {
        try {
            Long projectId = projectId();
            IssueStatus status = parseStatus(statusFilterBox == null ? null : statusFilterBox.getValue());
            Long reporterId = accountId(reporterFilterBox == null ? null : reporterFilterBox.getValue());
            Long assigneeId = accountId(assigneeFilterBox == null ? null : assigneeFilterBox.getValue());
            String keyword = keywordField == null ? "" : keywordField.getText();

            List<Issue> searchedIssues = controllers.search().searchIssues(
                    projectId,
                    status,
                    reporterId,
                    assigneeId,
                    keyword
            );

            List<Issue> visibleIssues = session.mergeVisibleIssues(
                    searchedIssues,
                    projectId,
                    status,
                    reporterId,
                    assigneeId,
                    keyword
            );

            issues.setAll(visibleIssues);
            issueTable.refresh();

            if (!issues.isEmpty()) {
                issueTable.getSelectionModel().selectFirst();
            } else {
                showIssueDetail(null);
            }

            showMessage("Loaded " + issues.size() + " issue(s).");

        } catch (Exception e) {
            showMessage("Failed to load issues: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchIssues() {
        refreshIssues();
    }

    private void showIssueDetail(Issue issue) {
        selectedIssue = issue;

        if (issue == null) {
            detailTitle.setText("No issue selected");
            detailDescription.setText("-");
            detailStatus.setText("-");
            detailPriority.setText("-");
            detailReporter.setText("-");
            detailAssignee.setText("-");
            detailFixer.setText("-");
            recommendationLabel.setText("아직 추천 후보를 조회하지 않았습니다.");
            commentsLabel.setText("No comments");
            updateAvailableActions();
            return;
        }

        detailTitle.setText(safe(issue.getTitle()));
        detailDescription.setText(safe(issue.getDescription()));
        detailStatus.setText(issue.getStatus() != null ? issue.getStatus().name() : "-");
        detailPriority.setText(issue.getPriority() != null ? issue.getPriority().name() : "-");
        detailReporter.setText(UiFormat.account(issue.getReporter()));
        detailAssignee.setText(UiFormat.account(issue.getAssignee()));
        detailFixer.setText(UiFormat.account(issue.getFixer()));

        recommendationLabel.setText("아직 추천 후보를 조회하지 않았습니다.");

        selectCurrentAssignee(issue);
        refreshComments(issue);
        updateAvailableActions();
    }

    private void refreshComments(Issue issue) {
        if (issue == null || issue.getId() == null) {
            commentsLabel.setText("No comments");
            return;
        }

        try {
            List<Comment> comments = controllers.comment().findCommentsByIssue(issue.getId());

            if (comments.isEmpty()) {
                commentsLabel.setText("No comments");
                return;
            }

            StringBuilder sb = new StringBuilder();

            for (Comment comment : comments) {
                sb.append("- [")
                        .append(formatDateOnly(comment))
                        .append("] ")
                        .append(UiFormat.username(comment.getAuthor()))
                        .append(": ")
                        .append(safe(comment.getContent()))
                        .append("\n");
            }

            commentsLabel.setText(sb.toString().trim());

        } catch (Exception e) {
            commentsLabel.setText("Failed to load comments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void assignIssue() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            return;
        }

        Account assignee = assigneeBox.getValue();
        Account pl = session.getCurrentUser();

        if (assignee == null) {
            showMessage("Select an assignee.");
            return;
        }

        if (pl == null) {
            showMessage("Please select a current user.");
            return;
        }

        Long issueId = selectedIssue.getId();

        try {
            controllers.issue().assignIssue(issueId, assignee, pl);
            refreshIssues();
            selectIssueById(issueId);
            showMessage("Assignee updated.");

        } catch (Exception e) {
            showMessage("Failed to assign: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void markFixed() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            return;
        }

        Long issueId = selectedIssue.getId();

        try {
            controllers.issue().markFixed(issueId, session.getCurrentUser());
            refreshIssues();
            selectIssueById(issueId);
            showMessage("Marked as FIXED.");

        } catch (Exception e) {
            showMessage("Failed to mark fixed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resolveIssue() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            return;
        }

        Long issueId = selectedIssue.getId();

        try {
            controllers.issue().resolveIssue(issueId, session.getCurrentUser());
            refreshIssues();
            selectIssueById(issueId);
            showMessage("Resolved.");

        } catch (Exception e) {
            showMessage("Failed to resolve: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeIssue() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            return;
        }

        Long issueId = selectedIssue.getId();

        try {
            controllers.issue().closeIssue(issueId, session.getCurrentUser());
            refreshIssues();
            selectIssueById(issueId);
            showMessage("Closed.");

        } catch (Exception e) {
            showMessage("Close failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reopenIssue() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            return;
        }

        Long issueId = selectedIssue.getId();

        try {
            controllers.issue().reopenIssue(issueId, session.getCurrentUser());
            refreshIssues();
            selectIssueById(issueId);
            showMessage("Reopened.");

        } catch (Exception e) {
            showMessage("Reopen failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recommendAssignee() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            recommendationLabel.setText("추천할 이슈를 먼저 선택하세요.");
            return;
        }

        try {
            List<Account> recommended = controllers.recommendation().recommendAssignees(selectedIssue, 3);

            if (recommended.isEmpty()) {
                recommendationLabel.setText("추천 후보가 없습니다.");
                showMessage("No recommendations available.");
                return;
            }

            assigneeBox.setValue(recommended.get(0));

            List<String> names = new ArrayList<>();

            for (int i = 0; i < recommended.size(); i++) {
                Account account = recommended.get(i);
                names.add((i + 1) + ". " + UiFormat.account(account));
            }

            recommendationLabel.setText(String.join("\n", names));

            List<String> simpleNames = recommended.stream()
                    .map(UiFormat::username)
                    .toList();

            showMessage("Recommended: " + String.join(", ", simpleNames));

        } catch (Exception e) {
            recommendationLabel.setText("추천 조회 실패: " + e.getMessage());
            showMessage("Recommendation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addComment() {
        if (selectedIssue == null) {
            showMessage("Select an issue first.");
            return;
        }

        Account author = session.getCurrentUser();
        String content = commentArea.getText();

        if (author == null || author.getId() == null) {
            showMessage("Please select a current user.");
            return;
        }

        if (content == null || content.isBlank()) {
            showMessage("Comment cannot be empty.");
            return;
        }

        try {
            controllers.comment().addComment(selectedIssue.getId(), author.getId(), content);
            commentArea.clear();
            refreshComments(selectedIssue);
            showMessage("Comment added.");

        } catch (Exception e) {
            showMessage("Failed to add comment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectIssueById(Long issueId) {
        if (issueId == null) {
            return;
        }

        for (Issue issue : issues) {
            if (issueId.equals(issue.getId())) {
                issueTable.getSelectionModel().select(issue);
                showIssueDetail(issue);
                return;
            }
        }
    }

    private void updateAvailableActions() {
        IssueStatus status = selectedIssue == null ? null : selectedIssue.getStatus();
        Account currentUser = session.getCurrentUser();

        boolean hasSelectedIssue = selectedIssue != null;
        boolean hasCurrentUser = currentUser != null;

        boolean canCreate = hasCurrentUser && (hasRole(currentUser, Role.TESTER) || hasRole(currentUser, Role.ADMIN));
        boolean canComment = hasSelectedIssue && hasCurrentUser;

        boolean canRecommend = hasSelectedIssue && hasCurrentUser
                && (hasRole(currentUser, Role.PL) || hasRole(currentUser, Role.ADMIN));

        boolean canAssign = hasSelectedIssue && hasCurrentUser
                && (hasRole(currentUser, Role.PL) || hasRole(currentUser, Role.ADMIN))
                && (status == IssueStatus.NEW || status == IssueStatus.REOPENED);

        boolean canMarkFixed = hasSelectedIssue && hasCurrentUser
                && hasRole(currentUser, Role.DEV)
                && status == IssueStatus.ASSIGNED
                && sameAccount(currentUser, selectedIssue.getAssignee());

        boolean canResolve = hasSelectedIssue && hasCurrentUser
                && (hasRole(currentUser, Role.TESTER) || hasRole(currentUser, Role.ADMIN))
                && status == IssueStatus.FIXED;

        boolean canClose = hasSelectedIssue && hasCurrentUser
                && (hasRole(currentUser, Role.PL) || hasRole(currentUser, Role.ADMIN))
                && status == IssueStatus.RESOLVED;

        boolean canReopen = hasSelectedIssue && hasCurrentUser
                && (hasRole(currentUser, Role.TESTER) || hasRole(currentUser, Role.PL) || hasRole(currentUser, Role.ADMIN))
                && (status == IssueStatus.CLOSED || status == IssueStatus.RESOLVED);

        setDisabled(createButton, !canCreate);
        setDisabled(recommendButton, !canRecommend);
        setDisabled(assignButton, !canAssign);
        setDisabled(markFixedButton, !canMarkFixed);
        setDisabled(resolveButton, !canResolve);
        setDisabled(closeButton, !canClose);
        setDisabled(reopenButton, !canReopen);
        setDisabled(addCommentButton, !canComment);

        if (availableActionsLabel != null) {
            availableActionsLabel.setText("Available actions: " + buildAvailableActionsText(
                    canCreate,
                    canRecommend,
                    canAssign,
                    canMarkFixed,
                    canResolve,
                    canClose,
                    canReopen,
                    canComment
            ));
        }
    }

    private String buildAvailableActionsText(boolean canCreate,
                                             boolean canRecommend,
                                             boolean canAssign,
                                             boolean canMarkFixed,
                                             boolean canResolve,
                                             boolean canClose,
                                             boolean canReopen,
                                             boolean canComment) {
        List<String> actions = new ArrayList<>();

        if (canCreate) {
            actions.add("Create");
        }
        if (canRecommend) {
            actions.add("Recommend");
        }
        if (canAssign) {
            actions.add("Assign");
        }
        if (canMarkFixed) {
            actions.add("Mark Fixed");
        }
        if (canResolve) {
            actions.add("Resolve");
        }
        if (canClose) {
            actions.add("Close");
        }
        if (canReopen) {
            actions.add("Reopen");
        }
        if (canComment) {
            actions.add("Comment");
        }

        return actions.isEmpty() ? "-" : String.join(", ", actions);
    }

    private void setDisabled(Button button, boolean disabled) {
        if (button != null) {
            button.setDisable(disabled);
        }
    }

    private Project getSelectedOrDefaultProject() {
        if (session.getSelectedProject() != null) {
            return session.getSelectedProject();
        }

        List<Project> projects = controllers.project().findAll();

        if (projects.isEmpty()) {
            return null;
        }

        Project firstProject = projects.get(0);
        session.selectProject(firstProject);

        return firstProject;
    }

    private Long projectId() {
        return session.getSelectedProject() == null ? null : session.getSelectedProject().getId();
    }

    private Long accountId(Account account) {
        return account == null ? null : account.getId();
    }

    private IssueStatus parseStatus(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }

        return IssueStatus.valueOf(value);
    }

    private boolean sameAccount(Account a, Account b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null) {
            return false;
        }

        return Objects.equals(a.getId(), b.getId());
    }

    private boolean hasRole(Account account, Role role) {
        return account != null && account.getRole() == role;
    }

    private String formatDateOnly(Comment comment) {
        if (comment == null || comment.getCreatedDate() == null) {
            return "-";
        }

        String text = comment.getCreatedDate().toString();

        if (text.length() >= 10) {
            return text.substring(0, 10);
        }

        return text;
    }

    private void selectCurrentAssignee(Issue issue) {
        if (issue == null || issue.getAssignee() == null) {
            return;
        }

        for (Account account : assigneeBox.getItems()) {
            if (sameAccount(account, issue.getAssignee())) {
                assigneeBox.setValue(account);
                return;
            }
        }
    }

    private javafx.util.StringConverter<Account> accountConverter(String nullText) {
        return new javafx.util.StringConverter<>() {
            @Override
            public String toString(Account account) {
                if (account == null) {
                    return nullText;
                }

                return UiFormat.account(account);
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        };
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private VBox fieldLabelBox(String labelText, javafx.scene.Node content) {
        VBox box = new VBox(4, fieldLabel(labelText), content);
        VBox.setVgrow(content, javafx.scene.layout.Priority.NEVER);
        return box;
    }

    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }

        System.out.println(message);
    }
}