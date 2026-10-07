package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.service.OrderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class OrderConfirmationScreen extends BorderPane {

    public OrderConfirmationScreen(Stage stage, Member member, OrderService.CheckoutOutcome outcome) {
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "cart"));
        setCenter(buildContent(stage, member, outcome));
    }

    private ScrollPane buildContent(Stage stage, Member member, OrderService.CheckoutOutcome outcome) {
        // ── Success icon & headline ───────────────────────────────────────────
        Label checkIcon = new Label("✓");
        checkIcon.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";");

        Label title = new Label("Order Confirmed!");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label orderId = new Label("Order #" + outcome.orderId());
        orderId.setStyle("-fx-font-size: 15px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        Label message = new Label(outcome.message());
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);

        // ── Info boxes ───────────────────────────────────────────────────────
        Label deliveryNote = new Label("Delivering to: " + outcome.deliveryAddress());
        deliveryNote.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";" +
                              "-fx-background-color: " + AppStyles.SURFACE_CONTAINER + ";" +
                              "-fx-background-radius: 8; -fx-padding: 14 20;");
        deliveryNote.setWrapText(true);
        deliveryNote.setMaxWidth(460);
        deliveryNote.setAlignment(Pos.CENTER);

        Label emailNote = new Label("A confirmation email has been sent to " + outcome.contactEmail());
        emailNote.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";" +
                           "-fx-background-color: " + AppStyles.SURFACE_CONTAINER + ";" +
                           "-fx-background-radius: 8; -fx-padding: 14 20;");
        emailNote.setWrapText(true);
        emailNote.setMaxWidth(460);
        emailNote.setAlignment(Pos.CENTER);

        // ── Status badge ─────────────────────────────────────────────────────
        Label statusBadge = new Label("Status: RECEIVED");
        statusBadge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";" +
                             "-fx-background-color: #e8f5e9; -fx-background-radius: 20; -fx-padding: 6 18;");

        // ── Actions ───────────────────────────────────────────────────────────
        Button continueBtn = new Button("Continue Shopping");
        continueBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 13 32; -fx-font-size: 14px;");
        continueBtn.setMinWidth(200);
        continueBtn.setOnAction(e -> {
            stage.getScene().setRoot(new CatalogueScreen(stage, member));
            stage.setTitle("IPOS-PU | Catalogue");
        });

        HBox actions = new HBox(16, continueBtn);
        if (!member.isGuest()) {
            Button viewOrdersBtn = new Button("View My Orders");
            viewOrdersBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 12 28; -fx-font-size: 14px;");
            viewOrdersBtn.setMinWidth(200);
            viewOrdersBtn.setOnAction(e -> {
                stage.getScene().setRoot(new OrderHistoryScreen(stage, member));
                stage.setTitle("IPOS-PU | My Orders");
            });
            actions.getChildren().add(viewOrdersBtn);
        }
        actions.setAlignment(Pos.CENTER);

        // ── Card ─────────────────────────────────────────────────────────────
        VBox card = new VBox(20, checkIcon, title, orderId, message, statusBadge, deliveryNote, emailNote, actions);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(56, 48, 56, 48));
        card.setMaxWidth(560);
        card.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 16;");
        card.setEffect(AppStyles.subtleShadow());

        StackPane centeredCard = new StackPane(card);
        centeredCard.setAlignment(Pos.CENTER);
        centeredCard.setPadding(new Insets(40));
        centeredCard.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(centeredCard);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }
}
