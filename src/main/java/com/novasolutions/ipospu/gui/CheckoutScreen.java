package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.controller.PromotionsController;
import com.novasolutions.ipospu.service.CartService;
import com.novasolutions.ipospu.service.OrderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class CheckoutScreen extends BorderPane {

    private final OrderService orderService = new OrderService();
    private final CartService  cartService  = new CartService();
    private final PromotionsController promotionsController = new PromotionsController();
    private final Member member;
    private final Stage stage;

    public CheckoutScreen(Stage stage, Member member) {
        this.stage  = stage;
        this.member = member;
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "cart"));
        setCenter(buildContent());
    }

    private ScrollPane buildContent() {
        List<CartItem> items = cartService.getCart(member);

        // ── Header ────────────────────────────────────────────────────────────
        Button backBtn = new Button("← Back to Cart");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.PRIMARY + ";" +
                         "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: transparent;");
        backBtn.setOnAction(e -> {
            stage.getScene().setRoot(new CartScreen(stage, member));
            stage.setTitle("IPOS-PU | My Cart");
        });

        Label pageTitle = new Label("Checkout");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label(member.isGuest()
                ? "Guest checkout: enter your email, delivery address, and payment details."
                : "Enter your delivery address and payment details.");
        pageSub.setStyle(AppStyles.bodyMuted());

        VBox header = new VBox(10, backBtn, pageTitle, pageSub);

        // ── Delivery address section ──────────────────────────────────────────
        Label addressTitle = new Label("Delivery Address");
        addressTitle.setStyle(AppStyles.sectionTitle());

        VBox guestEmailCard = null;
        TextField guestEmailField = null;
        if (member.isGuest()) {
            Label guestEmailTitle = new Label("Guest Contact");
            guestEmailTitle.setStyle(AppStyles.sectionTitle());

            Label guestEmailHelp = new Label("Use this email to receive your order confirmation.");
            guestEmailHelp.setStyle(AppStyles.bodyMuted());
            guestEmailHelp.setWrapText(true);

            Label emailLabel = new Label("EMAIL ADDRESS");
            emailLabel.setStyle(AppStyles.fieldLabel());

            guestEmailField = new TextField();
            guestEmailField.setPromptText("name@company.com");
            guestEmailField.setStyle(AppStyles.inputField());

            VBox emailGroup = new VBox(6, emailLabel, guestEmailField);

            guestEmailCard = new VBox(16, guestEmailTitle, guestEmailHelp, emailGroup);
            guestEmailCard.setPadding(new Insets(28));
            guestEmailCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
            guestEmailCard.setEffect(AppStyles.subtleShadow());
        }
        final TextField guestEmailInput = guestEmailField;

        Label line1Label = new Label("ADDRESS LINE 1");
        line1Label.setStyle(AppStyles.fieldLabel());
        TextField line1Field = new TextField();
        line1Field.setPromptText("House number and street name");
        line1Field.setStyle(AppStyles.inputField());
        VBox line1Group = new VBox(6, line1Label, line1Field);

        Label line2Label = new Label("ADDRESS LINE 2  (optional)");
        line2Label.setStyle(AppStyles.fieldLabel());
        TextField line2Field = new TextField();
        line2Field.setPromptText("Apartment, suite, etc.");
        line2Field.setStyle(AppStyles.inputField());
        VBox line2Group = new VBox(6, line2Label, line2Field);

        Label cityLabel = new Label("CITY");
        cityLabel.setStyle(AppStyles.fieldLabel());
        TextField cityField = new TextField();
        cityField.setPromptText("London");
        cityField.setStyle(AppStyles.inputField());
        VBox cityGroup = new VBox(6, cityLabel, cityField);
        HBox.setHgrow(cityGroup, Priority.ALWAYS);

        Label postcodeLabel = new Label("POSTCODE");
        postcodeLabel.setStyle(AppStyles.fieldLabel());
        TextField postcodeField = new TextField();
        postcodeField.setPromptText("SW1A 1AA");
        postcodeField.setStyle(AppStyles.inputField());
        VBox postcodeGroup = new VBox(6, postcodeLabel, postcodeField);

        HBox cityRow = new HBox(16, cityGroup, postcodeGroup);
        cityRow.setAlignment(Pos.CENTER_LEFT);

        VBox addressCard = new VBox(16, addressTitle, line1Group, line2Group, cityRow);
        addressCard.setPadding(new Insets(28));
        addressCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        addressCard.setEffect(AppStyles.subtleShadow());

        // ── Payment form ──────────────────────────────────────────────────────
        Label formTitle = new Label("Payment Details");
        formTitle.setStyle(AppStyles.sectionTitle());

        Label cardLabel = new Label("CARD NUMBER");
        cardLabel.setStyle(AppStyles.fieldLabel());
        TextField cardField = new TextField();
        cardField.setPromptText("1234 5678 1234 5678");
        cardField.setStyle(AppStyles.inputField());
        cardField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 16) digits = digits.substring(0, 16);
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i > 0 && i % 4 == 0) formatted.append(' ');
                formatted.append(digits.charAt(i));
            }
            String result = formatted.toString();
            if (!result.equals(newVal)) {
                cardField.setText(result);
                cardField.positionCaret(result.length());
            }
        });
        VBox cardGroup = new VBox(6, cardLabel, cardField);

        Label expiryLabel = new Label("EXPIRY DATE");
        expiryLabel.setStyle(AppStyles.fieldLabel());
        TextField expiryField = new TextField();
        expiryField.setPromptText("MM/YY");
        expiryField.setStyle(AppStyles.inputField());
        expiryField.textProperty().addListener((obs, oldVal, newVal) -> {
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 4) digits = digits.substring(0, 4);
            String result = digits.length() <= 2 ? digits : digits.substring(0, 2) + "/" + digits.substring(2);
            if (!result.equals(newVal)) {
                expiryField.setText(result);
                expiryField.positionCaret(result.length());
            }
        });
        VBox expiryGroup = new VBox(6, expiryLabel, expiryField);

        HBox cardRow = new HBox(16, cardGroup, expiryGroup);
        HBox.setHgrow(cardGroup, Priority.ALWAYS);
        cardRow.setAlignment(Pos.CENTER_LEFT);

        // Loyalty discount notice
        int nextOrder = member.orderCount() + 1;
        boolean loyaltyApplies = member.memberType().equals("NON_COMMERCIAL") && nextOrder % 10 == 0;
        VBox loyaltyNotice = null;
        if (loyaltyApplies) {
            Label loyaltyLabel = new Label("🎉  10th order loyalty discount — 10% will be applied to this order!");
            loyaltyLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";" +
                                  "-fx-background-color: #e8f5e9; -fx-background-radius: 8; -fx-padding: 12 16;");
            loyaltyLabel.setWrapText(true);
            loyaltyNotice = new VBox(loyaltyLabel);
        }

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        Button placeOrderBtn = new Button("Place Order");
        placeOrderBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 13 32; -fx-font-size: 14px;");
        placeOrderBtn.setMaxWidth(Double.MAX_VALUE);

        VBox paymentCard = new VBox(20, formTitle, cardRow);
        if (loyaltyNotice != null) paymentCard.getChildren().add(loyaltyNotice);
        paymentCard.getChildren().addAll(statusLabel, placeOrderBtn);
        paymentCard.setPadding(new Insets(28));
        paymentCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        paymentCard.setEffect(AppStyles.subtleShadow());

        // ── Left column: address + payment stacked ────────────────────────────
        VBox formColumn = guestEmailCard == null
                ? new VBox(20, addressCard, paymentCard)
                : new VBox(20, guestEmailCard, addressCard, paymentCard);
        HBox.setHgrow(formColumn, Priority.ALWAYS);

        // ── Order summary ─────────────────────────────────────────────────────
        Label summaryTitle = new Label("Order Summary");
        summaryTitle.setStyle(AppStyles.sectionTitle());

        VBox itemLines = new VBox(10);
        double subtotal = 0;
        for (CartItem item : items) {
            Label line = new Label(String.format("%s  x%d   £%.2f",
                    item.getProduct().getName(), item.getQuantity(), item.getLineTotal()));
            line.setStyle(AppStyles.bodyMuted());
            line.setWrapText(true);
            itemLines.getChildren().add(line);
            subtotal += item.getLineTotal();
        }

        Separator sep = new Separator();

        // Keep the screen totals aligned with OrderService: promotion discount first, loyalty second.
        double promotionDiscount = promotionsController.calculatePromotionDiscount(items);
        double discount = promotionDiscount + (loyaltyApplies ? (subtotal - promotionDiscount) * 0.10 : 0);
        double loyaltyDiscount = discount - promotionDiscount;
        double total = subtotal - discount;

        VBox totalsBox = new VBox(8);
        if (promotionDiscount > 0) {
            Label promotionLine = new Label(String.format("Promotion Discount:  -£%.2f", promotionDiscount));
            promotionLine.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";");
            totalsBox.getChildren().add(promotionLine);
        }
        if (discount > 0) {
            if (loyaltyDiscount > 0) {
                Label discountLine = new Label(String.format("Loyalty Discount (10%%):  -£%.2f", loyaltyDiscount));
                discountLine.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";");
                totalsBox.getChildren().add(discountLine);
            }
        }
        Label totalLine = new Label(String.format("Total:  £%.2f", total));
        totalLine.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");
        totalsBox.getChildren().add(totalLine);

        VBox summaryCard = new VBox(16, summaryTitle, itemLines, sep, totalsBox);
        summaryCard.setPadding(new Insets(28));
        summaryCard.setMinWidth(300);
        summaryCard.setMaxWidth(340);
        summaryCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        summaryCard.setEffect(AppStyles.subtleShadow());

        // ── Layout ────────────────────────────────────────────────────────────
        HBox body = new HBox(24, formColumn, summaryCard);
        body.setAlignment(Pos.TOP_LEFT);

        VBox page = new VBox(24, header, body);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        // ── Place order handler ───────────────────────────────────────────────
        placeOrderBtn.setOnAction(e -> {
            String line1    = line1Field.getText().trim();
            String line2    = line2Field.getText().trim();
            String city     = cityField.getText().trim();
            String postcode = postcodeField.getText().trim();
            String cardInput   = cardField.getText().trim();
            String expiryInput = expiryField.getText().trim();
            String guestEmail = member.isGuest() && guestEmailInput != null
                    ? guestEmailInput.getText().trim()
                    : null;

            if (member.isGuest() && (guestEmail == null || guestEmail.isBlank() || !guestEmail.contains("@"))) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Please enter a valid email address.");
                return;
            }
            if (line1.isBlank() || city.isBlank() || postcode.isBlank()) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Please fill in your delivery address.");
                return;
            }
            if (cardInput.isBlank() || expiryInput.isBlank()) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Please fill in all payment fields.");
                return;
            }

            String cardDigits = cardInput.replaceAll("\\s", "");
            long cardNumber;
            try {
                cardNumber = Long.parseLong(cardDigits);
            } catch (NumberFormatException ex) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Card number must contain digits only.");
                return;
            }

            String deliveryAddress = line1 + (line2.isBlank() ? "" : ", " + line2) + ", " + city + ", " + postcode;
            final String checkoutGuestEmail = guestEmail;

            placeOrderBtn.setDisable(true);
            placeOrderBtn.setText("Processing...");
            statusLabel.setText("");

            new Thread(() -> {
                OrderService.CheckoutOutcome outcome =
                        member.isGuest()
                                ? orderService.checkout(member, cardNumber, expiryInput, deliveryAddress, checkoutGuestEmail)
                                : orderService.checkout(member, cardNumber, expiryInput, deliveryAddress);
                javafx.application.Platform.runLater(() -> {
                    switch (outcome.result()) {
                        case SUCCESS -> {
                            stage.getScene().setRoot(new OrderConfirmationScreen(stage, member, outcome));
                            stage.setTitle("IPOS-PU | Order Confirmed");
                        }
                        case PAYMENT_FAILED, STOCK_UNAVAILABLE, CART_EMPTY -> {
                            statusLabel.setStyle(AppStyles.errorStyle());
                            statusLabel.setText(outcome.message());
                            placeOrderBtn.setDisable(false);
                            placeOrderBtn.setText("Place Order");
                        }
                    }
                });
            }).start();
        });

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }
}
