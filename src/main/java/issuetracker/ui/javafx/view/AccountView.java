package issuetracker.ui.javafx.view;

import issuetracker.controller.AppControllers;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.ui.UiSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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

        Label title = new Label("Accounts");
        title.getStyleClass().add("page-title");

        HBox body = new HBox(18, createAccountTable(), createAccountForm());
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(title, body);

        refreshAccounts();
    }

    private VBox createAccountTable() {
        TableView<Account> table = new TableView<>(accounts);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().add(column("Username", account ->
                account.getUsername() != null ? account.getUsername() : "-", 220));

        table.getColumns().add(column("Role", account ->
                account.getRole() != null ? account.getRole().name() : "-", 160));

        VBox panel = createPanel("Account 목록", table);
        HBox.setHgrow(panel, Priority.ALWAYS);

        return panel;
    }

    private VBox createAccountForm() {
        TextField username = new TextField();
        username.setPromptText("username");

        ComboBox<Role> role = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        role.getSelectionModel().select(Role.DEV);
        role.setMaxWidth(Double.MAX_VALUE);

        Button create = new Button("Create Account");
        create.getStyleClass().add("primary-button");
        create.setMaxWidth(Double.MAX_VALUE);

        create.setOnAction(event -> {
            if (username.getText() == null || username.getText().isBlank()) {
                showMessage("username을 입력하세요.", true);
                return;
            }

            try {
                controllers.account().createAccount(username.getText(), role.getValue());

                username.clear();
                refreshAccounts();

                showMessage("Account created.", false);

            } catch (IllegalArgumentException ex) {
                showMessage(ex.getMessage(), true);
            } catch (Exception ex) {
                showMessage("계정 생성 실패: " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        });

        message.getStyleClass().add("form-message");

        VBox form = new VBox(
                12,
                fieldLabel("Username"),
                username,
                fieldLabel("Role"),
                role,
                create,
                message
        );

        form.getStyleClass().add("panel");
        form.setPadding(new Insets(18));
        form.setPrefWidth(360);

        return form;
    }

    private void refreshAccounts() {
        accounts.setAll(controllers.account().findAll());
    }

    private void showMessage(String text, boolean error) {
        message.setText(text);
        message.getStyleClass().removeAll("error-text", "success-text");
        message.getStyleClass().add(error ? "error-text" : "success-text");
    }

    private TableColumn<Account, String> column(String title,
                                                java.util.function.Function<Account, String> mapper,
                                                double width) {
        TableColumn<Account, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        column.setPrefWidth(width);

        return column;
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
}