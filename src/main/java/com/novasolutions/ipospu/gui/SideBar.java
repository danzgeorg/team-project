package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.service.PromotionService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Shared navy sidebar for authenticated screens.
 * Pass one of "dashboard", "catalogue", "promotions", "admin", "orders", "profile" as activeItem.
 */
public class SideBar extends VBox {

    public SideBar(Stage stage, Member member, String activeItem) {
        PromotionService promotionService = new PromotionService();

        // ── Brand section ──────────────────────────────────────────────────
        Label title = new Label("Architectural Ledger");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("INTERNAL PROCUREMENT");
        subtitle.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.4);");

        VBox brand = new VBox(3, title, subtitle);
        brand.setPadding(new Insets(0, 0, 24, 4));

        // ── Nav items ─────────────────────────────────────────────────────
        VBox navCatalogue = navItem("View Catalogue", "catalogue".equals(activeItem));
        VBox navPromotions = promotionService.hasActiveCampaigns()
                ? navItem("Promotions", "promotions".equals(activeItem))
                : null;
        VBox navAdmin      = member != null && "ADMIN".equals(member.memberType())
                ? navItem("Campaign Admin", "admin".equals(activeItem))
                : null;
        VBox navReports    = member != null && "ADMIN".equals(member.memberType())
                ? navItem("Reports", "reports".equals(activeItem))
                : null;
        VBox navOrderAdmin = member != null && "ADMIN".equals(member.memberType())
                ? navItem("Order Admin", "order-admin".equals(activeItem))
                : null;
        VBox navCart      = member != null ? navItem("My Cart", "cart".equals(activeItem)) : null;
        VBox navOrders    = member != null && !member.isGuest() ? navItem("My Orders", "orders".equals(activeItem)) : null;
        VBox navProfile   = navItem("My Profile",     "profile".equals(activeItem));

        navCatalogue.setOnMouseClicked(e -> {
            stage.getScene().setRoot(new CatalogueScreen(stage, member));
            stage.setTitle("IPOS-PU | Catalogue");
        });

        if (navPromotions != null) {
            navPromotions.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new PromotionsScreen(stage, member));
                stage.setTitle("IPOS-PU | Promotions");
            });
        }

        if (navAdmin != null) {
            navAdmin.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new CampaignAdminScreen(stage, member));
                stage.setTitle("IPOS-PU | Campaign Admin");
            });
        }

        if (navReports != null) {
            navReports.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new ReportsScreen(stage, member));
                stage.setTitle("IPOS-PU | Reports");
            });
        }

        if (navOrderAdmin != null) {
            navOrderAdmin.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new AdminOrdersScreen(stage, member));
                stage.setTitle("IPOS-PU | Order Management");
            });
        }

        if (navCart != null) {
            navCart.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new CartScreen(stage, member));
                stage.setTitle("IPOS-PU | My Cart");
            });
        }

        if (navOrders != null) {
            navOrders.setOnMouseClicked(e -> {
                stage.getScene().setRoot(new OrderHistoryScreen(stage, member));
                stage.setTitle("IPOS-PU | My Orders");
            });
        }

        navProfile.setOnMouseClicked(e -> {
            stage.getScene().setRoot(new MemberProfileScreen(stage, member));
            stage.setTitle("IPOS-PU | My Profile");
        });

        VBox nav = new VBox(4);
        nav.getChildren().add(navCatalogue);
        if (navPromotions != null) nav.getChildren().add(navPromotions);
        if (navAdmin != null) nav.getChildren().add(navAdmin);
        if (navReports != null) nav.getChildren().add(navReports);
        if (navOrderAdmin != null) nav.getChildren().add(navOrderAdmin);
        if (navCart != null) nav.getChildren().add(navCart);
        if (navOrders != null) nav.getChildren().add(navOrders);
        nav.getChildren().add(navProfile);

        // ── Spacer ────────────────────────────────────────────────────────
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ── User card ─────────────────────────────────────────────────────
        Label nameLabel = new Label(member != null ? member.fullName() : "");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
        nameLabel.setWrapText(true);

        String roleStr = member != null
                ? switch (member.memberType()) {
                    case "ADMIN" -> "System Administrator";
                    case "COMMERCIAL" -> "Commercial Member";
                    case "GUEST" -> "Guest Access";
                    default -> "Non-Commercial Member";
                }
                : "";
        Label roleLabel = new Label(roleStr);
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.45);");

        VBox userCard = new VBox(4, nameLabel, roleLabel);
        userCard.setStyle("-fx-background-color: rgba(255,255,255,0.06);" +
                          "-fx-background-radius: 10; -fx-padding: 14;");

        // ── Assembly ──────────────────────────────────────────────────────
        setPrefWidth(240);
        setMinWidth(240);
        setMaxWidth(240);
        setPadding(new Insets(24, 12, 16, 12));
        setSpacing(0);
        setStyle("-fx-background-color: " + AppStyles.NAVY + ";");

        getChildren().addAll(brand, nav, spacer, userCard);
    }

    private VBox navItem(String text, boolean active) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px;" +
                       "-fx-font-weight: " + (active ? "bold" : "normal") + ";" +
                       "-fx-text-fill: " + (active ? "white" : "rgba(255,255,255,0.55)") + ";");

        String activeStyle   = "-fx-background-color: " + AppStyles.PRIMARY + "; -fx-background-radius: 6; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;";
        String hoverStyle    = "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 6; -fx-cursor: hand;";

        VBox box = new VBox(label);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle(active ? activeStyle : inactiveStyle);

        if (!active) {
            box.setOnMouseEntered(e -> box.setStyle(hoverStyle));
            box.setOnMouseExited(e -> box.setStyle(inactiveStyle));
        }

        return box;
    }
}
