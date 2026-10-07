package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Shared navy top navigation bar for all authenticated screens.
 */
public class TopBar extends HBox {

    public TopBar(Stage stage, Member member) {
        Label logo = new Label("IPOS-PU");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label memberName = new Label(member != null ? member.fullName() : "");
        memberName.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.55);");

        Button logoutBtn = buildLogoutButton();
        logoutBtn.setOnAction(e -> {
            stage.getScene().setRoot(new LoginScreen());
            stage.setTitle("IPOS-PU | Login");
        });

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 20, 0, 24));
        setPrefHeight(64);
        setMinHeight(64);
        setMaxHeight(64);
        setSpacing(16);
        setStyle("-fx-background-color: " + AppStyles.NAVY + ";");

        getChildren().addAll(logo, spacer, memberName, logoutBtn);
    }

    private Button buildLogoutButton() {
        String base = "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: white;" +
                      "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6;" +
                      "-fx-padding: 7 14; -fx-cursor: hand;";
        String hover = "-fx-background-color: rgba(255,255,255,0.20); -fx-text-fill: white;" +
                       "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6;" +
                       "-fx-padding: 7 14; -fx-cursor: hand;";

        Button btn = new Button("Logout");
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }
}
