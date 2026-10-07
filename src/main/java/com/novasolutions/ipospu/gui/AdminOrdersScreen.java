package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.db.MemberDAO;
import com.novasolutions.ipospu.db.OrderDAO;
import com.novasolutions.ipospu.db.PaymentDAO;
import com.novasolutions.ipospu.impl.PU_SMTP_API;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.Order;
import com.novasolutions.ipospu.model.OrderItem;
import com.novasolutions.ipospu.model.PaymentRecord;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminOrdersScreen extends BorderPane {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> STATUS_FLOW =
            List.of("RECEIVED", "PROCESSING", "SHIPPED", "DELIVERED");

    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final PU_SMTP_API smtpAPI = new PU_SMTP_API();
    private Order selectedOrder = null;

    public AdminOrdersScreen(Stage stage, Member member) {
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "order-admin"));

        if (member == null || !"ADMIN".equals(member.memberType())) {
            Label denied = new Label("Access denied – admin only.");
            denied.setStyle(AppStyles.bodyMuted());
            denied.setPadding(new Insets(32));
            setCenter(denied);
            return;
        }
        setCenter(buildContent());
    }

    private ScrollPane buildContent() {
        Label pageTitle = new Label("Order Management");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label("View all orders and advance their fulfilment status.");
        pageSub.setStyle(AppStyles.bodyMuted());

        // ── Order list ────────────────────────────────────────────────────────
        ListView<Order> orderList = new ListView<>();
        orderList.setPrefHeight(320);
        orderList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Order o, boolean empty) {
                super.updateItem(o, empty);
                if (empty || o == null) { setText(null); return; }
                setText(String.format("#%d  |  %s  |  £%.2f  |  %s",
                        o.getId(),
                        o.getCreatedAt().format(DT_FMT),
                        o.getTotalAmount(),
                        o.getStatus()));
            }
        });

        ListView<PaymentRecord> paymentsList = new ListView<>();
        paymentsList.setPrefHeight(280);
        paymentsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PaymentRecord payment, boolean empty) {
                super.updateItem(payment, empty);
                if (empty || payment == null) {
                    setText(null);
                    return;
                }
                String orderRef = payment.orderId() != null ? "#" + payment.orderId() : "No order linked";
                String customer = payment.customerEmail() != null ? payment.customerEmail() : "Unknown customer";
                setText(String.format(
                        "Payment #%d  |  %s  |  £%.2f  |  %s  |  %s",
                        payment.id(),
                        orderRef,
                        payment.amount(),
                        payment.status(),
                        customer
                ));
            }
        });

        // ── Detail panel ──────────────────────────────────────────────────────
        Label detailTitle = new Label("Order Details");
        detailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label detailInfo  = new Label("Select an order from the list above.");
        detailInfo.setStyle(AppStyles.bodyMuted());
        detailInfo.setWrapText(true);

        VBox itemsBox = new VBox(4);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button advanceBtn = new Button("Advance Status →");
        advanceBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 8 20;");
        advanceBtn.setDisable(true);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");

        HBox actions = new HBox(12, advanceBtn, refreshBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox detailCard = new VBox(12, detailTitle, detailInfo, itemsBox, actions, statusLabel);
        detailCard.setPadding(new Insets(20));
        detailCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST +
                            "; -fx-background-radius: 12;");
        detailCard.setEffect(AppStyles.subtleShadow());

        // ── Wire up selection ─────────────────────────────────────────────────
        orderList.getSelectionModel().selectedItemProperty().addListener((obs, old, order) -> {
            selectedOrder = order;
            statusLabel.setText("");
            itemsBox.getChildren().clear();
            if (order == null) {
                detailInfo.setText("Select an order from the list above.");
                advanceBtn.setDisable(true);
                return;
            }

            detailInfo.setText(String.format(
                    "Order #%d   |   %s   |   Status: %s%n" +
                    "Delivery: %s",
                    order.getId(),
                    order.getCreatedAt().format(DT_FMT),
                    order.getStatus(),
                    order.getGuestAddress() != null ? order.getGuestAddress() : "—"));

            for (OrderItem item : order.getItems()) {
                Label il = new Label(String.format("  • %s  ×%d  @ £%.2f",
                        item.productName() != null ? item.productName() : "Product #" + item.stockItemId(),
                        item.quantity(),
                        item.unitPrice()));
                il.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");
                itemsBox.getChildren().add(il);
            }

            boolean canAdvance = STATUS_FLOW.contains(order.getStatus()) &&
                    STATUS_FLOW.indexOf(order.getStatus()) < STATUS_FLOW.size() - 1;
            advanceBtn.setDisable(!canAdvance);
            if (canAdvance) {
                String next = STATUS_FLOW.get(STATUS_FLOW.indexOf(order.getStatus()) + 1);
                advanceBtn.setText("Advance to " + next + " →");
            } else {
                advanceBtn.setText("Status: " + order.getStatus());
            }
        });

        // ── Advance status ────────────────────────────────────────────────────
        advanceBtn.setOnAction(e -> {
            if (selectedOrder == null) return;
            int idx = STATUS_FLOW.indexOf(selectedOrder.getStatus());
            if (idx < 0 || idx >= STATUS_FLOW.size() - 1) return;

            String newStatus = STATUS_FLOW.get(idx + 1);
            // Capture ID before background thread (selection change clears selectedOrder)
            long orderId = selectedOrder.getId();
            Order orderToUpdate = selectedOrder;
            advanceBtn.setDisable(true);

            new Thread(() -> {
                orderDAO.updateOrderStatus(orderId, newStatus);
                sendOrderStatusUpdateEmail(orderToUpdate, newStatus);
                List<Order> refreshed = orderDAO.getAllOrders();
                Platform.runLater(() -> {
                    orderList.getItems().setAll(refreshed);
                    // Re-select the same order by captured ID
                    refreshed.stream()
                            .filter(o -> o.getId() == orderId)
                            .findFirst()
                            .ifPresent(o -> orderList.getSelectionModel().select(o));
                    statusLabel.setStyle(AppStyles.successStyle());
                    statusLabel.setText("Status updated to " + newStatus + ".");
                });
            }).start();
        });

        // ── Refresh ───────────────────────────────────────────────────────────
        Runnable loadOrders = () -> {
            new Thread(() -> {
                List<Order> orders = orderDAO.getAllOrders();
                Platform.runLater(() -> orderList.getItems().setAll(orders));
            }).start();
        };

        refreshBtn.setOnAction(e -> loadOrders.run());
        loadOrders.run();  // initial load

        Runnable loadPayments = () -> {
            new Thread(() -> {
                List<PaymentRecord> payments = paymentDAO.getAllPayments();
                Platform.runLater(() -> paymentsList.getItems().setAll(payments));
            }).start();
        };

        loadPayments.run();

        // ── Layout ───────────────────────────────────────────────────────────
        VBox listCard = new VBox(12, new Label("All Orders"), orderList);
        listCard.setPadding(new Insets(20));
        listCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST +
                          "; -fx-background-radius: 12;");
        listCard.setEffect(AppStyles.subtleShadow());
        ((Label) listCard.getChildren().get(0)).setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label paymentsTitle = new Label("Payments");
        paymentsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label paymentsHint = new Label("Payment table with linked order and customer information.");
        paymentsHint.setStyle(AppStyles.bodyMuted());
        paymentsHint.setWrapText(true);

        VBox paymentsCard = new VBox(12, paymentsTitle, paymentsHint, paymentsList);
        paymentsCard.setPadding(new Insets(20));
        paymentsCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST +
                              "; -fx-background-radius: 12;");
        paymentsCard.setEffect(AppStyles.subtleShadow());

        VBox page = new VBox(24,
                new VBox(6, pageTitle, pageSub),
                listCard,
                paymentsCard,
                detailCard);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private void sendOrderStatusUpdateEmail(Order order, String newStatus) {
        if (order == null) {
            return;
        }

        String recipient = order.getGuestEmail();
        String customerName = "Customer";

        if ((recipient == null || recipient.isBlank()) && order.getMemberId() != null) {
            Member member = memberDAO.findById(order.getMemberId());
            if (member != null) {
                recipient = member.email();
                customerName = member.fullName();
            }
        }

        if (recipient == null || recipient.isBlank()) {
            return;
        }

        String body = """
                Hello %s,

                Your order #%d has been updated.

                New status: %s
                Delivery address: %s

                You can log in to IPOS-PU and open "My Orders" to view the latest update.

                IPOS-PU
                """.formatted(
                customerName,
                order.getId(),
                newStatus,
                order.getGuestAddress() != null ? order.getGuestAddress() : "Not provided"
        );

        smtpAPI.sendEmail(recipient, "Order Update — #" + order.getId(), body);
    }
}
