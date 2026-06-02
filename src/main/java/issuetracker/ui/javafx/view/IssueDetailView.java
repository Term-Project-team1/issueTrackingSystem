package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IssueDetailView extends VBox {

    private final AppControllers controllers;
    private final UiSession session;
    private final Runnable onIssueChanged;

    private Issue issue;

    private final Label title = new Label("No issue selected");
    private final Label message = new Label("");

    private final Label description = new Label("-");
    private final Label status = new Label("-");
    private final Label priority = new Label("-");
    private final Label reporter = new Label("-");
    private final Label assignee = new Label("-");
    private final Label fixer = new Label("-");
    private final Label availableActions = new Label("Available actions: -");

    private final ComboBox<Account> assigneeBox = new ComboBox<>();
    private final ObservableList<String> comments = FXCollections.observableArrayList();
    private final ObservableList<String> recommendations = FXCollections.observableArrayList();

    private final ListView<String> commentList = new ListView<>(comments);
    private final ListView<String> recommendationList = new ListView<>(recommendations);

    private final TextArea commentArea = new TextArea();

    private Button recommendButton;
    private Button assignButton;
    private Button markFixedButton;
    private Button resolveButton;
    private Button closeButton;
    private Button reopenButton;
    private Button addCommentButton;

    public IssueDetailView(AppControllers controllers, UiSession session) {
        this(controllers, session, () -> {
        });
    }

    public IssueDetailView(AppControllers controllers, UiSession session, Runnable onIssueChanged) {
        this.controllers = controllers;
        this.session = session;
        this.onIssueChanged = onIssueChanged == null ? () -> {
        } : onIssueChanged;

        getStyleClass().add("panel");
        setPadding(new Insets(18));
        setSpacing(12);
        setPrefWidth(380);

        createView();
        loadAssignees();
        showIssue(null);
    }

    private void createView() {
        Label panelTitle = new Label("Issue Detail");
        panelTitle.getStyleClass().add("panel-title");

        title.getStyleClass().add("detail-title");
        title.setWrapText(true);

        availableActions.getStyleClass().add("field-label");
        message.getStyleClass().add("field-label");

        description.setWrapText(true);
        description.getStyleClass().add("detail-value");
        status.getStyleClass().add("detail-value");
        priority.getStyleClass().add("detail-value");
        reporter.getStyleClass().add("detail-value");
        assignee.getStyleClass().add("detail-value");
        fixer.getStyleClass().add("detail-value");

        assigneeBox.setMaxWidth(Double.MAX_VALUE);
        assigneeBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Account account) {
                return UiFormat.account(account);
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        });

        recommendButton = new Button("Recommend Assignee");
        recommendButton.getStyleClass().add("secondary-button");
        recommendButton.setMaxWidth(Double.MAX_VALUE);
        recommendButton.setOnAction(event -> recommendAssignees());

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

        addCommentButton = new Button("Add Comment");
        addCommentButton.getStyleClass().add("primary-button");
        addCommentButton.setMaxWidth(Double.MAX_VALUE);
        addCommentButton.setOnAction(event -> addComment());

        commentArea.setPromptText("comment");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(3);

        commentList.getStyleClass().add("data-list");
        commentList.setPrefHeight(150);

        recommendationList.getStyleClass().add("data-list");
        recommendationList.setPrefHeight(90);

        HBox assignRow = new HBox(10, assigneeBox, assignButton);
        HBox.setHgrow(assigneeBox, Priority.ALWAYS);

        HBox actionRow1 = new HBox(10, markFixedButton, resolveButton);
        HBox actionRow2 = new HBox(10, closeButton, reopenButton);

        getChildren().addAll(
                panelTitle,
                availableActions,
                title,
                field("Description", description),
                field("Status", status),
                field("Priority", priority),
                field("Reporter", reporter),
                field("Assignee", assignee),
                field("Fixer", fixer),
                recommendButton,
                field("추천 후보 Top3", recommendationList),
                assignRow,
                actionRow1,
                actionRow2,
                field("Comments", commentList),
                commentArea,
                addCommentButton,
                message
        );
    }

    public void showIssue(Issue selectedIssue) {
        this.issue = selectedIssue;

        if (selectedIssue == null) {
            title.setText("No issue selected");
            description.setText("-");
            status.setText("-");
            priority.setText("-");
            reporter.setText("-");
            assignee.setText("-");
            fixer.setText("-");
            comments.setAll("No comments");
            recommendations.clear();
            updateAvailableActions();
            return;
        }

        title.setText(safe(selectedIssue.getTitle()));
        description.setText(safe(selectedIssue.getDescription()));
        status.setText(selectedIssue.getStatus() == null ? "-" : selectedIssue.getStatus().name());
        priority.setText(selectedIssue.getPriority() == null ? "-" : selectedIssue.getPriority().name());
        reporter.setText(UiFormat.account(selectedIssue.getReporter()));
        assignee.setText(UiFormat.account(selectedIssue.getAssignee()));
        fixer.setText(UiFormat.account(selectedIssue.getFixer()));

        selectCurrentAssignee();
        loadComments();
        updateAvailableActions();
    }

    public Issue getIssue() {
        return issue;
    }

    private void loadAssignees() {
        try {
            List<Account> developers = controllers.account().findByRole(Role.DEV);
            assigneeBox.setItems(FXCollections.observableArrayList(developers));

            if (!developers.isEmpty()) {
                assigneeBox.setValue(developers.get(0));
            }

        } catch (Exception e) {
            showMessage("개발자 목록을 불러오지 못했습니다: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void selectCurrentAssignee() {
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

    private void loadComments() {
        if (issue == null || issue.getId() == null) {
            comments.setAll("No comments");
            return;
        }

        try {
            List<Comment> loadedComments = controllers.comment().findCommentsByIssue(issue.getId());

            if (loadedComments.isEmpty()) {
                comments.setAll("No comments");
                return;
            }

            comments.setAll(
                    loadedComments.stream()
                            .map(this::formatComment)
                            .toList()
            );

        } catch (Exception e) {
            comments.setAll("Failed to load comments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recommendAssignees() {
        if (issue == null) {
            showMessage("이슈를 먼저 선택하세요.", true);
            return;
        }

        try {
            List<Account> candidates = controllers.recommendation().recommendAssignees(issue, 3);

            if (candidates.isEmpty()) {
                recommendations.setAll("추천 후보가 없습니다.");
                showMessage("추천 후보가 없습니다.", true);
                return;
            }

            recommendations.setAll(
                    candidates.stream()
                            .map(UiFormat::account)
                            .toList()
            );

            assigneeBox.setValue(candidates.get(0));

            List<String> names = candidates.stream()
                    .map(UiFormat::username)
                    .toList();

            showMessage("Best candidate: " + String.join(", ", names), false);

        } catch (Exception e) {
            recommendations.setAll("추천 실패: " + e.getMessage());
            showMessage("추천 실패: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void assignIssue() {
        if (issue == null || issue.getId() == null) {
            showMessage("이슈를 먼저 선택하세요.", true);
            return;
        }

        Account assignee = assigneeBox.getValue();
        Account currentUser = session.getCurrentUser();

        if (assignee == null) {
            showMessage("담당 개발자를 선택하세요.", true);
            return;
        }

        if (currentUser == null) {
            showMessage("현재 사용자를 선택하세요.", true);
            return;
        }

        try {
            controllers.issue().assignIssue(issue.getId(), assignee, currentUser);
            reloadCurrentIssue();
            showMessage("Assignee updated.", false);
            onIssueChanged.run();

        } catch (Exception e) {
            showMessage("Failed to assign: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void markFixed() {
        if (!hasIssueAndUser()) {
            return;
        }

        try {
            controllers.issue().markFixed(issue.getId(), session.getCurrentUser());
            reloadCurrentIssue();
            showMessage("Marked as FIXED.", false);
            onIssueChanged.run();

        } catch (Exception e) {
            showMessage("Failed to mark fixed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void resolveIssue() {
        if (!hasIssueAndUser()) {
            return;
        }

        try {
            controllers.issue().resolveIssue(issue.getId(), session.getCurrentUser());
            reloadCurrentIssue();
            showMessage("Resolved.", false);
            onIssueChanged.run();

        } catch (Exception e) {
            showMessage("Failed to resolve: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void closeIssue() {
        if (!hasIssueAndUser()) {
            return;
        }

        try {
            controllers.issue().closeIssue(issue.getId(), session.getCurrentUser());
            reloadCurrentIssue();
            showMessage("Closed.", false);
            onIssueChanged.run();

        } catch (Exception e) {
            showMessage("Close failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void reopenIssue() {
        if (!hasIssueAndUser()) {
            return;
        }

        try {
            controllers.issue().reopenIssue(issue.getId(), session.getCurrentUser());
            reloadCurrentIssue();
            showMessage("Reopened.", false);
            onIssueChanged.run();

        } catch (Exception e) {
            showMessage("Reopen failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void addComment() {
        if (!hasIssueAndUser()) {
            return;
        }

        String content = commentArea.getText();

        if (content == null || content.isBlank()) {
            showMessage("Comment cannot be empty.", true);
            return;
        }

        try {
            controllers.comment().addComment(
                    issue.getId(),
                    session.getCurrentUser().getId(),
                    content
            );

            commentArea.clear();
            loadComments();

            showMessage("Comment added.", false);
            onIssueChanged.run();

        } catch (Exception e) {
            showMessage("Failed to add comment: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void reloadCurrentIssue() {
        if (issue == null || issue.getId() == null) {
            return;
        }

        try {
            Issue reloaded = controllers.issue().viewIssue(issue.getId());
            showIssue(reloaded);

        } catch (Exception e) {
            showMessage("이슈 새로고침 실패: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private boolean hasIssueAndUser() {
        if (issue == null || issue.getId() == null) {
            showMessage("이슈를 먼저 선택하세요.", true);
            return false;
        }

        if (session.getCurrentUser() == null || session.getCurrentUser().getId() == null) {
            showMessage("현재 사용자를 선택하세요.", true);
            return false;
        }

        return true;
    }

    private void updateAvailableActions() {
        Account currentUser = session.getCurrentUser();
        IssueStatus currentStatus = issue == null ? null : issue.getStatus();

        boolean hasIssue = issue != null;
        boolean hasUser = currentUser != null;

        boolean isAdmin = hasRole(currentUser, Role.ADMIN);
        boolean isPL = hasRole(currentUser, Role.PL);
        boolean isDev = hasRole(currentUser, Role.DEV);
        boolean isTester = hasRole(currentUser, Role.TESTER);

        boolean canRecommend = hasIssue && hasUser && (isPL || isAdmin);
        boolean canAssign = hasIssue && hasUser && (isPL || isAdmin)
                && (currentStatus == IssueStatus.NEW || currentStatus == IssueStatus.REOPENED);

        boolean canMarkFixed = hasIssue && hasUser && isDev
                && currentStatus == IssueStatus.ASSIGNED
                && sameAccount(currentUser, issue.getAssignee());

        boolean canResolve = hasIssue && hasUser && (isTester || isAdmin)
                && currentStatus == IssueStatus.FIXED;

        boolean canClose = hasIssue && hasUser && (isPL || isAdmin)
                && currentStatus == IssueStatus.RESOLVED;

        boolean canReopen = hasIssue && hasUser && (isTester || isPL || isAdmin)
                && (currentStatus == IssueStatus.CLOSED || currentStatus == IssueStatus.RESOLVED);

        boolean canComment = hasIssue && hasUser;

        setDisabled(recommendButton, !canRecommend);
        setDisabled(assignButton, !canAssign);
        setDisabled(markFixedButton, !canMarkFixed);
        setDisabled(resolveButton, !canResolve);
        setDisabled(closeButton, !canClose);
        setDisabled(reopenButton, !canReopen);
        setDisabled(addCommentButton, !canComment);

        availableActions.setText("Available actions: " + buildAvailableActionsText(
                canRecommend,
                canAssign,
                canMarkFixed,
                canResolve,
                canClose,
                canReopen,
                canComment
        ));
    }

    private String buildAvailableActionsText(boolean canRecommend,
                                             boolean canAssign,
                                             boolean canMarkFixed,
                                             boolean canResolve,
                                             boolean canClose,
                                             boolean canReopen,
                                             boolean canComment) {
        List<String> actions = new ArrayList<>();

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

    private boolean hasRole(Account account, Role role) {
        return account != null && account.getRole() == role;
    }

    private boolean sameAccount(Account a, Account b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null) {
            return false;
        }

        return Objects.equals(a.getId(), b.getId());
    }

    private void setDisabled(Button button, boolean disabled) {
        if (button != null) {
            button.setDisable(disabled);
        }
    }

    private String formatComment(Comment comment) {
        if (comment == null) {
            return "-";
        }

        String date = "-";

        if (comment.getCreatedDate() != null) {
            date = comment.getCreatedDate().toLocalDate().toString();
        }

        return "[" + date + "] "
                + UiFormat.username(comment.getAuthor())
                + ": "
                + safe(comment.getContent());
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private VBox field(String labelText, javafx.scene.Node content) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        VBox box = new VBox(5, label, content);
        VBox.setVgrow(content, Priority.NEVER);

        return box;
    }

    private void showMessage(String text, boolean error) {
        message.setText(text);
        message.getStyleClass().removeAll("error-text", "success-text");
        message.getStyleClass().add(error ? "error-text" : "success-text");
    }
}