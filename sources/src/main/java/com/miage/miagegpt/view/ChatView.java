package com.miage.miagegpt.view;

import com.miage.miagegpt.controller.ChatController;
import com.miage.miagegpt.model.ConversationManager;
import com.miage.miagegpt.service.APIResponse;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatView {
    private VBox chatBox;
    private VBox welcomeBox;
    private ScrollPane scrollPane;
    private Map<String, VBox> conversations = new HashMap<>();
    private Map<String, LocalDateTime> conversationDates = new HashMap<>();
    private Map<String, Integer> conversationMessageCounts = new HashMap<>();
    private Map<String, Long> conversationStartTimes = new HashMap<>();
    private String currentConversation = null;
    private ListView<String> historyList;
    private Label dateTimeLabel;
    private Label statsLabel;
    private BorderPane centerPane;
    private VBox inputArea;
    private BorderPane root;
    private Button newChatBtn;
    private Button homeBtn;
    private javafx.scene.layout.StackPane summarizeBtn;
    private TextArea inputField;
    private Button sendBtn;

    private Map<String, String> conversationHistory = new HashMap<>();

    private javafx.animation.Timeline typingTimeline;
    private HBox typingMessageRow;

    private boolean isDarkMode = false;
    private Button themeToggleBtn;
    private Label connectionIndicator;
    private ChatController controller;
    private Button exportBtn;
    private String lastPingValue = "Ping: --ms";

    private Thread currentMessageThread = null;
    private boolean stopMessageDisplay = false;

    public ChatView(ChatController controller) {
        this.controller = controller;
    }

    public void initialize(Stage primaryStage) {
        VBox sidebar = createSidebar();

        chatBox = new VBox(20);
        chatBox.setPadding(new Insets(20));
        chatBox.setStyle("-fx-background-color: #343541;");

        scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #343541; -fx-background-color: #343541;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        dateTimeLabel = new Label();
        dateTimeLabel.setStyle("-fx-text-fill: #8E8EA0; -fx-font-size: 12px; -fx-padding: 10;");
        updateDateTimeLabel();

        statsLabel = new Label();
        statsLabel.setStyle("-fx-text-fill: #8E8EA0; -fx-font-size: 12px; -fx-padding: 10;");

        summarizeBtn = createGradientButton("✨ Résumer la conversation", this::summarizeConversation);
        HBox.setMargin(summarizeBtn, new Insets(0, 0, 0, 42));

        exportBtn = new Button("💾 Exporter");
        exportBtn.setOnAction(e -> exportConversation());
        exportBtn.setStyle("-fx-font-size: 12px; -fx-padding: 10 12; -fx-font-weight: bold;");
        addHoverScaleEffect(exportBtn);
        updateExportButtonStyle();
        HBox.setMargin(exportBtn, new Insets(0, 0, 0, 16));
        HBox.setMargin(statsLabel, new Insets(0, 0, 0, 24));

        themeToggleBtn = new Button();
        themeToggleBtn.setPrefSize(44, 44);
        themeToggleBtn.setMinSize(44, 44);
        themeToggleBtn.setMaxSize(44, 44);
        updateThemeButtonLabel();
        addHoverScaleEffect(themeToggleBtn);
        themeToggleBtn.setOnAction(e -> toggleTheme());
        updateThemeButtonStyle();

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 15, 15, 15));
        header.setStyle("-fx-background-color: #343541;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(dateTimeLabel, summarizeBtn, exportBtn, statsLabel, spacer,
                themeToggleBtn);

        centerPane = new BorderPane();
        centerPane.setTop(header);
        centerPane.setCenter(scrollPane);
        centerPane.setStyle("-fx-background-color: #343541;");

        showWelcomeScreen();
        inputArea = createInputArea();
        root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(centerPane);
        applyTheme();
        updateThemeButtonStyle();

        loadSavedConversations();

        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setTitle("MiageGPT");
        primaryStage.setScene(scene);
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon_texte.png")));
        } catch (Exception ignored) {
        }

        primaryStage.setOnCloseRequest(event -> saveAllConversations());

        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setMinWidth(260);
        sidebar.setMaxWidth(260);
        sidebar.setStyle("-fx-background-color: #202123;");

        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/icon_logo.png")));
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(240);
        logoView.setSmooth(true);

        StackPane logoContainer = new StackPane(logoView);
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setMaxWidth(Double.MAX_VALUE);
        logoContainer.setPadding(new Insets(0, 0, 6, 0));

        newChatBtn = new Button("+ Nouvelle conversation");
        newChatBtn.setMaxWidth(Double.MAX_VALUE);
        newChatBtn.setOnAction(e -> {

            int conversationNumber = conversations.size() + 1;
            String baseName = "Conversation ";
            String conversationName = baseName + conversationNumber;
            while (conversations.containsKey(conversationName)) {
                conversationNumber++;
                conversationName = baseName + conversationNumber;
            }

            VBox newChatBox = new VBox(20);
            newChatBox.setPadding(new Insets(20));
            newChatBox.setStyle("-fx-background-color: #343541;");
            conversations.put(conversationName, newChatBox);
            conversationDates.put(conversationName, LocalDateTime.now());
            conversationStartTimes.put(conversationName, System.currentTimeMillis());
            conversationMessageCounts.put(conversationName, 0);

            historyList.getItems().add(0, conversationName);

            switchConversation(conversationName);
            historyList.refresh();
            saveCurrentConversation();

            root.setBottom(inputArea);
        });
        addHoverScaleEffect(newChatBtn);
        updateNewChatButtonStyle();

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #202123;");
        topBar.getChildren().add(newChatBtn);
        HBox.setHgrow(newChatBtn, Priority.ALWAYS);

        HBox connBox = new HBox(8);
        connBox.setAlignment(Pos.CENTER);
        Label connLabel = new Label("Connecté");
        connLabel.setStyle("-fx-text-fill: #10A37F; -fx-font-size: 11px;");
        connBox.getChildren().add(connLabel);
        connectionIndicator = connLabel;

        Label pingLabel = new Label("Ping: --ms");
        pingLabel.setStyle("-fx-text-fill: #8E8EA0; -fx-font-size: 10px;");
        pingLabel.setPadding(new Insets(0, 10, 0, 0));

        HBox bottomBar = new HBox(15);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setStyle("-fx-background-color: #202123;");
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.getChildren().addAll(connBox, pingLabel);

        homeBtn = new Button("🏠 Page d'accueil");
        addHoverScaleEffect(homeBtn);
        homeBtn.setStyle(
                "-fx-background-color: #2A2B32; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: #565869; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;");
        homeBtn.setOnAction(e -> {
            currentConversation = null;
            showWelcomeScreen();
            applyTheme();
            root.setBottom(null);
        });

        Label historyLabel = new Label("Historique");
        historyLabel.setStyle("-fx-text-fill: #8E8EA0; -fx-font-size: 11px; -fx-padding: 10 5 5 5;");

        historyList = new ListView<>();

        historyList.setStyle(
                "-fx-background-color: #202123; " +
                        "-fx-control-inner-background: #202123; " +
                        "-fx-text-fill: white;");
        historyList.setCellFactory(lv -> new ListCell<String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    // Clear any mouse handlers that may have been set when the cell was reused
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                    setOnMouseClicked(null);
                    setOnMousePressed(null);
                } else {
                    String textColor = isDarkMode ? "#ECECF1" : "#0F172A";
                    String hoverBg = isDarkMode ? "#2A2B32" : "#E1ECF4";
                    String activeBg = isDarkMode ? "#2A2B32" : "#D7E8F5";
                    String indicatorColor = isDarkMode ? "#19C37D" : "#0F766E";

                    HBox cellBox = new HBox(8);
                    cellBox.setAlignment(Pos.CENTER_LEFT);

                    HBox leftBox = new HBox(8);
                    leftBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(leftBox, Priority.ALWAYS);

                    Label textLabel = new Label(item);
                    textLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px;");
                    textLabel.setMaxWidth(180);
                    textLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
                    HBox.setHgrow(textLabel, Priority.ALWAYS);

                    if (item.equals(currentConversation)) {
                        Label indicator = new Label("●");
                        indicator.setStyle("-fx-text-fill: " + indicatorColor + "; -fx-font-size: 10px;");
                        leftBox.getChildren().addAll(indicator, textLabel);
                        setStyle("-fx-background-color: " + activeBg + "; -fx-padding: 8 10; -fx-cursor: hand;");
                    } else {
                        leftBox.getChildren().add(textLabel);
                        setStyle("-fx-background-color: transparent; -fx-padding: 8 10; -fx-cursor: hand;");
                    }

                    cellBox.getChildren().add(leftBox);

                        HBox actionBox = new HBox(5);
                        actionBox.setAlignment(Pos.CENTER_RIGHT);
                        actionBox.setVisible(false);
                        actionBox.setManaged(false);

                        String baseActionStyle = "-fx-background-color: transparent; -fx-text-fill: #8E8EA0; -fx-font-size: 14px; -fx-padding: 2 6; -fx-cursor: hand;";
                        String renameHoverStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 2 6; -fx-cursor: hand;";
                        String deleteHoverStyle = "-fx-background-color: transparent; -fx-text-fill: #FF6B6B; -fx-font-size: 14px; -fx-padding: 2 6; -fx-cursor: hand;";

                        Button renameBtn = new Button("✎");
                        addHoverScaleEffect(renameBtn);
                        renameBtn.setStyle(baseActionStyle);
                        renameBtn.setOnMouseEntered(e -> renameBtn.setStyle(renameHoverStyle));
                        renameBtn.setOnMouseExited(e -> renameBtn.setStyle(baseActionStyle));
                        renameBtn.setOnAction(e -> {
                        e.consume();
                        renameConversation(item);
                        });

                        Button deleteBtn = new Button("🗑");
                        addHoverScaleEffect(deleteBtn);
                        deleteBtn.setStyle(baseActionStyle);
                        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(deleteHoverStyle));
                        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(baseActionStyle));
                        deleteBtn.setOnAction(e -> {
                        e.consume();
                        deleteConversation(item);
                        });

                        actionBox.getChildren().addAll(renameBtn, deleteBtn);
                    cellBox.getChildren().add(actionBox);

                    setGraphic(cellBox);
                    setText(null);

                    setOnMouseEntered(e -> {
                        actionBox.setVisible(true);
                        actionBox.setManaged(true);
                        if (!item.equals(currentConversation)) {
                            setStyle("-fx-background-color: " + hoverBg + "; -fx-padding: 8 10; -fx-cursor: hand;");
                        }
                    });
                    setOnMouseExited(e -> {
                        actionBox.setVisible(false);
                        actionBox.setManaged(false);
                        if (!item.equals(currentConversation)) {
                            setStyle("-fx-background-color: transparent; -fx-padding: 8 10; -fx-cursor: hand;");
                        }
                    });
                }
            }
        });

        historyList.setOnMouseClicked(e -> {
            String selectedConv = historyList.getSelectionModel().getSelectedItem();
            if (selectedConv != null && !selectedConv.isEmpty() && e.getClickCount() == 1
                    && conversations.containsKey(selectedConv)) {
                switchConversation(selectedConv);
                historyList.refresh();

                if (root.getBottom() == null) {
                    root.setBottom(inputArea);
                }
            }
        });

        HBox homeBtnContainer = new HBox();
        homeBtnContainer.setAlignment(Pos.CENTER);
        homeBtnContainer.getChildren().add(homeBtn);

        VBox.setVgrow(historyList, Priority.ALWAYS);
        sidebar.getChildren().addAll(logoContainer, homeBtnContainer, topBar, historyLabel, historyList, bottomBar);

        return sidebar;
    }

    private VBox createInputArea() {
        VBox inputArea = new VBox(10);
        inputArea.setPadding(new Insets(20, 20, 20, 20));
        inputArea.setAlignment(Pos.CENTER);

        applyInputAreaBackground(inputArea);

        inputField = new TextArea();
        inputField.setPromptText("Envoyez un message...");
        inputField.setPrefRowCount(3);
        inputField.setWrapText(true);
        inputField.setMaxHeight(150);
        inputField.setMaxWidth(800);
        updateInputFieldStyle();

        sendBtn = new Button("➤");
        sendBtn.setStyle(
                "-fx-background-color: #000000ff; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 12 12; " +
                        "-fx-min-width: 46; " +
                        "-fx-min-height: 46; " +
                        "-fx-pref-width: 46; " +
                        "-fx-pref-height: 46; " +
                        "-fx-background-radius: 23; " +
                        "-fx-cursor: hand;");

        HBox inputBox = new HBox(10, inputField, sendBtn);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMaxWidth(800);

        Runnable sendMessage = () -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                inputField.clear();
                showTypingIndicator();

                javafx.application.Platform.runLater(() -> {
                    startSendButtonLoader();

                    currentMessageThread = new Thread(() -> {
                        try {
                            String history = conversationHistory.getOrDefault(currentConversation, "");
                            APIResponse apiResponse = controller.sendMessageWithPing(text, history);
                            String botResponse = apiResponse.content;
                            long pingMs = apiResponse.pingMs;

                            javafx.application.Platform.runLater(() -> {
                                hideTypingIndicator();

                                VBox sidebar = (VBox) root.getLeft();
                                HBox bottomBar = (HBox) sidebar.getChildren().get(sidebar.getChildren().size() - 1);
                                if (bottomBar.getChildren().size() >= 2) {
                                    Label pingLabel = (Label) bottomBar.getChildren().get(1);
                                    lastPingValue = "Ping: " + pingMs + "ms";
                                    pingLabel.setText(lastPingValue);
                                }

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {
                                }
                                addAIMessage(botResponse, text, history);
                                conversationHistory.put(currentConversation,
                                        controller.buildTurn(history, text, botResponse));
                                saveCurrentConversation();
                            });
                        } catch (Exception e) {
                            javafx.application.Platform.runLater(() -> {
                                hideTypingIndicator();
                                stopSendButtonLoader();
                                addAIMessage("Erreur: " + e.getMessage());
                            });
                        }
                    });
                    currentMessageThread.start();
                });
            }
        };
        applySendHandler(sendMessage);

        inputArea.getChildren().add(inputBox);
        return inputArea;
    }

    private void addUserMessage(String text) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER_RIGHT);
        messageRow.setPadding(new Insets(10, 0, 10, 0));
        messageRow.setId("user-message-row");

        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(700);
        messageBox.setId("user-message-box");

        String messageBg = isDarkMode ? "#444654" : "#E8F4F8";
        String textColor = isDarkMode ? "#ECECF1" : "#1F2937";
        String secondaryColor = isDarkMode ? "#8E8EA0" : "#6B7280";

        messageBox.setStyle(
                "-fx-background-color: " + messageBg + "; " +
                        "-fx-padding: 15; " +
                        "-fx-background-radius: 10;");

        Label authorLabel = new Label("Vous");
        authorLabel.setId("user-author");
        authorLabel.setStyle("-fx-text-fill: #19C37D; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setId("user-message-text");
        messageLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");

        LocalDateTime now = LocalDateTime.now();
        String timeStamp = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(timeStamp);
        timeLabel.setId("user-time");
        timeLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px;");

        messageBox.getChildren().addAll(authorLabel, messageLabel, timeLabel);

        java.util.Map<String, Label> labelMap = new java.util.HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        messageBox.setUserData(labelMap);

        messageRow.getChildren().add(messageBox);

        chatBox.getChildren().add(messageRow);

        int count = conversationMessageCounts.getOrDefault(currentConversation, 0);
        conversationMessageCounts.put(currentConversation, count + 1);
        updateStatsLabel();
        scrollToBottom();
        javafx.application.Platform.runLater(this::scrollToBottom);
    }

    private void addAIMessage(String text) {
        addAIMessage(text, null, null);
    }

    private void addAIMessage(String text, String sourcePrompt, String historyBefore) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER_LEFT);
        messageRow.setPadding(new Insets(10, 0, 10, 0));
        messageRow.setId("ai-message-row");

        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(700);
        messageBox.setId("ai-message-box");

        String messageBg = isDarkMode ? "#444654" : "#E8F4F8";
        String textColor = isDarkMode ? "#ECECF1" : "#1F2937";
        String secondaryColor = isDarkMode ? "#8E8EA0" : "#6B7280";

        messageBox.setStyle(
                "-fx-background-color: " + messageBg + "; " +
                        "-fx-padding: 15; " +
                        "-fx-background-radius: 10;");

        Label authorLabel = new Label("MiageGPT");
        authorLabel.setId("ai-author");
        authorLabel.setStyle("-fx-text-fill: #10A37F; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setId("ai-message-text");
        messageLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");

        LocalDateTime now = LocalDateTime.now();
        String timeStamp = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(timeStamp);
        timeLabel.setId("ai-time");
        timeLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px;");

        messageBox.getChildren().addAll(authorLabel, messageLabel, timeLabel);

        java.util.Map<String, Label> labelMap = new java.util.HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        messageBox.setUserData(labelMap);

        final String[] currentText = { text };
        Button copyBtn = new Button("📋");
        copyBtn.setStyle(
                "-fx-font-size: 11px; -fx-padding: 4 6; -fx-background-radius: 4; -fx-background-color: #6B7280; -fx-text-fill: white;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(currentText[0]);
            clipboard.setContent(content);
            copyBtn.setText("✓");
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500),
                            ev -> copyBtn.setText("📋")));
            timeline.play();
        });

        final Button[] reloadBtn = { null };
        if (sourcePrompt != null && historyBefore != null) {
            reloadBtn[0] = new Button("↻");
            reloadBtn[0].setStyle(
                    "-fx-font-size: 11px; -fx-padding: 4 6; -fx-background-radius: 4; -fx-background-color: #10A37F; -fx-text-fill: white;");
            reloadBtn[0].setTooltip(new Tooltip("Générer une nouvelle version"));
            reloadBtn[0].setDisable(true);

            reloadBtn[0].setOnAction(e -> {
                reloadBtn[0].setDisable(true);
                reloadBtn[0].setText("…");
                String previousResponse = currentText[0];

                new Thread(() -> {
                    try {
                        APIResponse apiResponse = controller.sendMessageWithPing(sourcePrompt,
                                historyBefore);
                        String newResponse = apiResponse.content;
                        long pingMs = apiResponse.pingMs;

                        javafx.application.Platform.runLater(() -> {

                            VBox sidebar = (VBox) root.getLeft();
                            HBox bottomBar = (HBox) sidebar.getChildren().get(sidebar.getChildren().size() - 1);
                            if (bottomBar.getChildren().size() >= 2) {
                                Label pingLabel = (Label) bottomBar.getChildren().get(1);
                                lastPingValue = "Ping: " + pingMs + "ms";
                                pingLabel.setText(lastPingValue);
                            }

                            currentText[0] = newResponse;

                            swapToLabel(messageBox, messageLabel);
                            Thread displayThread = new Thread(() -> {
                                stopMessageDisplay = false;
                                for (int i = 0; i <= newResponse.length(); i++) {
                                    if (stopMessageDisplay) {
                                        break;
                                    }
                                    final int index = i;
                                    javafx.application.Platform.runLater(() -> {
                                        messageLabel.setText(newResponse.substring(0, index));
                                        scrollToBottom();
                                    });
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException ignored) {
                                    }
                                }

                                javafx.application.Platform.runLater(() -> {

                                    String fullHistory = conversationHistory.getOrDefault(currentConversation,
                                            historyBefore);
                                    String oldTurn = controller.buildTurn(historyBefore, sourcePrompt,
                                            previousResponse);
                                    String newTurn = controller.buildTurn(historyBefore, sourcePrompt, newResponse);

                                    String updatedHistory;
                                    if (fullHistory.contains(oldTurn)) {
                                        updatedHistory = fullHistory.replaceFirst(
                                                java.util.regex.Pattern.quote(oldTurn),
                                                java.util.regex.Matcher.quoteReplacement(newTurn));
                                    } else {
                                        String oldAssistantLine = (historyBefore != null && !historyBefore.isEmpty())
                                                ? historyBefore + "\nMiageGPT: " + previousResponse
                                                : "MiageGPT: " + previousResponse;
                                        String newAssistantLine = (historyBefore != null && !historyBefore.isEmpty())
                                                ? historyBefore + "\nMiageGPT: " + newResponse
                                                : "MiageGPT: " + newResponse;

                                        if (fullHistory.contains(oldAssistantLine)) {
                                            updatedHistory = fullHistory.replaceFirst(
                                                    java.util.regex.Pattern.quote(oldAssistantLine),
                                                    java.util.regex.Matcher.quoteReplacement(newAssistantLine));
                                        } else if (fullHistory.equals(historyBefore) || fullHistory.isEmpty()) {
                                            updatedHistory = newTurn;
                                        } else {
                                            updatedHistory = fullHistory;
                                        }
                                    }
                                    conversationHistory.put(currentConversation, updatedHistory);
                                    saveCurrentConversation();

                                    reloadBtn[0].setText("↻");
                                    reloadBtn[0].setDisable(false);
                                    copyBtn.setText("📋");

                                    replaceWithClickableText(messageBox, messageLabel, currentText[0]);
                                });
                            });
                            displayThread.start();
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            reloadBtn[0].setText("↻");
                            reloadBtn[0].setDisable(false);
                            copyBtn.setText("📋");
                            addAIMessage("Erreur: " + ex.getMessage());
                        });
                    }
                }).start();
            });
        }

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_RIGHT);
        Region spacerFooter = new Region();
        HBox.setHgrow(spacerFooter, Priority.ALWAYS);
        Region gap = new Region();
        gap.setMinWidth(6);
        if (reloadBtn[0] != null) {
            footerBox.getChildren().addAll(timeLabel, spacerFooter, reloadBtn[0], gap, copyBtn);
        } else {
            footerBox.getChildren().addAll(timeLabel, spacerFooter, copyBtn);
        }

        messageBox.getChildren().clear();
        messageBox.getChildren().addAll(authorLabel, messageLabel, footerBox);

        messageRow.getChildren().add(messageBox);

        chatBox.getChildren().add(messageRow);

        conversationMessageCounts.put(currentConversation,
                conversationMessageCounts.getOrDefault(currentConversation, 0) + 1);
        updateStatsLabel();

        scrollToBottom();
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            scrollToBottom();
        });

        currentMessageThread = new Thread(() -> {
            stopMessageDisplay = false;
            for (int i = 0; i <= currentText[0].length(); i++) {
                if (stopMessageDisplay) {
                    break;
                }
                final int index = i;
                javafx.application.Platform.runLater(() -> {
                    messageLabel.setText(currentText[0].substring(0, index));
                    scrollToBottom();
                });
                try {

                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }

            javafx.application.Platform.runLater(() -> {
                stopSendButtonLoader();

                if (reloadBtn[0] != null) {
                    reloadBtn[0].setDisable(false);
                }

                replaceWithClickableText(messageBox, messageLabel, currentText[0]);
            });
        });

        currentMessageThread.start();
    }

    private static final java.util.regex.Pattern URL_PATTERN = java.util.regex.Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)");

    private javafx.scene.text.TextFlow createClickableTextFlow(String text) {
        javafx.scene.text.TextFlow textFlow = new javafx.scene.text.TextFlow();
        textFlow.setLineSpacing(2);
        textFlow.setMaxWidth(670);

        String textColor = isDarkMode ? "#ECECF1" : "#1F2937";

        java.util.regex.Matcher matcher = URL_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {

            if (matcher.start() > lastEnd) {
                javafx.scene.text.Text beforeText = new javafx.scene.text.Text(
                        text.substring(lastEnd, matcher.start()));
                beforeText.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 14px;");
                textFlow.getChildren().add(beforeText);
            }

            String url = matcher.group();
            Hyperlink link = new Hyperlink(url);
            link.setStyle("-fx-text-fill: #58A6FF; -fx-font-size: 14px; -fx-border-width: 0; -fx-padding: 0;");
            link.setOnAction(ev -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            textFlow.getChildren().add(link);

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            javafx.scene.text.Text remainingText = new javafx.scene.text.Text(text.substring(lastEnd));
            remainingText.setStyle("-fx-fill: " + textColor + "; -fx-font-size: 14px;");
            textFlow.getChildren().add(remainingText);
        }

        return textFlow;
    }

    private void replaceWithClickableText(VBox messageBox, Label messageLabel, String text) {
        if (!URL_PATTERN.matcher(text).find())
            return;

        javafx.scene.text.TextFlow textFlow = createClickableTextFlow(text);
        int idx = messageBox.getChildren().indexOf(messageLabel);
        if (idx >= 0) {
            messageBox.getChildren().set(idx, textFlow);
            messageBox.getProperties().put("textFlow", textFlow);
        }
    }

    private void swapToLabel(VBox messageBox, Label messageLabel) {
        Object flowObj = messageBox.getProperties().get("textFlow");
        if (flowObj instanceof javafx.scene.text.TextFlow) {
            int idx = messageBox.getChildren().indexOf(flowObj);
            if (idx >= 0) {
                messageBox.getChildren().set(idx, messageLabel);
            }
            messageBox.getProperties().remove("textFlow");
        }
    }

    private void applySendHandler(Runnable handler) {
        if (sendBtn != null) {
            sendBtn.setOnAction(e -> handler.run());
        }
        if (inputField != null) {
            inputField.setOnKeyPressed(e -> {
                if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
                    e.consume();
                    handler.run();
                }
            });
        }
    }

    private void switchConversation(String conversationName) {

        if (currentConversation != null && conversations.containsKey(currentConversation)) {
            conversations.put(currentConversation, chatBox);
        }

        currentConversation = conversationName;
        chatBox = conversations.get(conversationName);

        if (chatBox == null) {
            chatBox = new VBox(20);
            chatBox.setPadding(new Insets(20));
            chatBox.setStyle("-fx-background-color: #343541;");
            conversations.put(conversationName, chatBox);
            conversationDates.put(conversationName, LocalDateTime.now());
            conversationStartTimes.put(conversationName, System.currentTimeMillis());
            conversationMessageCounts.put(conversationName, 0);

            conversationHistory.put(conversationName, "");
        }

        scrollPane.setContent(chatBox);
        updateDateTimeLabel();
        updateStatsLabel();

        summarizeBtn.setVisible(true);
        exportBtn.setVisible(true);

        applyTheme();
        scrollToBottom();
    }

    private void renameConversation(String oldName) {
        final int MAX_NAME_LEN = 15;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Renommer la conversation");
        dialog.setHeaderText("Entrez un nouveau nom pour cette conversation :");

        ButtonType okButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(oldName);
        nameField.setPromptText("Nom");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px;");
        errorLabel.setWrapText(true);

        VBox content = new VBox(8);
        content.getChildren().addAll(new Label("Nom :"), nameField, errorLabel);
        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        Runnable refreshValidation = () -> {
            String trimmed = nameField.getText() == null ? "" : nameField.getText().trim();
            boolean tooLong = trimmed.length() > MAX_NAME_LEN;
            boolean empty = trimmed.isEmpty();
            boolean unchanged = trimmed.equals(oldName);
            boolean duplicate = !empty && !unchanged && conversations.containsKey(trimmed);

            if (tooLong) {
                errorLabel.setText("Le nom est trop long. Maximum : " + MAX_NAME_LEN + " caractères.");
            } else if (duplicate) {
                errorLabel.setText("Une conversation porte déjà ce nom.");
            } else if (empty) {
                errorLabel.setText("Le nom ne peut pas être vide.");
            } else if (unchanged) {
                errorLabel.setText("Modifiez le nom avant de valider.");
            } else {
                errorLabel.setText("");
            }

            okButton.setDisable(tooLong || empty || unchanged || duplicate);
        };

        nameField.textProperty().addListener((obs, oldValue, newValue) -> refreshValidation.run());
        refreshValidation.run();

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                String trimmed = nameField.getText() == null ? "" : nameField.getText().trim();
                return trimmed;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty() && !newName.equals(oldName)) {
                int index = historyList.getItems().indexOf(oldName);
                if (index >= 0) {
                    historyList.getItems().set(index, newName);
                }

                VBox conv = conversations.remove(oldName);
                LocalDateTime date = conversationDates.remove(oldName);
                String history = conversationHistory.remove(oldName);
                Long startTime = conversationStartTimes.remove(oldName);
                Integer msgCount = conversationMessageCounts.remove(oldName);

                if (conv != null) {
                    conversations.put(newName, conv);
                }
                if (date != null) {
                    conversationDates.put(newName, date);
                }
                if (history != null) {
                    conversationHistory.put(newName, history);
                }
                if (startTime != null) {
                    conversationStartTimes.put(newName, startTime);
                }
                if (msgCount != null) {
                    conversationMessageCounts.put(newName, msgCount);
                }

                controller.renameConversation(oldName, newName, date, history,
                        null, msgCount != null ? msgCount : 0);

                if (currentConversation.equals(oldName)) {
                    currentConversation = newName;
                }

                historyList.refresh();
            }
        });
    }

    private void deleteConversation(String conversationName) {

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer la conversation");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer cette conversation ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                historyList.getItems().remove(conversationName);

                conversations.remove(conversationName);
                conversationDates.remove(conversationName);
                conversationHistory.remove(conversationName);

                controller.deleteConversation(conversationName);

                if (currentConversation.equals(conversationName)) {
                    if (!historyList.getItems().isEmpty()) {
                        String newConv = historyList.getItems().get(0);
                        switchConversation(newConv);
                    } else {

                        currentConversation = null;
                        showWelcomeScreenEmptyHistory();
                        root.setBottom(null);
                    }
                }

                historyList.refresh();
            }
        });
    }

    private void updateDateTimeLabel() {
        LocalDateTime date = conversationDates.get(currentConversation);
        if (date != null && currentConversation != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dateTimeLabel.setText("Créée le : " + date.format(formatter));
        } else {
            dateTimeLabel.setText("");
        }
    }

    


    private void showWelcomeScreen() {
        String bgColor = isDarkMode ? "#343541" : "#FFFFFF";
        String textColor = isDarkMode ? "white" : "#1F2937";
        String secondaryColor = isDarkMode ? "#8E8EA0" : "#6B7280";

        welcomeBox = new VBox(25);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(100));
        welcomeBox.setStyle("-fx-background-color: " + bgColor + ";");

        Label title = new Label("Bienvenue sur MiageGPT");
        title.setFont(Font.font("System", FontWeight.BOLD, 48));
        title.setStyle("-fx-text-fill: " + textColor + ";");

        Label subtitle = new Label("Ton assistant dédié à la MIAGE et à l'AMS");
        subtitle.setFont(Font.font("System", 26));
        subtitle.setStyle("-fx-text-fill: " + secondaryColor + ";");

        Label hint = new Label("Crée une nouvelle conversation pour commencer");
        hint.setFont(Font.font("System", 16));
        hint.setStyle("-fx-text-fill: " + secondaryColor + ";");

        javafx.scene.text.Text icon = new javafx.scene.text.Text("💬");
        icon.setFont(Font.font("System", 100));
        icon.setFill(isDarkMode ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.web("#111827"));

        welcomeBox.getChildren().addAll(icon, title, subtitle, hint);
        scrollPane.setContent(welcomeBox);

        dateTimeLabel.setText("");
        statsLabel.setText("");

        summarizeBtn.setVisible(false);
        exportBtn.setVisible(false);
    }

    private void showWelcomeScreenEmptyHistory() {

        showWelcomeScreen();
        historyList.getItems().clear();
        historyList.refresh();
    }

    private void scrollToBottom() {
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    private void showTypingIndicator() {
        javafx.application.Platform.runLater(() -> {

            if (sendBtn != null) {
                double baseRadius = sendBtn.getWidth() / 2.0 + 2;
                javafx.scene.shape.Circle borderCircle = new javafx.scene.shape.Circle(baseRadius);
                borderCircle.setStroke(javafx.scene.paint.Color.web("#2563EB"));
                borderCircle.setStrokeWidth(2);
                borderCircle.setFill(null);
                borderCircle.setManaged(false);
                borderCircle.setMouseTransparent(true);
                borderCircle.setLayoutX(sendBtn.getLayoutX() + sendBtn.getWidth() / 2.0);
                borderCircle.setLayoutY(sendBtn.getLayoutY() + sendBtn.getHeight() / 2.0);
                if (sendBtn.getParent() instanceof HBox) {
                    HBox parentBox = (HBox) sendBtn.getParent();
                    if (!parentBox.getChildren().contains(borderCircle)) {
                        parentBox.getChildren().add(borderCircle);
                    }
                    borderCircle.toFront();
                    sendBtn.toFront();
                }

                javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(
                        javafx.util.Duration.seconds(0.6), borderCircle);
                rotate.setByAngle(360);
                rotate.setCycleCount(javafx.animation.Animation.INDEFINITE);
                rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
                rotate.play();

                javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(
                        javafx.util.Duration.seconds(0.8), borderCircle);
                pulse.setFromY(0.9);
                pulse.setToY(1.2);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                pulse.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

                javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(rotate,
                        pulse);
                parallelTransition.play();

                sendBtn.setUserData(parallelTransition);
                sendBtn.getProperties().put("borderCircle", borderCircle);
            }

            HBox messageRow = new HBox();
            messageRow.setAlignment(Pos.CENTER_LEFT);
            messageRow.setPadding(new Insets(10, 0, 10, 0));

            String msgBg = isDarkMode ? "#444654" : "#E8F4F8";
            String authorColor = isDarkMode ? "#10A37F" : "#0C4A34";
            String dotColor = authorColor;

            VBox messageBox = new VBox(5);
            messageBox.setMaxWidth(700);
            messageBox.setStyle(
                    "-fx-background-color: " + msgBg + "; " +
                            "-fx-padding: 15; " +
                            "-fx-background-radius: 10;");

            Label authorLabel = new Label("MiageGPT");
            authorLabel.setStyle("-fx-text-fill: " + authorColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");

            HBox typingBox = new HBox(5);
            typingBox.setAlignment(Pos.CENTER_LEFT);

            Label dot1 = new Label("●");
            dot1.setStyle("-fx-text-fill: " + dotColor + "; -fx-font-size: 10px;");
            Label dot2 = new Label("●");
            dot2.setStyle("-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.6;");
            Label dot3 = new Label("●");
            dot3.setStyle("-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.3;");

            typingBox.getChildren().addAll(dot1, dot2, dot3);

            messageBox.getChildren().addAll(authorLabel, typingBox);
            messageRow.getChildren().add(messageBox);

            typingMessageRow = messageRow;
            chatBox.getChildren().add(messageRow);
            scrollToBottom();

            typingTimeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(300),
                            e -> {
                                dot1.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.3;");
                                dot2.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 1.0;");
                                dot3.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.6;");
                            }),
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(600),
                            e -> {
                                dot1.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 1.0;");
                                dot2.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.3;");
                                dot3.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.6;");
                            }),
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(900),
                            e -> {
                                dot1.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.3;");
                                dot2.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 0.6;");
                                dot3.setStyle(
                                        "-fx-text-fill: " + dotColor + "; -fx-font-size: 10px; -fx-opacity: 1.0;");
                            }));
            typingTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            typingTimeline.play();
        });
    }

    private void updateStatsLabel() {
        if (currentConversation == null || statsLabel == null) {
            return;
        }

        int messageCount = conversationMessageCounts.getOrDefault(currentConversation, 0);

        String statsText = "📊 " + messageCount +
                " message" + (messageCount > 1 ? "s" : "");
        statsLabel.setText(statsText);
    }

    private void summarizeConversation() {
        if (currentConversation == null) {
            return;
        }

        String history = conversationHistory.getOrDefault(currentConversation, "");
        if (history.isEmpty()) {
            addAIMessage("Il n'y a pas assez de contenu pour résumer.");
            return;
        }

        String summaryPrompt = "Résume brièvement la conversation suivante en 2-3 phrases:\n\n" + history;

        showTypingIndicator();
        new Thread(() -> {
            try {
                setConnectionStatus(true);
                String summary = controller.sendMessage(summaryPrompt, history);

                Thread.sleep(500);

                javafx.application.Platform.runLater(() -> {
                    hideTypingIndicator();
                    setConnectionStatus(true);

                    String newHistory = history;
                    if (!newHistory.isEmpty()) {
                        newHistory += "\n";
                    }
                    newHistory += "MiageGPT: " + summary;
                    conversationHistory.put(currentConversation, newHistory);
                    saveCurrentConversation();

                    addAIMessage("**Résumé:** " + summary);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    hideTypingIndicator();
                    setConnectionStatus(false);
                    addAIMessage("Erreur lors du résumé: " + e.getMessage());
                });
            }
        }).start();
    }

    private StackPane createGradientButton(String text, Runnable action) {
        double width = 180;
        double height = 44;
        double cornerRadius = 14;

        StackPane buttonContainer = new StackPane();
        buttonContainer.setPrefSize(width, height);
        buttonContainer.setMaxSize(width, height);

        double gradientW = width * 12;
        double gradientH = height * 12;
        javafx.scene.shape.Rectangle gradientRect = new javafx.scene.shape.Rectangle(gradientW, gradientH);
        gradientRect.setFill(javafx.scene.paint.LinearGradient.valueOf(
                "from 0% 0% to 100% 100%, #7CE8FF 0%, #4CA4FF 20%, #7A6BFF 40%, #FF7EDB 60%, #FFD95E 80%, #7CE8FF 100%"));
        gradientRect.setMouseTransparent(true);
        gradientRect.setManaged(false);

        gradientRect.setTranslateX(-(gradientW - width) / 2.0);
        gradientRect.setTranslateY(-(gradientH - height) / 2.0);

        javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(
                javafx.util.Duration.seconds(5), gradientRect);
        rotate.setByAngle(360);
        rotate.setCycleCount(javafx.animation.RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();

        javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle(width, height);
        clipRect.setArcWidth(cornerRadius * 2);
        clipRect.setArcHeight(cornerRadius * 2);
        buttonContainer.setClip(clipRect);

        javafx.scene.shape.Rectangle innerBackground = new javafx.scene.shape.Rectangle(width - 4, height - 4);
        innerBackground.setArcWidth((cornerRadius - 2) * 2);
        innerBackground.setArcHeight((cornerRadius - 2) * 2);
        innerBackground.setId("innerBackground");
        innerBackground.setFill(javafx.scene.paint.Color.rgb(32, 33, 35, 1.0));

        javafx.scene.control.Label label = new javafx.scene.control.Label(text);
        label.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        label.setId("btnLabel");
        label.setTextFill(javafx.scene.paint.Color.WHITE);

        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.getChildren().addAll(gradientRect, innerBackground, label);
        buttonContainer.setOnMouseClicked(e -> action.run());

        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), buttonContainer);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), buttonContainer);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        buttonContainer.setOnMouseEntered(e -> {
            scaleUp.play();
            buttonContainer.setCursor(javafx.scene.Cursor.HAND);
        });
        buttonContainer.setOnMouseExited(e -> scaleDown.play());

        return buttonContainer;
    }

    private void addHoverScaleEffect(Button button) {
        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(200), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        button.setOnMouseEntered(e -> scaleUp.play());
        button.setOnMouseExited(e -> scaleDown.play());
    }

    private javafx.animation.Timeline loaderTimeline;

    private void startSendButtonLoader() {
        if (loaderTimeline != null) {
            loaderTimeline.stop();
        }

        String[] loaderStates = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
        final int[] index = { 0 };

        loaderTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(80),
                        e -> {
                            if (sendBtn != null) {
                                sendBtn.setText(loaderStates[index[0]]);
                                index[0] = (index[0] + 1) % loaderStates.length;
                            }
                        }));
        loaderTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        loaderTimeline.play();
    }

    private void stopSendButtonLoader() {
        if (loaderTimeline != null) {
            loaderTimeline.stop();
            loaderTimeline = null;
        }
        if (sendBtn != null) {
            sendBtn.setText("➤");
        }
    }

    private void applyInputAreaBackground(VBox target) {
        if (target == null)
            return;
        String bg = isDarkMode ? "#343541" : "#FFFFFF";
        target.setStyle("-fx-background-color: " + bg + ";");
    }

    private void updateInputFieldStyle() {
        if (inputField == null) {
            return;
        }
        String bg = isDarkMode ? "#40414F" : "#ECECF1";
        String text = isDarkMode ? "#111827" : "#111827";
        String prompt = isDarkMode ? "#8E8EA0" : "#6B7280";
        String border = isDarkMode ? "transparent" : "#6B7280";

        inputField.setStyle(
                "-fx-background-color: " + bg + "; " +
                        "-fx-text-fill: " + text + "; " +
                        "-fx-highlight-fill: " + (isDarkMode ? "#2563EB" : "#BFDBFE") + "; " +
                        "-fx-highlight-text-fill: " + text + "; " +
                        "-fx-prompt-text-fill: " + prompt + "; " +
                        "-fx-border-color: " + border + "; " +
                        "-fx-border-width: 0.3; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 12; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-family: 'Segoe UI', 'Arial';");
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();

        updateThemeButtonLabel();
        updateThemeButtonStyle();
    }

    private void updateThemeButtonLabel() {

        themeToggleBtn.setText(isDarkMode ? "🌙" : "☀");
    }

    private void applyTheme() {

        String darkBg = "#343541";
        String darkSidebar = "#202123";
        String darkMessageBg = "#444654";
        String darkText = "#ECECF1";
        String darkSecondary = "#8E8EA0";

        String lightBg = "#FFFFFF";
        String lightSidebar = "#F5F5F5";
        String lightMessageBg = "#E8F4F8";
        String lightText = "#1F2937";
        String lightSecondary = "#6B7280";

        String bgColor = isDarkMode ? darkBg : lightBg;
        String sidebarColor = isDarkMode ? darkSidebar : lightSidebar;
        String messageBg = isDarkMode ? darkMessageBg : lightMessageBg;
        String textColor = isDarkMode ? darkText : lightText;
        String secondaryColor = isDarkMode ? darkSecondary : lightSecondary;

        chatBox.setStyle("-fx-background-color: " + bgColor + ";");
        centerPane.setStyle("-fx-background-color: " + bgColor + ";");
        root.setStyle("-fx-background-color: " + bgColor + ";");

        applyInputAreaBackground(inputArea);
        updateInputFieldStyle();

        scrollPane.setStyle("-fx-background: " + bgColor + "; -fx-background-color: " + bgColor + ";");

        VBox sidebar = (VBox) root.getLeft();
        sidebar.setStyle("-fx-background-color: " + sidebarColor + ";");

        HBox header = (HBox) centerPane.getTop();
        header.setStyle("-fx-background-color: " + bgColor + ";");

        dateTimeLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 12px; -fx-padding: 10;");
        statsLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 12px; -fx-padding: 10;");

        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof HBox) {
                HBox bar = (HBox) node;
                bar.setStyle("-fx-background-color: " + sidebarColor + ";");
                for (javafx.scene.Node child : bar.getChildren()) {
                    if (child instanceof Button) {
                        Button btn = (Button) child;
                        if (btn == newChatBtn) {
                            updateNewChatButtonStyle();
                        } else {
                            btn.setStyle(
                                    "-fx-background-color: " + (isDarkMode ? "#2A2B32" : "#E5E7EB") + "; " +
                                            "-fx-text-fill: " + (isDarkMode ? "white" : "black") + "; " +
                                            "-fx-font-size: 13px; " +
                                            "-fx-padding: 10; " +
                                            "-fx-cursor: hand; " +
                                            "-fx-border-color: " + (isDarkMode ? "#565869" : "#D1D5DB") + "; " +
                                            "-fx-border-width: 1; " +
                                            "-fx-border-radius: 5; " +
                                            "-fx-background-radius: 5;");
                        }
                    } else if (child instanceof Label) {
                        ((Label) child).setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px;");
                    }
                }
            } else if (node instanceof Label) {
                ((Label) node)
                        .setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px; -fx-padding: 10 5 5 5;");
            } else if (node instanceof ListView) {
                @SuppressWarnings("unchecked")
                ListView<Object> list = (ListView<Object>) node;
                list.setStyle(
                        "-fx-background-color: " + sidebarColor + "; " +
                                "-fx-control-inner-background: " + sidebarColor + "; " +
                                "-fx-text-fill: " + textColor + ";");
            }
        }

        updateThemeButtonStyle();
        updateNewChatButtonStyle();
        updateHomeButtonStyle();
        updateExportButtonStyle();
        historyList.refresh();

        if (welcomeBox != null && scrollPane.getContent() == welcomeBox) {
            welcomeBox.setStyle("-fx-background-color: " + bgColor + ";");
            updateWelcomeBoxColors(welcomeBox, textColor, secondaryColor, isDarkMode);
        } else if (chatBox != null) {

            updateAllMessagesTheme(messageBg, textColor, secondaryColor);
        }

        if (summarizeBtn != null) {
            javafx.scene.shape.Rectangle innerBg = (javafx.scene.shape.Rectangle) summarizeBtn
                    .lookup("#innerBackground");
            javafx.scene.control.Label btnLabel = (javafx.scene.control.Label) summarizeBtn.lookup("#btnLabel");
            if (innerBg != null && btnLabel != null) {

                String btnBg = isDarkMode ? "#2A2B32" : "#E5E7EB";
                String btnText = isDarkMode ? "white" : "#111827";
                innerBg.setFill(javafx.scene.paint.Color.web(btnBg));
                btnLabel.setTextFill(javafx.scene.paint.Color.web(btnText));
            }
        }


    }

    private void updateAllMessagesTheme(String messageBg, String textColor, String secondaryColor) {
        for (javafx.scene.Node messageNode : chatBox.getChildren()) {
            if (messageNode instanceof HBox) {
                HBox messageRow = (HBox) messageNode;
                for (javafx.scene.Node child : messageRow.getChildren()) {
                    if (child instanceof VBox) {
                        VBox messageBox = (VBox) child;
                        String msgId = messageBox.getId();

                        if ("user-message-box".equals(msgId) || "ai-message-box".equals(msgId)) {
                            updateMessageBoxTheme(messageBox, msgId, messageBg, textColor, secondaryColor);
                        }
                    }
                }
            } else if (messageNode instanceof VBox) {
                updateWelcomeBoxColors((VBox) messageNode, textColor, secondaryColor, isDarkMode);
            }
        }
    }

    private void updateMessageBoxTheme(VBox messageBox, String msgId, String messageBg, String textColor,
            String secondaryColor) {

        messageBox.setStyle(
                "-fx-background-color: " + messageBg + "; " +
                        "-fx-padding: 15; " +
                        "-fx-background-radius: 10;");

        Object userData = messageBox.getUserData();
        if (userData instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Label> labelMap = (java.util.Map<String, Label>) userData;

            Label authorLabel = labelMap.get("author");
            Label textLabel = labelMap.get("text");
            Label timeLabel = labelMap.get("time");

            if (timeLabel != null) {
                timeLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px;");
            }
            if (textLabel != null) {
                textLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");
            }
            if (authorLabel != null) {
                if ("user-message-box".equals(msgId)) {
                    authorLabel.setStyle("-fx-text-fill: #19C37D; -fx-font-weight: bold; -fx-font-size: 13px;");
                } else if ("ai-message-box".equals(msgId)) {
                    authorLabel.setStyle("-fx-text-fill: #10A37F; -fx-font-weight: bold; -fx-font-size: 13px;");
                }
            }
        }

        Object textFlowObj = messageBox.getProperties().get("textFlow");
        if (textFlowObj instanceof javafx.scene.text.TextFlow) {
            javafx.scene.text.TextFlow flow = (javafx.scene.text.TextFlow) textFlowObj;
            for (javafx.scene.Node flowChild : flow.getChildren()) {
                if (flowChild instanceof javafx.scene.text.Text) {
                    ((javafx.scene.text.Text) flowChild).setStyle("-fx-fill: " + textColor + "; -fx-font-size: 14px;");
                }
            }
        }
    }

    private void updateWelcomeBoxColors(VBox box, String textColor, String secondaryColor, boolean darkMode) {
        int index = 0;
        for (javafx.scene.Node child : box.getChildren()) {
            if (child instanceof Label) {
                Label label = (Label) child;
                boolean isLast = index == box.getChildren().size() - 1;
                String color = isLast ? secondaryColor : textColor;
                String currentStyle = label.getStyle();

                if (currentStyle.contains("-fx-font-size")) {
                    label.setStyle("-fx-text-fill: " + color + ";");
                } else {
                    label.setStyle("-fx-text-fill: " + color + ";");
                }
            } else if (child instanceof javafx.scene.text.Text) {
                javafx.scene.text.Text text = (javafx.scene.text.Text) child;
                text.setFill(darkMode ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.web("#111827"));
            }
            index++;
        }
    }

    private void hideTypingIndicator() {

        if (sendBtn != null) {
            Object animationObj = sendBtn.getUserData();
            if (animationObj instanceof javafx.animation.ParallelTransition) {
                ((javafx.animation.ParallelTransition) animationObj).stop();
                sendBtn.setUserData(null);
            } else if (animationObj instanceof javafx.animation.RotateTransition) {

                ((javafx.animation.RotateTransition) animationObj).stop();
                sendBtn.setUserData(null);
            }
            Object circleObj = sendBtn.getProperties().get("borderCircle");
            if (circleObj instanceof javafx.scene.shape.Circle) {
                javafx.scene.shape.Circle borderCircle = (javafx.scene.shape.Circle) circleObj;
                if (sendBtn.getParent() instanceof HBox) {
                    HBox parentBox = (HBox) sendBtn.getParent();
                    parentBox.getChildren().remove(borderCircle);
                }
                sendBtn.getProperties().remove("borderCircle");
            }
            sendBtn.setStyle(
                    "-fx-background-color: #000000ff; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 12 12; " +
                            "-fx-min-width: 46; " +
                            "-fx-min-height: 46; " +
                            "-fx-pref-width: 46; " +
                            "-fx-pref-height: 46; " +
                            "-fx-background-radius: 23; " +
                            "-fx-cursor: hand;");

            Runnable sendMessage = () -> {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    addUserMessage(text);
                    inputField.clear();
                    showTypingIndicator();

                    new Thread(() -> {
                        try {
                            String history = conversationHistory.getOrDefault(currentConversation, "");
                            APIResponse apiResponse = controller.sendMessageWithPing(text, history);
                            String botResponse = apiResponse.content;
                            long pingMs = apiResponse.pingMs;

                            javafx.application.Platform.runLater(() -> {
                                hideTypingIndicator();

                                VBox sidebar = (VBox) root.getLeft();
                                HBox bottomBar = (HBox) sidebar.getChildren().get(sidebar.getChildren().size() - 1);
                                if (bottomBar.getChildren().size() >= 2) {
                                    Label pingLabel = (Label) bottomBar.getChildren().get(1);
                                    lastPingValue = "Ping: " + pingMs + "ms";
                                    pingLabel.setText(lastPingValue);
                                }

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {
                                }
                                addAIMessage(botResponse, text, history);
                                conversationHistory.put(currentConversation,
                                        controller.buildTurn(history, text, botResponse));
                                saveCurrentConversation();
                            });
                        } catch (Exception e) {
                            javafx.application.Platform.runLater(() -> {
                                hideTypingIndicator();
                                addAIMessage("Erreur: " + e.getMessage());
                            });
                        }
                    }).start();
                }
            };
            sendBtn.setOnAction(e -> sendMessage.run());
        }

        if (typingTimeline != null) {
            typingTimeline.stop();
            typingTimeline = null;
        }
        if (typingMessageRow != null) {
            chatBox.getChildren().remove(typingMessageRow);
            typingMessageRow = null;
        }

        stopMessageDisplay = true;
        if (currentMessageThread != null) {
            currentMessageThread.interrupt();
            currentMessageThread = null;
        }
    }

    private void updateThemeButtonStyle() {
        if (themeToggleBtn == null) {
            return;
        }
        String bg = isDarkMode ? "#2A2B32" : "#E5E7EB";
        String border = isDarkMode ? "#565869" : "#D1D5DB";
        String text = isDarkMode ? "white" : "#111827";

        themeToggleBtn.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 10 12; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 14; " +
                "-fx-background-radius: 14;");
        themeToggleBtn.setPrefSize(180, 44);
    }

    private void updateNewChatButtonStyle() {
        if (newChatBtn == null) {
            return;
        }
        String bg = "#0EA5E9";
        String border = "#0EA5E9";

        newChatBtn.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 10; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;");
    }

    private void updateHomeButtonStyle() {
        if (homeBtn == null) {

            return;
        }
        String bg = isDarkMode ? "#2A2B32" : "#E5E7EB";
        String border = isDarkMode ? "#565869" : "#D1D5DB";
        String text = isDarkMode ? "white" : "#111827";

        homeBtn.setStyle("-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + text + "; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 10; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: " + border + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;");
    }

    private void setConnectionStatus(boolean connected) {
        if (connectionIndicator == null) {
            return;
        }
        if (connected) {
            connectionIndicator.setText("Connecté");
            connectionIndicator.setStyle("-fx-text-fill: #10A37F; -fx-font-size: 11px;");
        } else {
            connectionIndicator.setText("Déconnecté");
            connectionIndicator.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px;");
        }
    }

    private void updateExportButtonStyle() {
        if (exportBtn == null)
            return;
        String baseBg = isDarkMode ? "#2A2B32" : "#E5F0F7";
        String borderColor = isDarkMode ? "#565869" : "#C5D8E8";
        String textColor = isDarkMode ? "white" : "#111827";

        exportBtn.setStyle(
                "-fx-background-color: " + baseBg + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 14; " +
                        "-fx-background-radius: 14;");
    }

    private void exportConversation() {
        if (currentConversation == null || currentConversation.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune conversation sélectionnée");
            alert.setHeaderText("Sélectionnez une conversation à exporter");
            alert.showAndWait();
            return;
        }

        try {
            String fileName = currentConversation.replaceAll("[^a-zA-Z0-9]", "_") + ".txt";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la conversation");
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

            File selectedFile = fileChooser.showSaveDialog(root.getScene().getWindow());
            if (selectedFile == null) {
                return;
            }

            controller.exportConversation(currentConversation, selectedFile);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export réussi");
            alert.setHeaderText("Conversation exportée");
            alert.setContentText("Fichier sauvegardé dans:\n" + selectedFile.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'export");
            alert.setHeaderText("Impossible d'exporter la conversation");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadSavedConversations() {
        List<ConversationManager.ConversationData> savedConversations = controller.loadConversations();

        for (ConversationManager.ConversationData data : savedConversations) {

            VBox convBox = rebuildConversationUI(data.history);

            conversations.put(data.name, convBox);
            conversationDates.put(data.name, data.date != null ? data.date : LocalDateTime.now());
            conversationHistory.put(data.name, data.history != null ? data.history : "");
            conversationMessageCounts.put(data.name, data.messageCount);

            long startTime = data.date != null
                    ? data.date.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis();
            conversationStartTimes.put(data.name, startTime);

            historyList.getItems().add(data.name);
        }

    }

    private VBox rebuildConversationUI(String history) {
        VBox convBox = new VBox(20);
        convBox.setPadding(new Insets(20));
        convBox.setStyle("-fx-background-color: #343541;");

        

        if (history == null || history.trim().isEmpty()) {
            return convBox;
        }

        List<String[]> messages = controller.parseHistory(history);
        for (String[] msg : messages) {
            if ("user".equals(msg[0])) {
                addStaticUserMessage(convBox, msg[1]);
            } else if ("assistant".equals(msg[0])) {
                addStaticAIMessage(convBox, msg[1]);
            }
        }

        return convBox;
    }

    private void addStaticUserMessage(VBox targetBox, String text) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER_RIGHT);
        messageRow.setPadding(new Insets(10, 0, 10, 0));
        messageRow.setId("user-message-row");

        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(700);
        messageBox.setId("user-message-box");

        String messageBg = isDarkMode ? "#444654" : "#E8F4F8";
        String textColor = isDarkMode ? "#ECECF1" : "#1F2937";
        String secondaryColor = isDarkMode ? "#8E8EA0" : "#6B7280";

        messageBox.setStyle(
                "-fx-background-color: " + messageBg + "; " +
                        "-fx-padding: 15; " +
                        "-fx-background-radius: 10;");

        Label authorLabel = new Label("Vous");
        authorLabel.setId("user-author");
        authorLabel.setStyle("-fx-text-fill: #19C37D; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setId("user-message-text");
        messageLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");

        Label timeLabel = new Label("");
        timeLabel.setId("user-time");
        timeLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px;");

        messageBox.getChildren().addAll(authorLabel, messageLabel, timeLabel);

        Map<String, Label> labelMap = new HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        messageBox.setUserData(labelMap);

        messageRow.getChildren().add(messageBox);
        targetBox.getChildren().add(messageRow);
    }

    private void addStaticAIMessage(VBox targetBox, String text) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER_LEFT);
        messageRow.setPadding(new Insets(10, 0, 10, 0));
        messageRow.setId("ai-message-row");

        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(700);
        messageBox.setId("ai-message-box");

        String messageBg = isDarkMode ? "#444654" : "#E8F4F8";
        String textColor = isDarkMode ? "#ECECF1" : "#1F2937";
        String secondaryColor = isDarkMode ? "#8E8EA0" : "#6B7280";

        messageBox.setStyle(
                "-fx-background-color: " + messageBg + "; " +
                        "-fx-padding: 15; " +
                        "-fx-background-radius: 10;");

        Label authorLabel = new Label("MiageGPT");
        authorLabel.setId("ai-author");
        authorLabel.setStyle("-fx-text-fill: #10A37F; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setId("ai-message-text");
        messageLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px;");

        Label timeLabel = new Label("");
        timeLabel.setId("ai-time");
        timeLabel.setStyle("-fx-text-fill: " + secondaryColor + "; -fx-font-size: 11px;");

        Button copyBtn = new Button("\uD83D\uDCCB");
        copyBtn.setStyle(
                "-fx-font-size: 11px; -fx-padding: 4 6; -fx-background-radius: 4; -fx-background-color: #6B7280; -fx-text-fill: white;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
            copyBtn.setText("✓");
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(1500),
                            ev -> copyBtn.setText("\uD83D\uDCCB")));
            timeline.play();
        });

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_RIGHT);
        Region spacerFooter = new Region();
        HBox.setHgrow(spacerFooter, Priority.ALWAYS);
        footerBox.getChildren().addAll(timeLabel, spacerFooter, copyBtn);

        messageBox.getChildren().addAll(authorLabel, messageLabel, footerBox);

        Map<String, Label> labelMap = new HashMap<>();
        labelMap.put("author", authorLabel);
        labelMap.put("text", messageLabel);
        labelMap.put("time", timeLabel);
        messageBox.setUserData(labelMap);

        messageRow.getChildren().add(messageBox);
        targetBox.getChildren().add(messageRow);
    }

    private void saveCurrentConversation() {
        if (currentConversation == null)
            return;

        controller.saveConversation(
                currentConversation,
                conversationDates.get(currentConversation),
                conversationHistory.getOrDefault(currentConversation, ""),
                null,
                conversationMessageCounts.getOrDefault(currentConversation, 0));
    }

    private void saveAllConversations() {
        for (String name : conversations.keySet()) {
            controller.saveConversation(
                    name,
                    conversationDates.get(name),
                    conversationHistory.getOrDefault(name, ""),
                    null,
                    conversationMessageCounts.getOrDefault(name, 0));
        }
    }
}
