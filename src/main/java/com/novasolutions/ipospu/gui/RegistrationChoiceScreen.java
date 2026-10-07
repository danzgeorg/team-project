package com.novasolutions.ipospu.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegistrationChoiceScreen extends StackPane {

    public RegistrationChoiceScreen(Stage stage) {
        setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        // ── Page header ───────────────────────────────────────────────────
        Label title = new Label("IPOS-PU");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.NAVY + ";");

        Label subtitle = new Label("Select your account type to begin the procurement journey.");
        subtitle.setStyle(AppStyles.bodyMuted());

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // ── Cards ─────────────────────────────────────────────────────────
        VBox nonCommCard  = buildCard(
                "Non-Commercial Member",
                "Ideal for individual members. Access the standard catalogue\n" +
                "and basic procurement tracking. Account created immediately.",
                new String[]{
                        "Access to standard catalogue",
                        "Basic order history tracking",
                        "Individual project management"
                },
                false
        );

        VBox commCard = buildCard(
                "Commercial Member",
                "Designed for businesses and organisations. Enterprise-grade tools,\n" +
                "bulk ordering, and priority logistics.",
                new String[]{
                        "Priority bulk procurement access",
                        "Multi-user firm dashboards",
                        "Tax-exempt purchasing workflows"
                },
                true
        );

        Button nonCommBtn = buildSelectButton(false);
        Button commBtn    = buildSelectButton(true);

        nonCommCard.getChildren().add(nonCommBtn);
        commCard.getChildren().add(commBtn);

        HBox cards = new HBox(24, nonCommCard, commCard);
        cards.setAlignment(Pos.CENTER);
        HBox.setHgrow(nonCommCard, Priority.ALWAYS);
        HBox.setHgrow(commCard, Priority.ALWAYS);
        cards.setMaxWidth(860);

        // ── Back link ─────────────────────────────────────────────────────
        Button backLink = new Button("← Back to Login");
        backLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";" +
                          "-fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: transparent;");

        // ── Container ─────────────────────────────────────────────────────
        VBox container = new VBox(36, header, cards, backLink);
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(860);

        StackPane centeredWrapper = new StackPane(container);
        centeredWrapper.setAlignment(Pos.CENTER);
        centeredWrapper.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");
        centeredWrapper.setPadding(new Insets(48));

        ScrollPane scroll = new ScrollPane(centeredWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");

        setAlignment(Pos.CENTER);
        getChildren().add(scroll);

        // ── Handlers ──────────────────────────────────────────────────────
        nonCommBtn.setOnAction(e -> {
            stage.getScene().setRoot(new NonCommercialRegisterScreen(stage));
            stage.setTitle("IPOS-PU | Register - Non-Commercial");
        });

        commBtn.setOnAction(e -> {
            stage.getScene().setRoot(new CommercialRegisterScreen(stage));
            stage.setTitle("IPOS-PU | Apply - Commercial");
        });

        backLink.setOnAction(e -> {
            stage.getScene().setRoot(new LoginScreen());
            stage.setTitle("IPOS-PU | Login");
        });
    }

    private VBox buildCard(String titleText, String descText, String[] features, boolean isPrimary) {
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label desc = new Label(descText);
        desc.setStyle(AppStyles.bodyMuted());
        desc.setWrapText(true);

        VBox featureList = new VBox(10);
        for (String feature : features) {
            Label bullet = new Label("✓  " + feature);
            bullet.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
            featureList.getChildren().add(bullet);
        }

        VBox card = new VBox(16, title, desc, featureList);
        card.setPadding(new Insets(32));
        card.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + ";" +
                      "-fx-background-radius: 12;");
        card.setEffect(AppStyles.cardShadow());
        card.setMinWidth(340);

        return card;
    }

    private Button buildSelectButton(boolean isPrimary) {
        Button btn = new Button(isPrimary ? "Select →" : "Select");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(44);
        btn.setStyle(isPrimary ? AppStyles.ghostGradBtn() + "-fx-padding: 12 0;"
                               : AppStyles.secondaryBtn() + "-fx-padding: 12 0;");
        return btn;
    }
}
