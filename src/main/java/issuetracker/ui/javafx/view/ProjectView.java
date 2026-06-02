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

        Label title = new Label("Projects");
        title.getStyleClass().add("page-title");

        HBox body = new HBox(18, createProjectList(), createProjectForm());
        VBox.setVgrow(body, Priority.ALWAYS);

        messageLabel = new Label("");
        messageLabel.getStyleClass().add("field-label");

        getChildren().addAll(title, body, messageLabel);

        refreshProjects();
        updateProjectActions();
    }

    private VBox createProjectList() {
        ListView<String> listView = new ListView<>(projects);
        listView.getStyleClass().add("data-list");

        VBox panel = createPanel("Project 목록", listView);
        HBox.setHgrow(panel, Priority.ALWAYS);

        return panel;
    }

    private VBox createProjectForm() {
        nameField = new TextField();
        nameField.setPromptText("project name");

        createButton = new Button("Create Project");
        createButton.getStyleClass().add("primary-button");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(event -> createProject());

        VBox form = new VBox(
                12,
                fieldLabel("Name"),
                nameField,
                createButton
        );

        form.getStyleClass().add("panel");
        form.setPadding(new Insets(18));
        form.setPrefWidth(360);

        return form;
    }

    private void createProject() {
        if (!isAdmin()) {
            showMessage("프로젝트 생성은 ADMIN만 가능합니다.");
            return;
        }

        String projectName = nameField.getText();

        if (projectName == null || projectName.isBlank()) {
            showMessage("프로젝트 이름을 입력하세요.");
            return;
        }

        try {
            Project createdProject = controllers.project().createProject(projectName);

            session.selectProject(createdProject);

            nameField.clear();
            refreshProjects();

            showMessage(currentUsername() + "이 " + projectName + "을 추가했습니다.");

        } catch (Exception e) {
            showMessage("프로젝트 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshProjects() {
        projects.setAll(
                controllers.project()
                        .findAll()
                        .stream()
                        .map(this::formatProject)
                        .toList()
        );
    }

    private void updateProjectActions() {
        boolean canCreateProject = isAdmin();

        if (createButton != null) {
            createButton.setDisable(!canCreateProject);
        }

        if (canCreateProject) {
            showMessage("현재 계정 " + currentUsername() + "은 프로젝트를 생성할 수 있습니다.");
        } else {
            showMessage("현재 계정 " + currentUsername() + "은 프로젝트를 생성할 수 없습니다. ADMIN만 가능합니다.");
        }
    }

    private String formatProject(Project project) {
        return UiFormat.projectWithId(project);
    }

    private boolean isAdmin() {
        Account currentUser = session.getCurrentUser();

        return currentUser != null
                && currentUser.getRole() == Role.ADMIN;
    }

    private String currentUsername() {
        Account currentUser = session.getCurrentUser();

        if (currentUser == null || currentUser.getUsername() == null) {
            return "-";
        }

        return currentUser.getUsername();
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private VBox createPanel(String title, javafx.scene.Node content) {
        Label titleNode = new Label(title);
        titleNode.getStyleClass().add("panel-title");

        VBox panel = new VBox(14, titleNode, content);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(18));

        VBox.setVgrow(content, Priority.ALWAYS);

        return panel;
    }

    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }

        System.out.println(message);
    }
}