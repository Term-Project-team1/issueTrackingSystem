package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProjectView extends VBox {
    private final AppControllers controllers;
    private final UiSession session;
    private final ObservableList<String> projects = FXCollections.observableArrayList();
    private TextField nameField;
    private Button createButton;
    private Label messageLabel;

    public ProjectView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;

        getStyleClass().add("content");
        setSpacing(18);
        setPadding(new Insets(28));
        HBox body = new HBox(18, projectList(), projectForm());
        VBox.setVgrow(body, Priority.ALWAYS);

        messageLabel = label("", "field-label");
        getChildren().addAll(label("Projects", "page-title"), body, messageLabel);
        refreshProjects();
        updateActions();
    }

    private VBox projectList() {
        ListView<String> list = new ListView<>(projects);
        list.getStyleClass().add("data-list");
        VBox panel = panel("Project 목록", list);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox projectForm() {
        nameField = new TextField();
        nameField.setPromptText("project name");
        createButton = new Button("Create Project");
        createButton.getStyleClass().add("primary-button");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(event -> createProject());

        VBox form = new VBox(12, label("Name", "field-label"), nameField, createButton);
        form.getStyleClass().add("panel");
        form.setPadding(new Insets(18));
        form.setPrefWidth(360);
        return form;
    }

    private void createProject() {
        String name = nameField.getText();
        if (!isAdmin() || name == null || name.isBlank()) {
            return;
        }

        try {
            Project project = controllers.project().createProject(name);
            session.selectProject(project);
            nameField.clear();
            refreshProjects();
            show(currentUsername() + " - " + name + " 추가되었습니다.");
        } catch (Exception ignored) {
        }
    }

    private void refreshProjects() {
        projects.setAll(
                controllers.project().findAll().stream().map(UiFormat::projectWithId).toList()
        );
    }
    private void updateActions() {
        if (createButton != null) {
            createButton.setDisable(!isAdmin());
        }
    }

    private boolean isAdmin() {
        Account user = session.getCurrentUser();
        return user != null && user.getRole() == Role.ADMIN;
    }

    private String currentUsername() {
        Account user = session.getCurrentUser();
        return user == null || user.getUsername() == null ? "-" : user.getUsername();
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
        VBox.setVgrow(content, Priority.ALWAYS);
        return panel;
    }

    private void show(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
        System.out.println(message);
    }
}