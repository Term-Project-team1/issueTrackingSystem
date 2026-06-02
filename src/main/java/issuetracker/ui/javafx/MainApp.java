package issuetracker.ui.javafx;

import issuetracker.AppControllerFactory;
import issuetracker.controller.AppControllers;
import issuetracker.ui.javafx.layout.MainLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static final int DEFAULT_WIDTH = 1360;
    private static final int DEFAULT_HEIGHT = 860;

    @Override
    public void start(Stage stage) {
        AppControllers controllers = AppControllerFactory.create();

        MainLayout mainLayout = new MainLayout(controllers);
        Scene scene = new Scene(mainLayout, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        var stylesheetUrl = getClass().getResource("/ui/style.css");

        if (stylesheetUrl != null) {
            scene.getStylesheets().add(stylesheetUrl.toExternalForm());
        }

        stage.setTitle("Issue Tracking System");
        stage.setMinWidth(1120);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}