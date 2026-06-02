package issuetracker.ui.javafx.layout;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.project.Project;
import issuetracker.ui.UiFormat;
import issuetracker.ui.UiSession;
import issuetracker.ui.javafx.component.Sidebar;
import issuetracker.ui.javafx.view.AccountView;
import issuetracker.ui.javafx.view.DashboardView;
import issuetracker.ui.javafx.view.IssueView;
import issuetracker.ui.javafx.view.ProjectView;
import issuetracker.ui.javafx.view.StatisticsView;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.List;

public class MainLayout extends BorderPane {

    private final Sidebar sidebar;
    private final AppControllers controllers;
    private final UiSession session;
    private Sidebar.MenuItem currentMenuItem = Sidebar.MenuItem.DASHBOARD;

    public MainLayout(AppControllers controllers) {
        this.controllers = controllers;
        this.session = new UiSession();

        getStyleClass().add("app-root");

        initializeSession();

        sidebar = new Sidebar(this::showView);
        setTop(createTopBar());
        setLeft(sidebar);
        showView(Sidebar.MenuItem.DASHBOARD);
    }

    private void initializeSession() {
        try {
            List<Account> accounts = controllers.account().findAll();

            Account defaultUser = accounts.stream().filter(account -> account != null && "admin".equalsIgnoreCase(account.getUsername())).findFirst().orElse(accounts.isEmpty() ? null : accounts.get(0));

            if (defaultUser != null) {
                session.selectCurrentUser(defaultUser);
            }

            List<Project> projects = controllers.project().findAll();

            if (!projects.isEmpty()) {
                session.selectProject(projects.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createTopBar() {
        Label userLabel = new Label("Current User");
        userLabel.getStyleClass().add("field-label");

        ComboBox<Account> currentUserBox = new ComboBox<>();
        currentUserBox.setMaxWidth(220);

        Label roleLabel = new Label("Role: -");
        roleLabel.getStyleClass().add("field-label");

        currentUserBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Account account) {
                return UiFormat.account(account);
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        });

        try {
            List<Account> accounts = controllers.account().findAll();
            currentUserBox.getItems().addAll(accounts);

            Account currentUser = session.getCurrentUser();

            if (currentUser != null) {
                currentUserBox.setValue(currentUser);
                roleLabel.setText("Role: " + currentUser.getRole());
            }

        } catch (Exception e) {
            roleLabel.setText("Role: -");
            e.printStackTrace();
        }

        currentUserBox.setOnAction(event -> {
            Account selected = currentUserBox.getValue();

            if (selected == null) {
                return;
            }

            session.selectCurrentUser(selected);
            roleLabel.setText("Role: " + selected.getRole());

            showView(currentMenuItem);
        });

        HBox topBar = new HBox(12, userLabel, currentUserBox, roleLabel);
        topBar.getStyleClass().addAll("top-bar", "panel");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setMinHeight(72);
        topBar.setPrefHeight(72);

        return topBar;
    }

    private void showView(Sidebar.MenuItem menuItem) {
        currentMenuItem = menuItem;
        sidebar.select(menuItem);
        setCenter(createView(menuItem));
    }

    private Node createView(Sidebar.MenuItem menuItem) {
        return switch (menuItem) {
            case DASHBOARD -> new DashboardView(controllers, session);
            case PROJECTS -> new ProjectView(controllers, session);
            case ISSUES -> new IssueView(controllers, session);
            case ACCOUNTS -> new AccountView(controllers, session);
            case STATISTICS -> new StatisticsView(controllers, session);
        };
    }
}