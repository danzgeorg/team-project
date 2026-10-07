package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class CartScreen extends BorderPane {

    private final CartService cartService = new CartService();
    private final Member member;
    private final Stage stage;

    public CartScreen(Stage stage, Member member) {
        this.stage  = stage;
        this.member = member;
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "cart"));
        refresh();
    }

    private void refresh() {
        new Thread(() -> {
            List<CartItem> items = cartService.getCart(member);
            javafx.application.Platform.runLater(() -> setCenter(buildContent(items)));
        }).start();
    }

    private ScrollPane buildContent(List<CartItem> items) {
        // ── Header ────────────────────────────────────────────────────────────
        Button backBtn = new Button("← Back to Catalogue");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.PRIMARY + ";" +
                         "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: transparent;");
        backBtn.setOnAction(e -> {
            stage.getScene().setRoot(new CatalogueScreen(stage, member));
            stage.setTitle("IPOS-PU | Catalogue");
        });

        Label pageTitle = new Label("Your Cart");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        int itemCount = items.stream().mapToInt(CartItem::getQuantity).sum();
        Label pageSub = new Label(itemCount + " item" + (itemCount == 1 ? "" : "s") + " in your cart");
        pageSub.setStyle(AppStyles.bodyMuted());

        VBox header = new VBox(10, backBtn, pageTitle, pageSub);

        // ── Main body ─────────────────────────────────────────────────────────
        HBox body;
        if (items.isEmpty()) {
            body = new HBox(buildEmptyState());
            body.setAlignment(Pos.CENTER);
        } else {
            VBox itemList = buildItemList(items);
            VBox summary  = buildSummary(items);
            HBox.setHgrow(itemList, Priority.ALWAYS);
            body = new HBox(24, itemList, summary);
            body.setAlignment(Pos.TOP_LEFT);
        }

        VBox page = new VBox(24, header, body);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private VBox buildItemList(List<CartItem> items) {
        VBox list = new VBox(12);
        for (CartItem item : items) {
            list.getChildren().add(buildItemCard(item));
        }
        return list;
    }

    private VBox buildItemCard(CartItem item) {
        // Product info
        Label name = new Label(item.getProduct().getName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label code = new Label(item.getProduct().getCode());
        code.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;" +
                      "-fx-background-color: " + AppStyles.SURFACE_CONTAINER + ";" +
                      "-fx-background-radius: 4; -fx-padding: 2 6;" +
                      "-fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        Label unitPrice = new Label(String.format("£%.2f each", item.getProduct().getPrice()));
        unitPrice.setStyle(AppStyles.bodyMuted());

        VBox productInfo = new VBox(4, name, code, unitPrice);
        HBox.setHgrow(productInfo, Priority.ALWAYS);

        // Quantity controls
        Button minus = new Button("−");
        minus.setStyle(AppStyles.secondaryBtn() + "-fx-min-width: 32; -fx-min-height: 32; -fx-font-size: 16px;");

        TextField qtyField = new TextField(String.valueOf(item.getQuantity()));
        qtyField.setStyle(AppStyles.inputField() + "-fx-pref-width: 64; -fx-alignment: center;");
        qtyField.setMaxWidth(64);
        qtyField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        Button plus = new Button("+");
        plus.setStyle(AppStyles.secondaryBtn() + "-fx-min-width: 32; -fx-min-height: 32; -fx-font-size: 16px;");

        HBox qtyBox = new HBox(8, minus, qtyField, plus);
        qtyBox.setAlignment(Pos.CENTER);

        // Line total
        Label lineTotal = new Label(String.format("£%.2f", item.getLineTotal()));
        lineTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");
        lineTotal.setMinWidth(70);
        lineTotal.setAlignment(Pos.CENTER_RIGHT);

        // Remove button
        Button remove = new Button("Remove");
        remove.setStyle("-fx-background-color: " + AppStyles.ERROR_CONT + ";" +
                        "-fx-text-fill: " + AppStyles.ON_ERROR_CONT + ";" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 6; -fx-cursor: hand;");

        // Stock warning
        Label stockWarning = new Label();
        stockWarning.setStyle(AppStyles.errorStyle());

        // Card container
        HBox row = new HBox(16, productInfo, qtyBox, lineTotal, remove);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16));

        VBox card = new VBox(0, row);
        card.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        card.setEffect(AppStyles.subtleShadow());

        Runnable applyDirectQuantity = () -> {
            String raw = qtyField.getText().trim();
            if (raw.isBlank()) {
                qtyField.setText(String.valueOf(item.getQuantity()));
                return;
            }

            int newQty;
            try {
                newQty = Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                qtyField.setText(String.valueOf(item.getQuantity()));
                return;
            }

            if (newQty <= 0) {
                new Thread(() -> {
                    cartService.removeFromCart(item.getId());
                    javafx.application.Platform.runLater(this::refresh);
                }).start();
                return;
            }

            if (newQty == item.getQuantity()) {
                return;
            }

            new Thread(() -> {
                boolean ok = cartService.updateQuantity(item.getId(), item.getProduct().getId(), newQty);
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        refresh();
                    } else {
                        qtyField.setText(String.valueOf(item.getQuantity()));
                        stockWarning.setText("Max available stock reached");
                        if (!card.getChildren().contains(stockWarning)) {
                            card.getChildren().add(stockWarning);
                        }
                    }
                });
            }).start();
        };

        qtyField.setOnAction(e -> applyDirectQuantity.run());
        qtyField.focusedProperty().addListener((obs, oldFocused, focused) -> {
            if (!focused) {
                applyDirectQuantity.run();
            }
        });

        // Button handlers (declared after all labels are initialised)
        minus.setOnAction(e -> {
            int newQty = item.getQuantity() - 1;
            minus.setDisable(true);
            if (newQty <= 0) {
                new Thread(() -> {
                    cartService.removeFromCart(item.getId());
                    javafx.application.Platform.runLater(() -> { minus.setDisable(false); refresh(); });
                }).start();
            } else {
                new Thread(() -> {
                    boolean ok = cartService.updateQuantity(item.getId(), item.getProduct().getId(), newQty);
                    javafx.application.Platform.runLater(() -> {
                        minus.setDisable(false);
                        if (ok) {
                            refresh();
                        }
                    });
                }).start();
            }
        });

        plus.setOnAction(e -> {
            int newQty = item.getQuantity() + 1;
            plus.setDisable(true);
            new Thread(() -> {
                boolean ok = cartService.updateQuantity(item.getId(), item.getProduct().getId(), newQty);
                javafx.application.Platform.runLater(() -> {
                    plus.setDisable(false);
                    if (ok) {
                        refresh();
                    } else {
                        stockWarning.setText("Max available stock reached");
                        if (!card.getChildren().contains(stockWarning)) {
                            card.getChildren().add(stockWarning);
                        }
                    }
                });
            }).start();
        });

        remove.setOnAction(e -> {
            remove.setDisable(true);
            new Thread(() -> {
                cartService.removeFromCart(item.getId());
                javafx.application.Platform.runLater(() -> { remove.setDisable(false); refresh(); });
            }).start();
        });

        return card;
    }

    private VBox buildSummary(List<CartItem> items) {
        Label summaryTitle = new Label("Order Summary");
        summaryTitle.setStyle(AppStyles.sectionTitle());

        Separator sep = new Separator();

        Label totalCaption = new Label("Total (excl. delivery)");
        totalCaption.setStyle(AppStyles.bodyMuted());

        double total = cartService.getCartTotal(items);
        Label totalLabel = new Label(String.format("£%.2f", total));
        totalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Button checkoutBtn = new Button("Proceed to Checkout");
        checkoutBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 12 24;");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setOnAction(e -> {
            stage.getScene().setRoot(new CheckoutScreen(stage, member));
            stage.setTitle("IPOS-PU | Checkout");
        });

        Button continueShopping = new Button("← Continue Shopping");
        continueShopping.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 10 20;");
        continueShopping.setMaxWidth(Double.MAX_VALUE);
        continueShopping.setOnAction(e -> {
            stage.getScene().setRoot(new CatalogueScreen(stage, member));
            stage.setTitle("IPOS-PU | Catalogue");
        });

        VBox summary = new VBox(16, summaryTitle, sep, totalCaption, totalLabel,
                                checkoutBtn, continueShopping);
        summary.setPadding(new Insets(24));
        summary.setMinWidth(260);
        summary.setMaxWidth(280);
        summary.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        summary.setEffect(AppStyles.subtleShadow());
        return summary;
    }

    private VBox buildEmptyState() {
        Label icon = new Label("🛒");
        icon.setStyle("-fx-font-size: 48px;");

        Label msg = new Label("Your cart is empty");
        msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label sub = new Label("Browse the catalogue and add items to get started.");
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
