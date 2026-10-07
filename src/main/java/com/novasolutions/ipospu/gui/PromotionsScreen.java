package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.controller.PromotionsController;
import com.novasolutions.ipospu.model.CampaignProduct;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.PromotionCampaign;
import com.novasolutions.ipospu.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PromotionsScreen extends BorderPane {

    private final PromotionsController promotionsController = new PromotionsController();
    private final CartService cartService = new CartService();
    private final Member member;
    private final Stage stage;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public PromotionsScreen(Stage stage, Member member) {
        this.stage = stage;
        this.member = member;
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "promotions"));
        setCenter(buildContent());
    }

    private ScrollPane buildContent() {
        Label title = new Label("Active Promotions");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label sub = new Label("Select a campaign to view its products and discounts.");
        sub.setStyle(AppStyles.bodyMuted());

        List<PromotionCampaign> active = promotionsController.getActivePromotions();

        if (active.isEmpty()) {
            VBox page = new VBox(20, title, sub, buildEmptyState());
            page.setPadding(new Insets(32));
            page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");
            ScrollPane scroll = new ScrollPane(page);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
            return scroll;
        }

        ListView<PromotionCampaign> campaignList = new ListView<>();
        campaignList.getItems().setAll(active);
        campaignList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(PromotionCampaign item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " [" + item.status() + "]");
            }
        });

        VBox detailsPane = new VBox(14);
        detailsPane.setPadding(new Insets(20));
        detailsPane.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        detailsPane.setEffect(AppStyles.subtleShadow());

        campaignList.getSelectionModel().selectedItemProperty().addListener((obs, old, campaign) -> {
            if (campaign != null) {
                promotionsController.recordCampaignView(campaign.id());
                detailsPane.getChildren().setAll(buildCampaignDetails(campaign));
            }
        });

        campaignList.getSelectionModel().selectFirst();

        VBox listCard = new VBox(12, new Label("Campaigns"), campaignList);
        listCard.setPadding(new Insets(20));
        listCard.setMinWidth(280);
        listCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        listCard.setEffect(AppStyles.subtleShadow());

        HBox content = new HBox(18, listCard, detailsPane);
        HBox.setHgrow(detailsPane, Priority.ALWAYS);
        content.setAlignment(Pos.TOP_LEFT);

        VBox page = new VBox(20, title, sub, content);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private List<VBox> buildCampaignDetails(PromotionCampaign campaign) {
        Label header = new Label(campaign.name());
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label dates = new Label(campaign.startDate().format(DATE_FORMAT) + " to " + campaign.endDate().format(DATE_FORMAT));
        dates.setStyle(AppStyles.bodyMuted());

        Label discount = new Label(String.format("%s%% off selected products", trimTrailingZeros(campaign.discountPercent())));
        discount.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";");

        VBox summary = new VBox(6, header, dates, discount);

        VBox productsBlock = new VBox(10);
        productsBlock.getChildren().add(new Label("Campaign Products"));

        if (campaign.products().isEmpty()) {
            productsBlock.getChildren().add(buildEmptyState());
        } else {
            for (CampaignProduct product : campaign.products()) {
                productsBlock.getChildren().add(buildProductRow(product));
            }
        }

        return List.of(summary, productsBlock);
    }

    private VBox buildProductRow(CampaignProduct product) {
        Label name = new Label(product.productName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label info = new Label(String.format("Discount: %s%%", trimTrailingZeros(product.discountPercent())));
        info.setStyle(AppStyles.bodyMuted());

        Button addBtn = new Button("Add to Cart");
        addBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 8 18;");
        addBtn.setOnAction(e -> {
            try {
                CartService.AddResult result = cartService.addToCart(member, product.productId(), 1);
                switch (result) {
                    case SUCCESS -> {
                        addBtn.setText("Added");
                        addBtn.setDisable(true);
                    }
                    case OUT_OF_STOCK -> addBtn.setText("Out of Stock");
                    case INSUFFICIENT_STOCK -> addBtn.setText("Limited Stock");
                }
            } catch (Exception ex) {
                addBtn.setText("Failed");
                addBtn.setDisable(true);
            }
        });

        VBox left = new VBox(4, name, info);
        HBox row = new HBox(12, left, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 10; -fx-border-color: " + AppStyles.SURFACE_HIGH + "; -fx-border-radius: 10;");

        VBox wrapper = new VBox(row);
        return wrapper;
    }

    private VBox buildEmptyState() {
        Label title = new Label("No active promotions right now");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label sub = new Label("Check back later for live campaign offers.");
        sub.setStyle(AppStyles.bodyMuted());

        VBox box = new VBox(8, title, sub);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        return box;
    }

    private String trimTrailingZeros(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%s", value);
    }
}
