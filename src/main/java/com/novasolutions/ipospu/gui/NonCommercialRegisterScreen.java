package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.controller.RegistrationController;
import com.novasolutions.ipospu.service.RegistrationResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NonCommercialRegisterScreen extends BorderPane {

    private final RegistrationController registrationController = new RegistrationController();

    public NonCommercialRegisterScreen(Stage stage) {

        // ── Minimal top bar ───────────────────────────────────────────────
        setTop(buildMinimalTopBar(stage));

        // ── Main content centered ─────────────────────────────────────────
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");
        content.setPadding(new Insets(48));

        // ── Form card ─────────────────────────────────────────────────────
        Label title = new Label("Non-Commercial Registration");
        title.setStyle(AppStyles.headline());

        Label subtitle = new Label("Access the IPOS-PU internal procurement system for individual institutional use.");
        subtitle.setStyle(AppStyles.bodyMuted());
        subtitle.setWrapText(true);

        Label nameLabel = new Label("FULL NAME");
        nameLabel.setStyle(AppStyles.fieldLabel());
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your legal name");
        nameField.setStyle(AppStyles.inputField());
        VBox nameGroup = new VBox(6, nameLabel, nameField);

        Label emailLabel = new Label("INSTITUTIONAL EMAIL");
        emailLabel.setStyle(AppStyles.fieldLabel());
        TextField emailField = new TextField();
        emailField.setPromptText("name@organization.com");
        emailField.setStyle(AppStyles.inputField());
        VBox emailGroup = new VBox(6, emailLabel, emailField);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = new Button("Register →");
        registerBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 12 32;");
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 12 32; -fx-opacity: 0.9;"));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 12 32;"));

        Button backLink = new Button("← Back to login");
        backLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.PRIMARY + ";" +
                          "-fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: transparent;");

        HBox actions = new HBox(16, registerBtn, backLink);
        actions.setAlignment(Pos.CENTER_LEFT);

        // ── Success panel (hidden until success) ──────────────────────────
        Label successTitle = new Label("Registration Successful");
        successTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label successSub = new Label("Your temporary password is shown below. Copy it before continuing.");
        successSub.setStyle(AppStyles.bodyMuted());
        successSub.setWrapText(true);

        Label passwordDisplay = new Label();
        passwordDisplay.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.PRIMARY + ";" +
                                 "-fx-background-color: " + AppStyles.SURFACE_LOW + ";" +
                                 "-fx-background-radius: 6; -fx-padding: 12 20; -fx-font-family: monospace;");
        passwordDisplay.setMaxWidth(Double.MAX_VALUE);

        Button copyBtn = new Button("Copy Password");
        copyBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 10 20;");

        Label securityNote = new Label("You will be prompted to update this password on first login.");
        securityNote.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + "; -fx-font-style: italic;");

        VBox successPanel = new VBox(12, successTitle, successSub, passwordDisplay, copyBtn, securityNote);
        successPanel.setStyle("-fx-background-color: " + AppStyles.SURFACE_HIGH + ";" +
                              "-fx-background-radius: 10; -fx-padding: 24;" +
                              "-fx-border-color: " + AppStyles.PRIMARY + "; -fx-border-width: 0 0 0 4;" +
                              "-fx-border-radius: 0 10 10 0;");
        successPanel.setVisible(false);
        successPanel.setManaged(false);

        VBox card = new VBox(24, title, subtitle, nameGroup, emailGroup, messageLabel, actions, successPanel);
        card.setStyle(AppStyles.card());
        card.setMaxWidth(520);
        card.setEffect(AppStyles.cardShadow());

        content.setAlignment(Pos.CENTER);
        content.getChildren().add(card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        setCenter(scroll);

        // ── Handlers ──────────────────────────────────────────────────────
        registerBtn.setOnAction(e -> {
            RegistrationResult result = registrationController.registerNonCommercial(
                    nameField.getText(), emailField.getText());

            if (result.isSuccess()) {
                passwordDisplay.setText(result.getPassword());
                successPanel.setVisible(true);
                successPanel.setManaged(true);
                messageLabel.setText("");
                registerBtn.setDisable(true);
                nameField.clear();
                emailField.clear();
            } else {
                messageLabel.setStyle(AppStyles.errorStyle());
                messageLabel.setText(result.getMessage());
            }
        });

        copyBtn.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(passwordDisplay.getText());
            Clipboard.getSystemClipboard().setContent(cc);
            copyBtn.setText("Copied ✓");
        });

        backLink.setOnAction(e -> {
            stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
            stage.setTitle("IPOS-PU | Registration");
        });
    }

    private HBox buildMinimalTopBar(Stage stage) {
        Label logo = new Label("IPOS-PU");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("← Cancel");
        cancelBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white;" +
                           "-fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 7 14; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
            stage.setTitle("IPOS-PU | Registration");
        });

        HBox bar = new HBox(logo, spacer, cancelBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 0, 24));
        bar.setPrefHeight(64);
        bar.setMinHeight(64);
        bar.setMaxHeight(64);
        bar.setStyle("-fx-background-color: " + AppStyles.NAVY + ";");
        return bar;
    }
}
