package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.controller.PromotionsController;
import com.novasolutions.ipospu.db.ProductDAO;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.Product;
import com.novasolutions.ipospu.model.PromotionCampaign;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CampaignAdminScreen extends BorderPane {

    private final PromotionsController promotionsController = new PromotionsController();
    private final ProductDAO productDAO = new ProductDAO();
    private final List<Product> caProducts = productDAO.getAllProducts();
    private final Map<String, Product> productByCode = new HashMap<>();
    private final Map<Integer, Product> productById = new HashMap<>();
    private PromotionCampaign selectedCampaign;

    public CampaignAdminScreen(Stage stage, Member member) {
        caProducts.forEach(product -> {
            productByCode.put(product.getCode(), product);
            productById.put(product.getId(), product);
        });
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "admin"));
        if (member == null || !"ADMIN".equals(member.memberType())) {
            setCenter(buildAccessDenied());
            return;
        }
        setCenter(buildContent());
    }

    private ScrollPane buildAccessDenied() {
        Label title = new Label("Access denied");
        title.setStyle(AppStyles.headline());

        Label sub = new Label("Only admin accounts can create, edit, or deactivate promotion campaigns.");
        sub.setStyle(AppStyles.bodyMuted());
        sub.setWrapText(true);

        VBox box = new VBox(12, title, sub);
        box.setPadding(new Insets(32));
        box.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private ScrollPane buildContent() {
        Label title = new Label("Campaign Administration");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label sub = new Label("Create, edit, and deactivate promotion campaigns.");
        sub.setStyle(AppStyles.bodyMuted());

        TextField nameField = new TextField();
        nameField.setPromptText("Campaign name");
        nameField.setStyle(AppStyles.inputField());

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(7));

        TextField productsField = new TextField();
        productsField.setPromptText("e.g. 10000001:5, 10000002:10");
        productsField.setStyle(AppStyles.inputField());

        Label productsHint = new Label("Format: Code:Discount%, comma separated (e.g. 10000001:5, 10000002:10). Use the material code shown in the catalogue.");
        productsHint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
        productsHint.setWrapText(true);

        ListView<Product> stockItemsList = new ListView<>();
        stockItemsList.setPlaceholder(new Label("No CA stock items found."));
        stockItemsList.setPrefHeight(280);
        stockItemsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(String.format(
                        "%s | %s | %d in stock | %s @ \u00a3%.2f",
                        item.getCode(),
                        item.getName(),
                        item.getStockQuantity(),
                        item.getPackageType(),
                        item.getPrice()
                ));
            }
        });
        stockItemsList.getItems().setAll(caProducts);

        Label stockHint = new Label("Available CA stock items. Use the material code on the left in the promotion field.");
        stockHint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
        stockHint.setWrapText(true);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button createBtn = new Button("Create Campaign");
        createBtn.setStyle(AppStyles.ghostGradBtn());

        Button updateBtn = new Button("Update Selected");
        updateBtn.setStyle(AppStyles.secondaryBtn());

        Button deactivateBtn = new Button("Terminate Early");
        deactivateBtn.setStyle("-fx-background-color: " + AppStyles.ERROR_CONT + "; -fx-text-fill: " + AppStyles.ON_ERROR_CONT + ";");

        Button deleteBtn = new Button("Delete Campaign");
        deleteBtn.setStyle("-fx-background-color: #7f0000; -fx-text-fill: white;");

        // Conflict resolution — shown only when a conflict is detected
        Button resolveBtn = new Button("Remove conflicting products and proceed");
        resolveBtn.setStyle("-fx-background-color: #e65100; -fx-text-fill: white; -fx-background-radius: 6;");
        resolveBtn.setVisible(false);
        resolveBtn.setManaged(false);

        // Clear resolveBtn whenever user edits the products field
        productsField.textProperty().addListener((obs, old, nv) -> {
            resolveBtn.setVisible(false);
            resolveBtn.setManaged(false);
            resolveBtn.setUserData(null);
        });

        VBox form = new VBox(12,
                fieldBlock("Campaign Name", nameField),
                fieldBlock("Start Date", startPicker),
                fieldBlock("End Date", endPicker),
                fieldBlock("Products & Discounts", new VBox(4, productsField, productsHint)),
                fieldBlock("CA Stock Items", new VBox(6, stockItemsList, stockHint)),
                statusLabel,
                resolveBtn,
                new HBox(10, createBtn, updateBtn, deactivateBtn, deleteBtn)
        );
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        form.setEffect(AppStyles.subtleShadow());
        HBox.setHgrow(form, Priority.ALWAYS);

        ListView<PromotionCampaign> campaignsList = new ListView<>();
        campaignsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(PromotionCampaign item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " [" + item.status() + "]");
            }
        });
        refreshCampaigns(campaignsList);

        campaignsList.getSelectionModel().selectedItemProperty().addListener((obs, old, campaign) -> {
            selectedCampaign = campaign;
            if (campaign != null) {
                nameField.setText(campaign.name());
                startPicker.setValue(campaign.startDate());
                endPicker.setValue(campaign.endDate());
                // Populate productsField as "id:discount, id:discount" pairs
                String productStr = campaign.products().stream()
                        .map(p -> toDisplayCode(p.productId()) + ":" + trimTrailingZeros(p.discountPercent()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                productsField.setText(productStr);
                statusLabel.setText("");
            }
        });

        createBtn.setOnAction(e -> {
            resolveBtn.setVisible(false);
            resolveBtn.setManaged(false);
            try {
                promotionsController.createCampaign(
                        nameField.getText().trim(),
                        startPicker.getValue(),
                        endPicker.getValue(),
                        parseProductDiscounts(productsField.getText())
                );
                statusLabel.setStyle(AppStyles.successStyle());
                statusLabel.setText("Campaign created successfully.");
                refreshCampaigns(campaignsList);
            } catch (Exception ex) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText(ex.getMessage());
                if (ex.getMessage() != null && ex.getMessage().startsWith("Promotion conflict")) {
                    showConflictResolution(resolveBtn, statusLabel, productsField,
                            nameField, startPicker, endPicker, campaignsList, null, ex.getMessage());
                }
            }
        });

        updateBtn.setOnAction(e -> {
            resolveBtn.setVisible(false);
            resolveBtn.setManaged(false);
            if (selectedCampaign == null) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Select a campaign to update.");
                return;
            }
            try {
                promotionsController.updateCampaign(
                        selectedCampaign.id(),
                        nameField.getText().trim(),
                        startPicker.getValue(),
                        endPicker.getValue(),
                        parseProductDiscounts(productsField.getText())
                );
                statusLabel.setStyle(AppStyles.successStyle());
                statusLabel.setText("Campaign updated successfully.");
                refreshCampaigns(campaignsList);
            } catch (Exception ex) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText(ex.getMessage());
                if (ex.getMessage() != null && ex.getMessage().startsWith("Promotion conflict")) {
                    showConflictResolution(resolveBtn, statusLabel, productsField,
                            nameField, startPicker, endPicker, campaignsList, selectedCampaign, ex.getMessage());
                }
            }
        });

        deactivateBtn.setOnAction(e -> {
            if (selectedCampaign == null) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Select a campaign to terminate.");
                return;
            }
            try {
                promotionsController.deactivateCampaign(selectedCampaign.id());
                statusLabel.setStyle(AppStyles.successStyle());
                statusLabel.setText("Campaign terminated early (status set to INACTIVE).");
                refreshCampaigns(campaignsList);
            } catch (Exception ex) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText(ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            if (selectedCampaign == null) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText("Select a campaign to delete.");
                return;
            }
            try {
                promotionsController.deleteCampaign(selectedCampaign.id());
                statusLabel.setStyle(AppStyles.successStyle());
                statusLabel.setText("Campaign deleted permanently.");
                selectedCampaign = null;
                nameField.clear();
                productsField.clear();
                refreshCampaigns(campaignsList);
            } catch (Exception ex) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText(ex.getMessage());
            }
        });

        VBox listCard = new VBox(12,
                new Label("Campaigns"),
                campaignsList
        );
        listCard.setPadding(new Insets(24));
        listCard.setMinWidth(300);
        listCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        listCard.setEffect(AppStyles.subtleShadow());

        HBox content = new HBox(18, form, listCard);
        content.setAlignment(Pos.TOP_LEFT);

        VBox page = new VBox(20, title, sub, content);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private VBox fieldBlock(String labelText, javafx.scene.Node input) {
        Label label = new Label(labelText);
        label.setStyle(AppStyles.fieldLabel());
        VBox box = new VBox(6, label, input);
        return box;
    }

    /**
     * Parses the conflict error message to extract conflicting product names,
     * matches them against current productsField entries, removes them, and
     * shows the resolveBtn so the user can proceed without those products.
     */
    private void showConflictResolution(Button resolveBtn, Label statusLabel, TextField productsField,
                                        TextField nameField, DatePicker startPicker, DatePicker endPicker,
                                        ListView<PromotionCampaign> campaignsList,
                                        PromotionCampaign campaignToUpdate, String errorMessage) {
        // Parse conflicting product names from message: "Promotion conflict with 'X' for: A, B, C"
        int forIdx = errorMessage.indexOf("for: ");
        if (forIdx < 0) return;
        String conflictPart = errorMessage.substring(forIdx + 5);
        java.util.Set<String> conflictNames = new java.util.HashSet<>();
        for (String part : conflictPart.split(",")) {
            conflictNames.add(part.trim().toLowerCase());
        }

        resolveBtn.setVisible(true);
        resolveBtn.setManaged(true);
        resolveBtn.setOnAction(ev -> {
            // We can't easily match by name without a DB call, so instead fetch
            // the product names for the current IDs by looking them up in the
            // existing campaigns list (they are already loaded with names).
            // Simpler approach: remove from productsField any ID whose name appears in conflictNames.
            // We do this by checking against all known campaign products.
            java.util.Map<Integer, String> productNames = new java.util.HashMap<>();
            promotionsController.getAllCampaigns().stream()
                    .flatMap(c -> c.products().stream())
                    .forEach(p -> productNames.putIfAbsent(p.productId(), p.productName() != null ? p.productName().toLowerCase() : ""));

            try {
                Map<Integer, Double> current = parseProductDiscounts(productsField.getText());
                Map<Integer, Double> filtered = new LinkedHashMap<>();
                for (Map.Entry<Integer, Double> entry : current.entrySet()) {
                    String pName = productNames.getOrDefault(entry.getKey(), "").toLowerCase();
                    boolean conflicts = conflictNames.stream().anyMatch(cn -> pName.contains(cn) || cn.contains(pName));
                    if (!conflicts) {
                        filtered.put(entry.getKey(), entry.getValue());
                    }
                }

                // Update the productsField with filtered entries
                String newText = filtered.entrySet().stream()
                        .map(en -> toDisplayCode(en.getKey()) + ":" + trimTrailingZeros(en.getValue()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                productsField.setText(newText);

                // Retry
                if (campaignToUpdate == null) {
                    promotionsController.createCampaign(nameField.getText().trim(),
                            startPicker.getValue(), endPicker.getValue(), filtered);
                    statusLabel.setStyle(AppStyles.successStyle());
                    statusLabel.setText("Campaign created (conflicting products removed).");
                } else {
                    promotionsController.updateCampaign(campaignToUpdate.id(), nameField.getText().trim(),
                            startPicker.getValue(), endPicker.getValue(), filtered);
                    statusLabel.setStyle(AppStyles.successStyle());
                    statusLabel.setText("Campaign updated (conflicting products removed).");
                }
                resolveBtn.setVisible(false);
                resolveBtn.setManaged(false);
                refreshCampaigns(campaignsList);
            } catch (Exception ex2) {
                statusLabel.setStyle(AppStyles.errorStyle());
                statusLabel.setText(ex2.getMessage());
            }
        });
    }

    private void refreshCampaigns(ListView<PromotionCampaign> campaignsList) {
        campaignsList.getItems().setAll(promotionsController.getAllCampaigns());
    }

    /**
     * Parses "code:discount%, code:discount%" format into a Map<stockItemId, Double>.
     * Example: "10000001:5, 10000002:10" → {1→5.0, 2→10.0}
     */
    private Map<Integer, Double> parseProductDiscounts(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("At least one stock item is required (format: Code:Discount%, e.g. 10000001:5, 10000002:10)");
        }
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (String token : raw.split(",")) {
            token = token.trim();
            if (token.isBlank()) continue;
            String[] parts = token.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format '" + token + "' — use Code:Discount% (e.g. 10000001:5)");
            }
            String productCode = parts[0].trim();
            double discount;
            try {
                discount = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid discount in '" + token + "' — use Code:Discount% (e.g. 10000001:5)");
            }
            Product product = productByCode.get(productCode);
            if (product == null) {
                throw new IllegalArgumentException("Unknown material code '" + productCode + "'. Use a code from the CA stock catalogue.");
            }
            result.put(product.getId(), discount);
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("At least one stock item is required");
        }
        return result;
    }

    private String toDisplayCode(int productId) {
        Product product = productById.get(productId);
        return product != null ? product.getCode() : String.valueOf(productId);
    }

    private String trimTrailingZeros(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.valueOf(value);
    }
}
