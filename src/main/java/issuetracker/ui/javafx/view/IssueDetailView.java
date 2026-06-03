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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IssueDetailView extends VBox {
    private static final String[] ACTIONS = {"Recommend", "Assign", "Mark Fixed", "Resolve", "Close", "Reopen", "Comment"};
    private final AppControllers controllers;
    private final UiSession session;
    private final Runnable onIssueChanged;
    private final ObservableList<String> comments = FXCollections.observableArrayList();
    private final ObservableList<String> recommendations = FXCollections.observableArrayList();

    private Issue issue;
    private final Label title = detail("No issue selected", "detail-title"), message = label("", "field-label");
    private final Label actions = label("Available actions: -", "field-label");
    private final Label description = detail("-", "detail-value"), status = detail("-", "detail-value");
    private final Label priority = detail("-", "detail-value"), reporter = detail("-", "detail-value");
    private final Label assignee = detail("-", "detail-value"), fixer = detail("-", "detail-value");
    private final ComboBox<Account> assigneeBox = new ComboBox<>();
    private final TextArea commentArea = new TextArea();
    private Button recommendButton, assignButton, fixedButton, resolveButton, closeButton, reopenButton, commentButton;

    public IssueDetailView(AppControllers controllers, UiSession session) {
        this(controllers, session, () -> {
        });
    }

    public IssueDetailView(AppControllers controllers, UiSession session, Runnable onIssueChanged) {
        this.controllers = controllers;
        this.session = session;
        this.onIssueChanged = onIssueChanged == null ? () -> {} : onIssueChanged;
        getStyleClass().add("panel");
        setPadding(new Insets(18));
        setSpacing(12);
        setPrefWidth(380);
        createView();
        loadAssignees();
        showIssue(null);
    }

    private void createView() {
        assigneeBox.setMaxWidth(Double.MAX_VALUE);
        assigneeBox.setConverter(accountConverter());
        recommendButton = button("Recommend Assignee", "secondary-button", e -> recommend());
        assignButton = button("Assign", "primary-button", e -> assign());
        fixedButton = button("Mark Fixed", "secondary-button", e -> change("Marked as FIXED.", "Failed to mark fixed: ", (id, user) -> controllers.issue().markFixed(id, user)));
        resolveButton = button("Resolve", "secondary-button", e -> change("Resolved.", "Failed to resolve: ", (id, user) -> controllers.issue().resolveIssue(id, user)));
        closeButton = button("Close", "danger-button", e -> change("Closed.", "Close failed: ", (id, user) -> controllers.issue().closeIssue(id, user)));
        reopenButton = button("Reopen", "secondary-button", e -> change("Reopened.", "Reopen failed: ", (id, user) -> controllers.issue().reopenIssue(id, user)));
        commentButton = button("Add Comment", "primary-button", e -> addComment());
        recommendButton.setMaxWidth(Double.MAX_VALUE);
        commentButton.setMaxWidth(Double.MAX_VALUE);
        commentArea.setPromptText("comment");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        HBox assignRow = new HBox(10, assigneeBox, assignButton);
        HBox.setHgrow(assigneeBox, Priority.ALWAYS);
        getChildren().addAll(label("Issue Detail", "panel-title"), actions, field("Title",title),
                field("Description", description), field("Status", status), field("Priority", priority),
                field("Reporter", reporter), field("Assignee", assignee), field("Fixer", fixer),
                recommendButton, field("Recommended Candidates Top3", list(recommendations, 90)), assignRow,
                new HBox(10, fixedButton, resolveButton), new HBox(10, closeButton, reopenButton),
                field("Comments", list(comments, 150)), commentArea, commentButton, message);
    }

    public void showIssue(Issue selectedIssue) {
        issue = selectedIssue;
        if (issue == null) {
            setDetail("No issue selected", "-", "-", "-", "-", "-", "-");
            comments.setAll("No comments");
            recommendations.clear();
        } else {
            setDetail(safe(issue.getTitle()), safe(issue.getDescription()), name(issue.getStatus()), name(issue.getPriority()),
                    UiFormat.account(issue.getReporter()), UiFormat.account(issue.getAssignee()), UiFormat.account(issue.getFixer()));
            selectAssignee();
            loadComments();
        }
        updateActions();
    }

    public Issue getIssue() {
        return issue;
    }

    public void loadAssignees() {
        try {
            List<Account> developers = controllers.account().findByRole(Role.DEV);
            assigneeBox.setItems(FXCollections.observableArrayList(developers));
            if (!developers.isEmpty()) assigneeBox.setValue(developers.get(0));
        } catch (Exception e) {
            show("개발자 목록을 불러오지 못했습니다: " + e.getMessage(), true);
        }
    }

    private void loadComments() {
        if (!hasIssue()) {
            comments.setAll("No comments");
            return;
        }
        try {
            List<Comment> loaded = controllers.comment().findCommentsByIssue(issue.getId());
            comments.setAll(loaded.isEmpty() ? List.of("No comments") : loaded.stream().map(this::commentText).toList());
        } catch (Exception e) {
            comments.setAll("Failed to load comments: " + e.getMessage());
        }
    }

    private void recommend() {
        if (!hasIssue()) return;
        try {
            List<Account> candidates = controllers.recommendation().recommendAssignees(issue, 3);
            if (candidates.isEmpty()) {
                recommendations.setAll("추천 후보가 없습니다.");
                show("추천 후보가 없습니다.", true);
                return;
            }
            recommendations.setAll(candidates.stream().map(UiFormat::account).toList());
            assigneeBox.setValue(candidates.get(0));
            show("Best candidate: " + String.join(", ", candidates.stream().map(UiFormat::username).toList()), false);
        } catch (Exception e) {
            recommendations.setAll("추천 실패: " + e.getMessage());
            show("추천 실패: " + e.getMessage(), true);
        }
    }

    private void assign() {
        Account user = session.getCurrentUser(), assignee = assigneeBox.getValue();
        if (!hasIssue() || user == null || user.getId() == null || assignee == null) return;
        try {
            controllers.issue().assignIssue(issue.getId(), assignee, user);
            afterChange("Assignee updated.");
        } catch (Exception e) {
            show("Failed to assign: " + e.getMessage(), true);
        }
    }

    private void change(String success, String error, IssueAction action) {
        if (!hasIssueAndUser()) return;
        try {
            action.apply(issue.getId(), session.getCurrentUser());
            afterChange(success);
        } catch (Exception e) {
            show(error + e.getMessage(), true);
        }
    }

    private void addComment() {
        if (!hasIssueAndUser()) return;
        String content = commentArea.getText();
        if (content == null || content.isBlank()) {
            show("Comment cannot be empty.", true);
            return;
        }
        try {
            controllers.comment().addComment(issue.getId(), session.getCurrentUser().getId(), content);
            commentArea.clear();
            loadComments();
            show("Comment added.", false);
            onIssueChanged.run();
        } catch (Exception e) {
            show("Failed to add comment: " + e.getMessage(), true);
        }
    }

    private void afterChange(String text) {
        try {
            showIssue(controllers.issue().viewIssue(issue.getId()));
            show(text, false);
            onIssueChanged.run();
        } catch (Exception e) {
            show("이슈 새로고침 실패: " + e.getMessage(), true);
        }
    }

    private void updateActions() {
        Account user = session.getCurrentUser();
        IssueStatus state = issue == null ? null : issue.getStatus();
        boolean admin = role(user, Role.ADMIN), pl = role(user, Role.PL), dev = role(user, Role.DEV), tester = role(user, Role.TESTER);
        boolean[] enabled = {issue != null && user != null && (pl || admin),
                issue != null && user != null && (pl || admin) && (state == IssueStatus.NEW || state == IssueStatus.REOPENED),
                issue != null && user != null && dev && state == IssueStatus.ASSIGNED && same(user, issue.getAssignee()),
                issue != null && user != null && (tester || admin) && state == IssueStatus.FIXED,
                issue != null && user != null && (pl || admin) && state == IssueStatus.RESOLVED,
                issue != null && user != null && (tester || pl || admin) && (state == IssueStatus.CLOSED || state == IssueStatus.RESOLVED),
                issue != null && user != null};
        Button[] buttons = {recommendButton, assignButton, fixedButton, resolveButton, closeButton, reopenButton, commentButton};
        List<String> names = new ArrayList<>();
        for (int i = 0; i < enabled.length; i++) {
            buttons[i].setDisable(!enabled[i]);
            if (enabled[i]) names.add(ACTIONS[i]);
        }
        actions.setText("Available actions: " + (names.isEmpty() ? "-" : String.join(", ", names)));
    }

    private boolean hasIssue() {
        return issue != null && issue.getId() != null;
    }

    private boolean hasIssueAndUser() {
        Account user = session.getCurrentUser();
        return hasIssue() && user != null && user.getId() != null;
    }

    private boolean role(Account account, Role role) {
        return account != null && account.getRole() == role;
    }

    private boolean same(Account a, Account b) {
        return a != null && b != null && a.getId() != null && b.getId() != null && Objects.equals(a.getId(), b.getId());
    }

    private void selectAssignee() {
        if (issue.getAssignee() != null)
            assigneeBox.getItems().stream().filter(account -> same(account, issue.getAssignee())).findFirst().ifPresent(assigneeBox::setValue);
    }

    private String commentText(Comment comment) {
        String date = comment == null || comment.getCreatedDate() == null ? "-" : comment.getCreatedDate().toLocalDate().toString();
        return "[" + date + "] " + UiFormat.username(comment == null ? null : comment.getAuthor()) + ": " + safe(comment == null ? null : comment.getContent());
    }

    private void setDetail(String t, String d, String s, String p, String r, String a, String f) {
        title.setText(t);
        description.setText(d);
        status.setText(s);
        priority.setText(p);
        reporter.setText(r);
        assignee.setText(a);
        fixer.setText(f);
    }

    private String safe(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    private String name(Enum<?> value) {
        return value == null ? "-" : value.name();
    }

    private Button button(String text, String style, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().add(style);
        button.setOnAction(action);
        return button;
    }

    private static Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }

    private static Label detail(String text, String style) {
        Label label = label(text, style);
        label.setWrapText(true);
        return label;
    }

    private VBox field(String name, Node content) {
        VBox box = new VBox(5, label(name, "detail-label"), content);
        VBox.setVgrow(content, Priority.NEVER);
        return box;
    }

    private ListView<String> list(ObservableList<String> items, int height) {
        ListView<String> list = new ListView<>(items);
        list.getStyleClass().add("data-list");
        list.setPrefHeight(height);
        return list;
    }

    private javafx.util.StringConverter<Account> accountConverter() {
        return new javafx.util.StringConverter<>() {
            public String toString(Account account) { return UiFormat.account(account); }
            public Account fromString(String string) { return null; }
        };
    }

    private void show(String text, boolean error) {
        message.setText(text);
        message.getStyleClass().removeAll("error-text", "success-text");
        message.getStyleClass().add(error ? "error-text" : "success-text");
    }

    @FunctionalInterface
    private interface IssueAction {
        void apply(Long issueId, Account user);
    }
}