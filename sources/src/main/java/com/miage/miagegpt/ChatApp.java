package com.miage.miagegpt;

import com.miage.miagegpt.controller.ChatController;
import com.miage.miagegpt.view.ChatView;
import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ChatController controller = new ChatController();
        ChatView view = new ChatView(controller);
        view.initialize(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
