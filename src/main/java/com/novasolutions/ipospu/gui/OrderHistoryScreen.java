package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.db.OrderDAO;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.Order;
import com.novasolutions.ipospu.model.OrderItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryScreen extends BorderPane {

    private final OrderDAO orderDAO = new OrderDAO();
    private final Member member;
    private final Stage stage;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public OrderHistoryScreen(Stage stage, Member member) {
        this.stage  = stage;
        this.member = member;
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "orders"));
        loadOrders();
    }

    private void loadOrders() {
        new Thread(() -> {
            List<Order> orders = orderDAO.getOrdersForMember(member.id());
            javafx.application.Platform.runLater(() -> setCenter(buildContent(orders)));
        }).start();
    }

    private ScrollPane buildContent(List<Order> orders) {
        Label pageTitle = new Label("My Orders");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label(orders.size() + " order" + (orders.size() == 1 ? "" : "s") + " placed");
        pageSub.setStyle(AppStyles.bodyMuted());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        refreshBtn.setOnAction(e -> loadOrders());

        HBox titleRow = new HBox(16, pageTitle, refreshBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pageTitle, Priority.ALWAYS);

        VBox header = new VBox(8, titleRow, pageSub);

        VBox orderList;
        if (orders.isEmpty()) {
            orderList = buildEmptyState();
        } else {
            orderList = new VBox(16);
            for (Order order : orders) {
                orderList.getChildren().add(buildOrderCard(order));
            }
        }

        VBox page = new VBox(24, header, orderList);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private VBox buildOrderCard(Order order) {
        // Header row
        Label orderIdLabel = new Label("Order #" + order.getId());
        orderIdLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label dateLabel = new Label(order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "");
        dateLabel.setStyle(AppStyles.bodyMuted());

        HBox.setHgrow(orderIdLabel, Priority.ALWAYS);
        HBox cardHeader = new HBox(12, orderIdLabel, dateLabel);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        // Status badge
        Label statusBadge = new Label(order.getStatus());
        String badgeColor = switch (order.getStatus()) {
            case "RECEIVED"   -> "#e8f5e9";
            case "PROCESSING" -> AppStyles.TERT_FIXED;
            case "DISPATCHED" -> AppStyles.SURFACE_CONTAINER;
            case "DELIVERED"  -> AppStyles.SURFACE_HIGH;
            default           -> AppStyles.SURFACE_CONTAINER;
        };
        String badgeText = switch (order.getStatus()) {
            case "RECEIVED"   -> AppStyles.SURFACE_TINT;
            case "PROCESSING" -> AppStyles.ON_TERT_VAR;
            default           -> AppStyles.ON_SURFACE_VAR;
        };
        statusBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;" +
                             "-fx-background-color: " + badgeColor + ";" +
                             "-fx-text-fill: " + badgeText + ";" +
                             "-fx-background-radius: 12; -fx-padding: 4 12;");

        // Items
        VBox itemLines = new VBox(6);
        for (OrderItem item : order.getItems()) {
            Label line = new Label(String.format("%s  x%d   £%.2f",
                    item.productName(), item.quantity(), item.lineTotal()));
            line.setStyle(AppStyles.bodyMuted());
            line.setWrapText(true);
            itemLines.getChildren().add(line);
        }

        Separator sep = new Separator();

        // Totals
        VBox totals = new VBox(4);
        if (order.getDiscountAmount() > 0) {
            Label discLine = new Label(String.format("Loyalty Discount (10%%):  -£%.2f", order.getDiscountAmount()));
            discLine.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";");
            totals.getChildren().add(discLine);
        }
        Label totalLine = new Label(String.format("Total:  £%.2f", order.getTotalAmount()));
        totalLine.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");
        totals.getChildren().add(totalLine);

        VBox card = new VBox(12, cardHeader, statusBadge, itemLines, sep, totals);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        card.setEffect(AppStyles.subtleShadow());
        return card;
    }

    private VBox buildEmptyState() {
        Label icon = new Label("📦");
        icon.setStyle("-fx-font-size: 48px;");

        Label msg = new Label("No orders yet");
        msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label sub = new Label("Place your first order from the catalogue.");
        sub.setStyle(AppStyles.bodyMuted());

        Button browseBtn = new Button("Browse Catalogue");
        browseBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 12 24;");
        browseBtn.setOnAction(e -> {
            stage.getScene().setRoot(new CatalogueScreen(stage, member));
            stage.setTitle("IPOS-PU | Catalogue");
        });

        VBox empty = new VBox(12, icon, msg, sub, browseBtn);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80));
        return empty;
    }
}