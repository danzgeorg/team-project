package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OrdersScreen extends BorderPane {

    public OrdersScreen(Stage stage, Member member) {
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "orders"));
        setCenter(buildContent());
    }

    private ScrollPane buildContent() {
        Label pageTitle = new Label("My Orders");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label("Track your requisition statuses and purchase history.");
        pageSub.setStyle(AppStyles.bodyMuted());

        // ── Coming soon placeholder ────────────────────────────────────────
        Label icon = new Label("🛒");
        icon.setStyle("-fx-font-size: 40px;");

        Label comingSoon = new Label("Orders Coming Soon");
        comingSoon.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label comingSub = new Label("Order placement and tracking will be available in the next release.");
        comingSub.setStyle(AppStyles.bodyMuted());
        comingSub.setWrapText(true);

        VBox placeholder = new VBox(10, icon, comingSoon, comingSub);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(48));
        placeholder.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + ";" +
                             "-fx-background-radius: 12;");
        placeholder.setEffect(AppStyles.subtleShadow());

        VBox page = new VBox(28, new VBox(6, pageTitle, pageSub), placeholder);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }
}
