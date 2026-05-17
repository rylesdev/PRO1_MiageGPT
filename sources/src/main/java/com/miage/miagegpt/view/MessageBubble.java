package com.miage.miagegpt.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MessageBubble {

    public final HBox row;
    public final VBox box;
    public final Label messageLabel;
    public final HBox footerBox;
    public final Button copyBtn;

    private MessageBubble(HBox row, VBox box, Label messageLabel, HBox footerBox, Button copyBtn) {
        this.row = row;
        this.box = box;
        this.messageLabel = messageLabel;
        this.footerBox = footerBox;
        this.copyBtn = copyBtn;
    }

    public static MessageBubble forUser(String text, String time) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setId("user-message-row");

        VBox box = new VBox(5);
        box.setMaxWidth(700);
        box.setId("user-message-box");
        box.getStyleClass().add("message-box");

        Label authorLabel = new Label("Vous");
        authorLabel.setId("user-author");
        authorLabel.getStyleClass().add("user-author");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setId("user-message-text");
        messageLabel.getStyleClass().add("message-text");

        Label timeLabel = new Label(time);
        timeLabel.setId("user-time");
        timeLabel.getStyleClass().add("timestamp-label");

        Map<String, Label> labelMap = new HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        box.setUserData(labelMap);

        box.getChildren().addAll(authorLabel, messageLabel, timeLabel);
        row.getChildren().add(box);

        return new MessageBubble(row, box, messageLabel, null, null);
    }

    public static MessageBubble forAI(String time, Supplier<String> textGetter) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setId("ai-message-row");

        VBox box = new VBox(5);
        box.setMaxWidth(700);
        box.setId("ai-message-box");
        box.getStyleClass().add("message-box");

        Label authorLabel = new Label("MiageGPT");
        authorLabel.setId("ai-author");
        authorLabel.getStyleClass().add("ai-author");

        Label messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setId("ai-message-text");
        messageLabel.getStyleClass().add("message-text");

        Label timeLabel = new Label(time);
        timeLabel.setId("ai-time");
        timeLabel.getStyleClass().add("timestamp-label");

        Button copyBtn = new Button("📋");
        copyBtn.getStyleClass().add("copy-btn");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(textGetter.get());
            clipboard.setContent(content);
            copyBtn.setText("✓");
            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500),
                            ev -> copyBtn.setText("📋"))).play();
        });

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footerBox.getChildren().addAll(timeLabel, spacer, copyBtn);

        Map<String, Label> labelMap = new HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        box.setUserData(labelMap);

        box.getChildren().addAll(authorLabel, messageLabel, footerBox);
        row.getChildren().add(box);

        return new MessageBubble(row, box, messageLabel, footerBox, copyBtn);
    }

    public static HBox staticUser(String text) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setId("user-message-row");

        VBox box = new VBox(5);
        box.setMaxWidth(700);
        box.setId("user-message-box");
        box.getStyleClass().add("message-box");

        Label authorLabel = new Label("Vous");
        authorLabel.setId("user-author");
        authorLabel.getStyleClass().add("user-author");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setId("user-message-text");
        messageLabel.getStyleClass().add("message-text");

        Label timeLabel = new Label("");
        timeLabel.setId("user-time");
        timeLabel.getStyleClass().add("timestamp-label");

        Map<String, Label> labelMap = new HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        box.setUserData(labelMap);

        box.getChildren().addAll(authorLabel, messageLabel, timeLabel);
        row.getChildren().add(box);
        return row;
    }

    public static HBox staticAI(String text, boolean isDarkMode) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setId("ai-message-row");

        VBox box = new VBox(5);
        box.setMaxWidth(700);
        box.setId("ai-message-box");
        box.getStyleClass().add("message-box");

        Label authorLabel = new Label("MiageGPT");
        authorLabel.setId("ai-author");
        authorLabel.getStyleClass().add("ai-author");

        TextFlow messageFlow = MarkdownRenderer.renderFlow(text, isDarkMode);

        Label timeLabel = new Label("");
        timeLabel.setId("ai-time");
        timeLabel.getStyleClass().add("timestamp-label");

        Button copyBtn = new Button("📋");
        copyBtn.getStyleClass().add("copy-btn");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
            copyBtn.setText("✓");
            new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500),
                            ev -> copyBtn.setText("📋"))).play();
        });

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footerBox.getChildren().addAll(timeLabel, spacer, copyBtn);

        Map<String, Label> labelMap = new HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("time", timeLabel);
        box.setUserData(labelMap);

        box.getChildren().addAll(authorLabel, messageFlow, footerBox);
        box.getProperties().put("textFlow", messageFlow);
        box.getProperties().put("markdownSource", text);

        row.getChildren().add(box);
        return row;
    }
}
