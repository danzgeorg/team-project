package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardScreen extends BorderPane {

    public DashboardScreen(Stage stage, Member member) {
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "dashboard"));
        setCenter(buildContent(stage, member));
    }

    private ScrollPane buildContent(Stage stage, Member member) {
        // ── Welcome section ───────────────────────────────────────────────
        String firstName = member.isGuest() ? "Guest" : member.fullName().split(" ")[0];

        Label welcomeHeading = new Label("Welcome back, " + firstName);
        welcomeHeading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label welcomeSub = new Label(member.isGuest()
                ? "Browse the live catalogue, build a temporary cart, and place an order without registering."
                : "Here is the status of your procurement activities.");
        welcomeSub.setStyle(AppStyles.bodyMuted());

        VBox welcomeSection = new VBox(6, welcomeHeading, welcomeSub);

        // ── Navigation cards ──────────────────────────────────────────────
        VBox catalogueCard = navCard(
                "View Catalogue",
                "Browse architectural materials and\nprocure items for active projects.",
                AppStyles.PRIMARY_FIXED,
                AppStyles.PRIMARY
        );
        catalogueCard.setOnMouseClicked(e -> {
            stage.getScene().setRoot(new CatalogueScreen(stage, member));
            stage.setTitle("IPOS-PU | Catalogue");
        });
        catalogueCard.setCursor(javafx.scene.Cursor.HAND);

        VBox ordersCard = null;
        if (!member.isGuest()) {
            ordersCard = navCard(
                    "My Orders",
                    "Track requisition statuses, review\npurchase history and pending items.",
                    AppStyles.TERT_FIXED,
                    AppStyles.ON_TERT_VAR
            );
            ordersCard.setCursor(javafx.scene.Cursor.HAND);
            ordersCard.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new OrdersScreen(stage, member));
                stage.setTitle("IPOS-PU | My Orders");
            });
        }

        VBox profileCard = navCard(
                "My Profile",
                "Update your credentials, review your\nmembership details and settings.",
                AppStyles.SURFACE_HIGH,
                AppStyles.PRIMARY
        );
        profileCard.setCursor(javafx.scene.Cursor.HAND);
        profileCard.setOnMouseClicked(e -> {
            stage.getScene().setRoot(new MemberProfileScreen(stage, member));
            stage.setTitle("IPOS-PU | My Profile");
        });

        HBox cardsRow = ordersCard == null
                ? new HBox(20, catalogueCard, profileCard)
                : new HBox(20, catalogueCard, ordersCard, profileCard);
        HBox.setHgrow(catalogueCard, Priority.ALWAYS);
        HBox.setHgrow(profileCard, Priority.ALWAYS);
        if (ordersCard != null) {
            HBox.setHgrow(ordersCard, Priority.ALWAYS);
        }

        // ── Ledger summary section ─────────────────────────────────────────
        Label ledgerTitle = new Label(member.isGuest() ? "Guest Access" : "Account Summary");
        ledgerTitle.setStyle(AppStyles.sectionTitle());

        Label ledgerSub = new Label(member.isGuest()
                ? "Guest sessions are browse-only."
                : "Your membership information at a glance.");
        ledgerSub.setStyle(AppStyles.bodyMuted());

        VBox ledgerHeader = new VBox(4, ledgerTitle, ledgerSub);

        VBox summaryCard = buildSummaryCard(member);

        VBox ledgerSection = new VBox(16, ledgerHeader, summaryCard);

        // ── Page assembly ─────────────────────────────────────────────────
        VBox page = new VBox(32, welcomeSection, cardsRow, ledgerSection);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private VBox navCard(String title, String description, String iconBg, String iconFg) {
        VBox iconBox = new VBox();
        iconBox.setPrefSize(48, 48);
        iconBox.setMinSize(48, 48);
        iconBox.setMaxSize(48, 48);
        iconBox.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label descLabel = new Label(description);
        descLabel.setStyle(AppStyles.bodyMuted());
        descLabel.setWrapText(true);

        Label arrowLabel = new Label("Explore →");
        arrowLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + iconFg + ";");

        VBox card = new VBox(14, iconBox, titleLabel, descLabel, arrowLabel);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        card.setEffect(AppStyles.subtleShadow());

        card.setOnMouseEntered(e -> card.setEffect(AppStyles.cardShadow()));
        card.setOnMouseExited(e -> card.setEffect(AppStyles.subtleShadow()));

        return card;
    }

    private VBox buildSummaryCard(Member member) {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12; -fx-padding: 28;");
        card.setEffect(AppStyles.subtleShadow());

        card.getChildren().addAll(
                summaryRow("Full Name",         member.fullName()),
                summaryRow("Email",             member.email()),
                summaryRow("Member Type",       member.memberType()),
                summaryRow("Membership Status", member.membershipStatus()),
                summaryRow("Orders Placed",     String.valueOf(member.orderCount()))
        );

        if (member.isGuest()) {
            card.getChildren().add(summaryRow("Available Features", "Browse catalogue, promotions, cart, and checkout"));
        }

        if (member.companyName() != null && !member.companyName().isBlank()) {
            card.getChildren().add(1, summaryRow("Company", member.companyName()));
        }

        return card;
    }

    private HBox summaryRow(String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
        keyLabel.setMinWidth(160);

        Label valueLabel = new Label(value != null ? value : "—");
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        HBox row = new HBox(16, keyLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: transparent transparent " + AppStyles.SURFACE_LOW + " transparent;" +
                     "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 12 0;");
        return row;
    }
}
