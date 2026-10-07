package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.service.ChangePasswordResult;
import com.novasolutions.ipospu.service.MembershipService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChangePasswordScreen extends BorderPane {

    private final MembershipService membershipService = new MembershipService();

    public ChangePasswordScreen(Stage stage, Member member) {

        // ── Minimal top bar ───────────────────────────────────────────────
        setTop(buildMinimalTopBar());

        // ── Centered card ─────────────────────────────────────────────────
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");
        content.setPadding(new Insets(48));

        // Lock icon area
        Label lockIcon = new Label("🔒");
        lockIcon.setStyle("-fx-font-size: 28px;");

        VBox iconCircle = new VBox(lockIcon);
        iconCircle.setAlignment(Pos.CENTER);
        iconCircle.setPrefSize(64, 64);
        iconCircle.setStyle("-fx-background-color: " + AppStyles.SURFACE_HIGH + ";" +
                            "-fx-background-radius: 32;");

        Label title = new Label("Update Password");
        title.setStyle(AppStyles.headline());

        Label subtitle = new Label("For your security, a password reset is required for your first-time login.");
        subtitle.setStyle(AppStyles.bodyMuted());
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(320);

        VBox headerBlock = new VBox(12, iconCircle, title, subtitle);
        headerBlock.setAlignment(Pos.CENTER);

        // New password field
        Label newPassLabel = new Label("NEW PASSWORD");
        newPassLabel.setStyle(AppStyles.fieldLabel());
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("••••••••••••");
        newPasswordField.setStyle(AppStyles.inputField());
        VBox newPassGroup = new VBox(6, newPassLabel, newPasswordField);

        // Confirm password field
        Label confirmLabel = new Label("CONFIRM PASSWORD");
        confirmLabel.setStyle(AppStyles.fieldLabel());
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("••••••••••••");
        confirmPasswordField.setStyle(AppStyles.inputField());
        VBox confirmGroup = new VBox(6, confirmLabel, confirmPasswordField);

        // Requirements hint box
        Label reqHeader = new Label("ℹ  Security Requirements:");
        reqHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        Label reqBody = new Label("At least 6 characters  ·  Mix of letters and numbers  ·  No recent passwords");
        reqBody.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
        reqBody.setWrapText(true);

        VBox requirementsBox = new VBox(6, reqHeader, reqBody);
        requirementsBox.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOW + ";" +
                                 "-fx-background-radius: 8; -fx-padding: 14;");

        // Status message
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        // Reset button
        Button resetBtn = new Button("Reset Password");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 13 0;");
        resetBtn.setOnMouseEntered(e -> resetBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 13 0; -fx-opacity: 0.9;"));
        resetBtn.setOnMouseExited(e -> resetBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 13 0;"));

        // Assemble card
        VBox card = new VBox(20,
                headerBlock,
                newPassGroup,
                confirmGroup,
                requirementsBox,
                messageLabel,
                resetBtn
        );
        card.setStyle(AppStyles.card());
        card.setMaxWidth(420);
        card.setEffect(AppStyles.cardShadow());

        content.setAlignment(Pos.CENTER);
        content.getChildren().add(card);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        setCenter(scroll);

        // ── Handler ───────────────────────────────────────────────────────
        resetBtn.setOnAction(e -> {
            ChangePasswordResult result = membershipService.changePassword(
                    member.email(),
                    newPasswordField.getText(),
                    confirmPasswordField.getText()
            );

            if (result.isSuccess()) {
                messageLabel.setStyle(AppStyles.successStyle());
                messageLabel.setText(result.getMessage());

                stage.getScene().setRoot(new DashboardScreen(stage, member));
                stage.setTitle("IPOS-PU | Dashboard");
            } else {
                messageLabel.setStyle(AppStyles.errorStyle());
                messageLabel.setText(result.getMessage());
            }
        });
    }

    private HBox buildMinimalTopBar() {
        Label logo = new Label("IPOS-PU");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label secPortal = new Label("Security Portal");
        secPortal.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.5);");

        HBox bar = new HBox(logo, spacer, secPortal);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(64);
        bar.setMinHeight(64);
        bar.setMaxHeight(64);
        bar.setStyle("-fx-background-color: " + AppStyles.NAVY + ";");
        return bar;
    }
}
