package issuetracker.ui.javafx.component;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sidebar extends VBox {
    public enum MenuItem {DASHBOARD, PROJECTS, ISSUES, ACCOUNTS, STATISTICS}
    private final Consumer<MenuItem> onMenuSelected;
    private final Map<MenuItem, Button> menuButtons = new EnumMap<>(MenuItem.class);

    public Sidebar(Consumer<MenuItem> onMenuSelected) {
        this.onMenuSelected = onMenuSelected;
        getStyleClass().add("sidebar");
        setPadding(new Insets(28, 18, 18, 18));
        setSpacing(14);
        setPrefWidth(240);

        VBox brand = createBrand();

        Button dashboardButton = createMenuButton(MenuItem.DASHBOARD, "D  Dashboard");
        Button projectButton = createMenuButton(MenuItem.PROJECTS, "P  Projects");
        Button issueButton = createMenuButton(MenuItem.ISSUES, "I  Issues");
        Button accountButton = createMenuButton(MenuItem.ACCOUNTS, "A  Accounts");
        Button statisticsButton = createMenuButton(MenuItem.STATISTICS, "S  Statistics");

        VBox menuBox = new VBox(8, dashboardButton, projectButton, issueButton, accountButton, statisticsButton);
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(brand, menuBox, spacer);
    }

    private VBox createBrand() {
        Label logo = new Label("ITS");
        logo.getStyleClass().add("sidebar-logo");
        Label subtitle = new Label("Issue Tracking System");
        subtitle.getStyleClass().add("sidebar-subtitle");

        VBox brand = new VBox(6, logo, subtitle);
        brand.getStyleClass().add("sidebar-brand");
        brand.setPadding(new Insets(0, 0, 22, 0));

        return brand;
    }

    private Button createMenuButton(MenuItem menuItem, String text) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            if (onMenuSelected != null) {
                onMenuSelected.accept(menuItem);
            }
        });
        menuButtons.put(menuItem, button);

        return button;
    }

    public void select(MenuItem selectedMenuItem) {
        for (Map.Entry<MenuItem, Button> entry : menuButtons.entrySet()) {
            Button button = entry.getValue();
            button.getStyleClass().remove("selected");

            if (entry.getKey() == selectedMenuItem) {
                button.getStyleClass().add("selected");
            }
        }
    }
}