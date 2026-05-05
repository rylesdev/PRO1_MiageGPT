package com.miage.miagegpt;

import com.miage.miagegpt.controller.AdminController;
import com.miage.miagegpt.view.AdminView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class AdminApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        AdminController controller = new AdminController();

        primaryStage.hide();

        AdminView adminView = new AdminView(primaryStage, controller);
        adminView.show();

        adminView.getStage().setOnHidden(e -> Platform.exit());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
