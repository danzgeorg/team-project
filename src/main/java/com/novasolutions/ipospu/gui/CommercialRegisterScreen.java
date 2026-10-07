package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.controller.CommercialRegistrationController;
import com.novasolutions.ipospu.service.CommercialApplicationResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CommercialRegisterScreen extends BorderPane {

    private final CommercialRegistrationController controller = new CommercialRegistrationController();

    public CommercialRegisterScreen(Stage stage) {

        // ── Minimal top bar ───────────────────────────────────────────────
        setTop(buildMinimalTopBar(stage));

        // ── Scrollable main area ──────────────────────────────────────────
        VBox page = buildPage(stage);
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        setCenter(scroll);
    }

    private VBox buildPage(Stage stage) {
        // ── Page header ───────────────────────────────────────────────────
        Label title = new Label("Commercial Registration");
        title.setStyle(AppStyles.headline());

        Label subtitle = new Label("Onboard your business to the IPOS-PU procurement network.");
        subtitle.setStyle(AppStyles.bodyMuted());

        VBox pageHeader = new VBox(6, title, subtitle);

        // ── Info panel (left) ─────────────────────────────────────────────
        Label reqTitle = new Label("Registration Requirements");
        reqTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.PRIMARY + ";");

        String[] requirements = {
                "Verified Companies House information for UK-based entities.",
                "Director identification for KYC compliance checks.",
                "Valid business address for physical procurement deliveries."
        };

        VBox reqList = new VBox(12);
        for (String req : requirements) {
            Label item = new Label("✓  " + req);
            item.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
            item.setWrapText(true);
            reqList.getChildren().add(item);
        }

        VBox infoPanel = new VBox(16, reqTitle, reqList);
        infoPanel.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOW + "; -fx-background-radius: 12; -fx-padding: 28;");
        infoPanel.setMinWidth(240);
        infoPanel.setMaxWidth(280);

        // ── Form card (right) ─────────────────────────────────────────────
        Label formTitle = new Label("Application Details");
        formTitle.setStyle(AppStyles.sectionTitle());

        // Fields
        TextField applicantNameField = styledField("APPLICANT NAME", "John Doe");
        TextField companyNameField   = styledField("COMPANY NAME",   "Ledger Architecture Ltd");
        TextField companiesHouseField= styledField("COMPANIES HOUSE NUMBER", "8-digit number");
        TextField businessTypeField  = styledField("BUSINESS TYPE", "Ltd, PLC, Partnership…");
        TextField directorNamesField = styledField("DIRECTOR NAMES", "Full names separated by commas");
        TextField businessAddressField= styledField("BUSINESS ADDRESS", "Full registered office address");
        TextField emailField         = styledField("BUSINESS EMAIL", "procurement@company.com");

        // Two-column grid for first 4 fields
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(16);

        javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, cc);

        VBox g1 = fieldGroup("APPLICANT NAME",          applicantNameField);
        VBox g2 = fieldGroup("COMPANY NAME",            companyNameField);
        VBox g3 = fieldGroup("COMPANIES HOUSE NUMBER",  companiesHouseField);
        VBox g4 = fieldGroup("BUSINESS TYPE",           businessTypeField);

        grid.add(g1, 0, 0); grid.add(g2, 1, 0);
        grid.add(g3, 0, 1); grid.add(g4, 1, 1);

        GridPane.setHgrow(g1, Priority.ALWAYS);
        GridPane.setHgrow(g2, Priority.ALWAYS);
        GridPane.setHgrow(g3, Priority.ALWAYS);
        GridPane.setHgrow(g4, Priority.ALWAYS);

        // Full-width fields below
        VBox directorGroup = new VBox(6,
                labelFor("DIRECTOR NAMES"), directorNamesField);
        VBox addressGroup = new VBox(6,
                labelFor("BUSINESS ADDRESS"), businessAddressField);
        VBox emailGroup = new VBox(6,
                labelFor("BUSINESS EMAIL"), emailField);

        // Status message
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        // Submit button
        Button submitBtn = new Button("Submit Registration →");
        submitBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 12 28;");
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 12 28; -fx-opacity: 0.9;"));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 12 28;"));

        HBox actionBar = new HBox(submitBtn);
        actionBar.setAlignment(Pos.CENTER_RIGHT);

        VBox formCard = new VBox(20,
                formTitle, grid,
                directorGroup, addressGroup, emailGroup,
                messageLabel, actionBar
        );
        formCard.setStyle(AppStyles.card());
        formCard.setEffect(AppStyles.cardShadow());
        VBox.setVgrow(formCard, Priority.ALWAYS);

        // ── Two-column layout ─────────────────────────────────────────────
        HBox twoCol = new HBox(24, infoPanel, formCard);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        // ── Back link ─────────────────────────────────────────────────────
        Button backLink = new Button("← Back");
        backLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";" +
                          "-fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: transparent;");
        backLink.setOnAction(e -> {
            stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
            stage.setTitle("IPOS-PU | Registration");
        });

        VBox page = new VBox(28, pageHeader, twoCol, backLink);
        page.setPadding(new Insets(36));

        // ── Submit handler ────────────────────────────────────────────────
        submitBtn.setOnAction(e -> {
            CommercialApplicationResult result = controller.submitCommercialApplication(
                    applicantNameField.getText(),
                    companyNameField.getText(),
                    companiesHouseField.getText(),
                    directorNamesField.getText(),
                    businessTypeField.getText(),
                    businessAddressField.getText(),
                    emailField.getText()
            );

            if (result.isSuccess()) {
                messageLabel.setStyle(AppStyles.successStyle());
                messageLabel.setText(result.getMessage());
                submitBtn.setDisable(true);
                applicantNameField.clear(); companyNameField.clear();
                companiesHouseField.clear(); directorNamesField.clear();
                businessTypeField.clear(); businessAddressField.clear();
                emailField.clear();
            } else {
                messageLabel.setStyle(AppStyles.errorStyle());
                messageLabel.setText(result.getMessage());
            }
        });

        return page;
    }

    private TextField styledField(String placeholder, String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(AppStyles.inputField());
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private Label labelFor(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(AppStyles.fieldLabel());
        return lbl;
    }

    private VBox fieldGroup(String labelText, TextField field) {
        VBox group = new VBox(6, labelFor(labelText), field);
        HBox.setHgrow(group, Priority.ALWAYS);
        GridPane.setHgrow(group, Priority.ALWAYS);
        return group;
    }

    private HBox buildMinimalTopBar(Stage stage) {
        Label logo = new Label("IPOS-PU");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("← Cancel");
        cancelBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white;" +
                           "-fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 7 14; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
            stage.setTitle("IPOS-PU | Registration");
        });

        HBox bar = new HBox(logo, spacer, cancelBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 0, 24));
        bar.setPrefHeight(64);
        bar.setMinHeight(64);
        bar.setMaxHeight(64);
        bar.setStyle("-fx-background-color: " + AppStyles.NAVY + ";");
        return bar;
    }
}
