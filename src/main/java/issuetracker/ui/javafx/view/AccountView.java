package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.ui.UiSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Function;

public class AccountView extends VBox {
    private final AppControllers controllers;
    private final UiSession session;
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final Label message = new Label();

    public AccountView(AppControllers controllers, UiSession session) {
        this.controllers = controllers;
        this.session = session;

        getStyleClass().add("content");
        setSpacing(18);
        setPadding(new Insets(28));

        HBox body = new HBox(18, accountTable(), accountForm());
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(label("Accounts", "page-title"), body);
        refreshAccounts();
    }

    private VBox accountTable() {
        TableView<Account> table = new TableView<>(accounts);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(
                column("Username", account -> safe(account.getUsername()), 220),
                column("Role", account -> account.getRole() == null ? "-" : account.getRole().name(), 160)
        );
        VBox panel = panel("Account 목록", table);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox accountForm() {
        TextField username = new TextField();
        username.setPromptText("username");
        ComboBox<Role> role = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        role.getSelectionModel().select(Role.DEV);
        role.setMaxWidth(Double.MAX_VALUE);
        Button create = new Button("Create Account");
        create.getStyleClass().add("primary-button");
        create.setMaxWidth(Double.MAX_VALUE);
        create.setOnAction(event -> createAccount(username, role));
        message.getStyleClass().add("form-message");

        VBox form = new VBox(12, label("Username", "field-label"), username, label("Role", "field-label"), role, create, message);
        form.getStyleClass().add("panel");
        form.setPadding(new Insets(18));
        form.setPrefWidth(360);
        return form;
    }

    private void createAccount(TextField username, ComboBox<Role> role) {
        String name = username.getText();

        if (name == null || name.isBlank()) {
            show("username을 입력하세요.", true);
            return;
        }

        try {
            controllers.account().createAccount(name, role.getValue());
            username.clear();
            refreshAccounts();
            show("Account created.", false);
        } catch (Exception ignored) {
        }
    }

    private void refreshAccounts() {
        accounts.setAll(controllers.account().findAll());
    }

    private TableColumn<Account, String> column(String title, Function<Account, String> mapper, double width) {
        TableColumn<Account, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private String safe(String text) {
        return text == null || text.isBlank() ? "-" : text;
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

    private void show(String text, boolean error) {
        message.setText(text);
        message.getStyleClass().removeAll("error-text", "success-text");
        message.getStyleClass().add(error ? "error-text" : "success-text");
    }
}