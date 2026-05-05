package com.miage.miagegpt.view;

import com.miage.miagegpt.controller.AdminController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class AdminView {

    private final AdminController controller;
    private final Stage parentStage;
    private Stage adminStage;

    public AdminView(Stage parentStage, AdminController controller) {
        this.controller = controller;
        this.parentStage = parentStage;
    }

    public Stage getStage() {
        return adminStage;
    }

    public void show() {
        adminStage = new Stage();
        adminStage.initModality(Modality.WINDOW_MODAL);
        adminStage.initOwner(parentStage);
        adminStage.setTitle("Administration - Base de données association");
        try {
            adminStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception ignored) {}

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #1E1E2E;");

        Tab assoTab = new Tab("Association");
        assoTab.setClosable(false);
        assoTab.setContent(createAssociationTab());

        Tab membresTab = new Tab("Membres");
        membresTab.setClosable(false);
        membresTab.setContent(createMembresTab());

        Tab faqTab = new Tab("FAQ");
        faqTab.setClosable(false);
        faqTab.setContent(createFAQTab());

        Tab reseauxTab = new Tab("Réseaux");
        reseauxTab.setClosable(false);
        reseauxTab.setContent(createReseauxTab());

        tabPane.getTabs().addAll(assoTab, membresTab, faqTab, reseauxTab);

        Scene scene = new Scene(tabPane, 800, 600);
        adminStage.setScene(scene);
        adminStage.show();
    }

    private VBox createAssociationTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("Informations de l'association");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #ECECF1;");

        TextField nomField = createStyledField("Nom de l'association");
        TextArea descField = createStyledTextArea("Description de l'association");
        TextField adresseField = createStyledField("Adresse (ex: B\u00e2timent C, Salle 42)");
        TextField universiteField = createStyledField("Universit\u00e9 / Campus");
        TextField typeAssoField = createStyledField("Type (ex: BDE, BDS, Club, Association loi 1901)");

        String[] info = controller.getAssociationInfo();
        if (info != null) {
            nomField.setText(info[0] != null ? info[0] : "");
            descField.setText(info[1] != null ? info[1] : "");
            adresseField.setText(info[2] != null ? info[2] : "");
            universiteField.setText(info[3] != null ? info[3] : "");
            typeAssoField.setText(info[4] != null ? info[4] : "");
        }

        Button saveBtn = createStyledButton("Sauvegarder", "#10A37F");
        saveBtn.setOnAction(e -> {
            controller.updateAssociationInfo(
                nomField.getText(),
                descField.getText(),
                adresseField.getText(),
                universiteField.getText(),
                typeAssoField.getText()
            );
            showAlert("Succ\u00e8s", "Les informations ont \u00e9t\u00e9 mises \u00e0 jour !");
        });

        container.getChildren().addAll(
            title,
            createFieldRow("Nom :", nomField),
            createFieldRow("Description :", descField),
            createFieldRow("Adresse / Local :", adresseField),
            createFieldRow("Universit\u00e9 :", universiteField),
            createFieldRow("Type d'asso :", typeAssoField),
            saveBtn
        );

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    private VBox createMembresTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("Gestion des membres");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #ECECF1;");

        VBox membersList = new VBox(5);
        membersList.setStyle("-fx-background-color: #1E1E2E; -fx-padding: 10;");

        Label addTitle = new Label("Ajouter un membre");
        addTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        addTitle.setStyle("-fx-text-fill: #ECECF1;");

        TextField nomField = createStyledField("Nom");
        TextField prenomField = createStyledField("Prénom");
        TextField roleField = createStyledField("Rôle (ex: Président)");
        TextField emailField = createStyledField("Email");
        TextArea descField = createStyledTextArea("Infos supplémentaires");

        Button addBtn = createStyledButton("+ Ajouter le membre", "#10A37F");

        Runnable refreshList = () -> {
            refreshMembersList(membersList);
        };

        addBtn.setOnAction(e -> {
            controller.addMember(
                nomField.getText(), prenomField.getText(), roleField.getText(),
                emailField.getText(),
                descField.getText()
            );
            nomField.clear(); prenomField.clear(); roleField.clear();
            emailField.clear(); descField.clear();
            refreshList.run();
            showAlert("Succès", "Membre ajouté !");
        });

        refreshList.run();

        container.getChildren().addAll(
            title, membersList,
            new Separator(),
            addTitle,
            createFieldRow("Nom :", nomField),
            createFieldRow("Prénom :", prenomField),
            createFieldRow("Rôle :", roleField),
            createFieldRow("Email :", emailField),
            createFieldRow("Infos supp. :", descField),
            addBtn
        );

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    private HBox createMemberRow(String[] m, VBox membersList) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #343541; -fx-background-radius: 5;");

        Label info = new Label(m[2] + " " + m[1] + " - " + m[3] + " (" + m[4] + ")");
        info.setStyle("-fx-text-fill: #ECECF1; -fx-font-size: 13px;");
        HBox.setHgrow(info, Priority.ALWAYS);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        editBtn.setOnAction(ev -> showEditMemberDialog(m, membersList));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        deleteBtn.setOnAction(ev -> {
            if (showConfirmation("Supprimer le membre", "Voulez-vous vraiment supprimer " + m[2] + " " + m[1] + " ?")) {
                controller.deleteMember(Integer.parseInt(m[0]));
                refreshMembersList(membersList);
            }
        });

        row.getChildren().addAll(info, editBtn, deleteBtn);
        return row;
    }

    private void refreshMembersList(VBox membersList) {
        membersList.getChildren().clear();
        List<String[]> updatedMembers = controller.getAllMembers();
        for (String[] um : updatedMembers) {
            membersList.getChildren().add(createMemberRow(um, membersList));
        }
    }

    private void showEditMemberDialog(String[] m, VBox membersList) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(parentStage);
        editStage.setTitle("Modifier le membre : " + m[2] + " " + m[1]);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("Modifier le membre");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #ECECF1;");

        TextField nomField = createStyledField("Nom");
        nomField.setText(m[1] != null ? m[1] : "");
        TextField prenomField = createStyledField("Prénom");
        prenomField.setText(m[2] != null ? m[2] : "");
        TextField roleField = createStyledField("Rôle");
        roleField.setText(m[3] != null ? m[3] : "");
        TextField emailField = createStyledField("Email");
        emailField.setText(m[4] != null ? m[4] : "");
        TextArea descField = createStyledTextArea("Infos supplémentaires");
        descField.setText(m[5] != null ? m[5] : "");

        Button saveBtn = createStyledButton("Sauvegarder", "#10A37F");
        Button cancelBtn = createStyledButton("Annuler", "#6B7280");

        saveBtn.setOnAction(ev -> {
            controller.updateMember(
                Integer.parseInt(m[0]),
                nomField.getText(), prenomField.getText(), roleField.getText(),
                emailField.getText(),
                descField.getText()
            );
            refreshMembersList(membersList);
            editStage.close();
            showAlert("Succès", "Membre modifié !");
        });

        cancelBtn.setOnAction(ev -> editStage.close());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(cancelBtn, saveBtn);

        form.getChildren().addAll(
            title,
            createFieldRow("Nom :", nomField),
            createFieldRow("Prénom :", prenomField),
            createFieldRow("Rôle :", roleField),
            createFieldRow("Email :", emailField),
            createFieldRow("Infos supp. :", descField),
            buttons
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        Scene scene = new Scene(scroll, 550, 500);
        editStage.setScene(scene);
        editStage.show();
    }

    private VBox createFAQTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("Gestion de la FAQ");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #ECECF1;");

        VBox faqList = new VBox(5);
        faqList.setStyle("-fx-background-color: #1E1E2E; -fx-padding: 10;");

        Label addTitle = new Label("Ajouter une question/réponse");
        addTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        addTitle.setStyle("-fx-text-fill: #ECECF1;");

        TextField questionField = createStyledField("Question");
        TextArea reponseField = createStyledTextArea("Réponse");
        TextField categorieField = createStyledField("Catégorie (ex: Adhésion, Général)");

        Button addBtn = createStyledButton("+ Ajouter la FAQ", "#10A37F");

        Runnable refreshList = () -> {
            refreshFaqList(faqList);
        };

        addBtn.setOnAction(e -> {
            controller.addFAQ(questionField.getText(), reponseField.getText(), categorieField.getText());
            questionField.clear(); reponseField.clear(); categorieField.clear();
            refreshList.run();
            showAlert("Succès", "FAQ ajoutée !");
        });

        refreshList.run();

        container.getChildren().addAll(
            title, faqList,
            new Separator(),
            addTitle,
            createFieldRow("Question :", questionField),
            createFieldRow("Réponse :", reponseField),
            createFieldRow("Catégorie :", categorieField),
            addBtn
        );

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    private VBox createFaqCard(String[] f, VBox faqList) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #343541; -fx-background-radius: 5;");

        Label qLabel = new Label("Q: " + f[1]);
        qLabel.setStyle("-fx-text-fill: #10A37F; -fx-font-size: 13px; -fx-font-weight: bold;");
        qLabel.setWrapText(true);

        Label aLabel = new Label("R: " + f[2]);
        aLabel.setStyle("-fx-text-fill: #ECECF1; -fx-font-size: 12px;");
        aLabel.setWrapText(true);

        Label catLabel = new Label("(" + f[3] + ")");
        catLabel.setStyle("-fx-text-fill: #8E8EA0; -fx-font-size: 11px;");

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        editBtn.setOnAction(ev -> showEditFaqDialog(f, faqList));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        deleteBtn.setOnAction(ev -> {
            if (showConfirmation("Supprimer la FAQ", "Voulez-vous vraiment supprimer cette question ?")) {
                controller.deleteFAQ(Integer.parseInt(f[0]));
                refreshFaqList(faqList);
            }
        });

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bottomRow.getChildren().addAll(catLabel, spacer, editBtn, deleteBtn);

        card.getChildren().addAll(qLabel, aLabel, bottomRow);
        return card;
    }

    private void refreshFaqList(VBox faqList) {
        faqList.getChildren().clear();
        List<String[]> updatedFaqs = controller.getAllFAQ();
        for (String[] uf : updatedFaqs) {
            faqList.getChildren().add(createFaqCard(uf, faqList));
        }
    }

    private void showEditFaqDialog(String[] f, VBox faqList) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(parentStage);
        editStage.setTitle("Modifier la FAQ");

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("Modifier la question/r\u00e9ponse");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #ECECF1;");

        TextField questionField = createStyledField("Question");
        questionField.setText(f[1] != null ? f[1] : "");
        TextArea reponseField = createStyledTextArea("R\u00e9ponse");
        reponseField.setText(f[2] != null ? f[2] : "");
        TextField categorieField = createStyledField("Cat\u00e9gorie");
        categorieField.setText(f[3] != null ? f[3] : "");

        Button saveBtn = createStyledButton("Sauvegarder", "#10A37F");
        Button cancelBtn = createStyledButton("Annuler", "#6B7280");

        saveBtn.setOnAction(ev -> {
            controller.updateFAQ(
                Integer.parseInt(f[0]),
                questionField.getText(),
                reponseField.getText(),
                categorieField.getText()
            );
            refreshFaqList(faqList);
            editStage.close();
            showAlert("Succ\u00e8s", "FAQ modifi\u00e9e !");
        });

        cancelBtn.setOnAction(ev -> editStage.close());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(cancelBtn, saveBtn);

        form.getChildren().addAll(
            title,
            createFieldRow("Question :", questionField),
            createFieldRow("R\u00e9ponse :", reponseField),
            createFieldRow("Cat\u00e9gorie :", categorieField),
            buttons
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        Scene scene = new Scene(scroll, 550, 400);
        editStage.setScene(scene);
        editStage.show();
    }

    private VBox createReseauxTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("R\u00e9seaux & liens de contact");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #ECECF1;");

        Label desc = new Label("Ajoutez ici les liens vers vos r\u00e9seaux sociaux, email de contact, etc.\n"
            + "Le chatbot pourra les communiquer aux utilisateurs.");
        desc.setStyle("-fx-text-fill: #8E8EA0; -fx-font-size: 12px;");
        desc.setWrapText(true);

        VBox reseauxList = new VBox(5);
        reseauxList.setStyle("-fx-background-color: #1E1E2E; -fx-padding: 10;");

        Label addTitle = new Label("Ajouter un lien");
        addTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        addTitle.setStyle("-fx-text-fill: #ECECF1;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "Email", "Instagram", "LinkedIn", "X (Twitter)", "Facebook",
            "Discord", "TikTok", "YouTube", "Site web", "Autre"
        );
        typeCombo.setPromptText("Type de r\u00e9seau");
        typeCombo.setEditable(true);
        typeCombo.setStyle(
            "-fx-background-color: #343541; " +
            "-fx-border-color: #565869; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        TextField valeurField = createStyledField("Lien ou adresse (ex: @asso_miage, https://...)");
        TextField libelleField = createStyledField("Libell\u00e9 (optionnel, ex: Page officielle)");

        Button addBtn = createStyledButton("+ Ajouter le lien", "#10A37F");

        Runnable refreshList = () -> {
            refreshReseauxList(reseauxList);
        };

        addBtn.setOnAction(e -> {
            String type = typeCombo.getValue();
            if (type == null || type.trim().isEmpty() || valeurField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le type et la valeur sont obligatoires.");
                return;
            }
            controller.addReseau(type.trim(), valeurField.getText().trim(), libelleField.getText().trim());
            typeCombo.setValue(null);
            valeurField.clear();
            libelleField.clear();
            refreshList.run();
            showAlert("Succ\u00e8s", "Lien ajout\u00e9 !");
        });

        refreshList.run();

        container.getChildren().addAll(
            title, desc, reseauxList,
            new Separator(),
            addTitle,
            createFieldRow("Type :", typeCombo),
            createFieldRow("Valeur :", valeurField),
            createFieldRow("Libell\u00e9 :", libelleField),
            addBtn
        );

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    private HBox createReseauRow(String[] r, VBox reseauxList) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #343541; -fx-background-radius: 5;");

        String displayText = r[1] + " : " + r[2];
        if (r[3] != null && !r[3].isEmpty()) {
            displayText += " (" + r[3] + ")";
        }
        Label info = new Label(displayText);
        info.setStyle("-fx-text-fill: #ECECF1; -fx-font-size: 13px;");
        HBox.setHgrow(info, Priority.ALWAYS);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        editBtn.setOnAction(ev -> showEditReseauDialog(r, reseauxList));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-background-radius: 4;");
        deleteBtn.setOnAction(ev -> {
            if (showConfirmation("Supprimer le lien", "Voulez-vous vraiment supprimer \"" + r[1] + " : " + r[2] + "\" ?")) {
                controller.deleteReseau(Integer.parseInt(r[0]));
                refreshReseauxList(reseauxList);
            }
        });

        row.getChildren().addAll(info, editBtn, deleteBtn);
        return row;
    }

    private void refreshReseauxList(VBox reseauxList) {
        reseauxList.getChildren().clear();
        List<String[]> reseaux = controller.getAllReseaux();
        if (reseaux.isEmpty()) {
            Label empty = new Label("Aucun r\u00e9seau ajout\u00e9 pour le moment.");
            empty.setStyle("-fx-text-fill: #8E8EA0; -fx-font-style: italic;");
            reseauxList.getChildren().add(empty);
        } else {
            for (String[] r : reseaux) {
                reseauxList.getChildren().add(createReseauRow(r, reseauxList));
            }
        }
    }

    private void showEditReseauDialog(String[] r, VBox reseauxList) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(parentStage);
        editStage.setTitle("Modifier le lien : " + r[1]);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #2B2D42;");

        Label title = new Label("Modifier le lien");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #ECECF1;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "Email", "Instagram", "LinkedIn", "X (Twitter)", "Facebook",
            "Discord", "TikTok", "YouTube", "Site web", "Autre"
        );
        typeCombo.setEditable(true);
        typeCombo.setValue(r[1] != null ? r[1] : "");
        typeCombo.setStyle(
            "-fx-background-color: #343541; " +
            "-fx-border-color: #565869; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        TextField valeurField = createStyledField("Valeur");
        valeurField.setText(r[2] != null ? r[2] : "");
        TextField libelleField = createStyledField("Libell\u00e9");
        libelleField.setText(r[3] != null ? r[3] : "");

        Button saveBtn = createStyledButton("Sauvegarder", "#10A37F");
        Button cancelBtn = createStyledButton("Annuler", "#6B7280");

        saveBtn.setOnAction(ev -> {
            String type = typeCombo.getValue();
            if (type == null || type.trim().isEmpty() || valeurField.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le type et la valeur sont obligatoires.");
                return;
            }
            controller.updateReseau(
                Integer.parseInt(r[0]),
                type.trim(),
                valeurField.getText().trim(),
                libelleField.getText().trim()
            );
            refreshReseauxList(reseauxList);
            editStage.close();
            showAlert("Succ\u00e8s", "Lien modifi\u00e9 !");
        });

        cancelBtn.setOnAction(ev -> editStage.close());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(cancelBtn, saveBtn);

        form.getChildren().addAll(
            title,
            createFieldRow("Type :", typeCombo),
            createFieldRow("Valeur :", valeurField),
            createFieldRow("Libell\u00e9 :", libelleField),
            buttons
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2B2D42; -fx-background-color: #2B2D42;");

        Scene scene = new Scene(scroll, 550, 350);
        editStage.setScene(scene);
        editStage.show();
    }

    private TextField createStyledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-background-color: #343541; " +
            "-fx-text-fill: #ECECF1; " +
            "-fx-prompt-text-fill: #8E8EA0; " +
            "-fx-border-color: #565869; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 8;"
        );
        return field;
    }

    private TextArea createStyledTextArea(String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(3);
        area.setWrapText(true);
        area.setStyle(
            "-fx-control-inner-background: #343541; " +
            "-fx-text-fill: #ECECF1; " +
            "-fx-prompt-text-fill: #8E8EA0; " +
            "-fx-border-color: #565869; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        return area;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-opacity: 0.85;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-opacity: 0.85;", "")));
        return btn;
    }

    private HBox createFieldRow(String labelText, javafx.scene.Node field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setMinWidth(120);
        label.setStyle("-fx-text-fill: #ECECF1; -fx-font-size: 13px;");

        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(label, field);
        return row;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
