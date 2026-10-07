package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MemberProfileScreen extends BorderPane {

    public MemberProfileScreen(Stage stage, Member member) {
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "profile"));
        setCenter(buildContent(stage, member));
    }

    private ScrollPane buildContent(Stage stage, Member member) {

        // ── Page header ───────────────────────────────────────────────────
        Label pageTitle = new Label("My Profile");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label(member.isGuest()
                ? "Guest sessions are temporary and support cart and checkout during this app session."
                : "Your membership details and account information.");
        pageSub.setStyle(AppStyles.bodyMuted());

        VBox pageHeader = new VBox(6, pageTitle, pageSub);

        // ── Profile card ──────────────────────────────────────────────────
        Label sectionLabel = new Label("Account Information");
        sectionLabel.setStyle(AppStyles.sectionTitle());

        VBox detailsCard = new VBox(0);
        detailsCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + ";" +
                             "-fx-background-radius: 12; -fx-padding: 28;");
        detailsCard.setEffect(AppStyles.subtleShadow());

        detailsCard.getChildren().addAll(
                profileRow("Full Name",         member.fullName()),
                profileRow("Email Address",     member.email()),
                profileRow("Member Type",       formatMemberType(member.memberType())),
                profileRow("Membership Status", member.membershipStatus()),
                profileRow("Orders Placed",     String.valueOf(member.orderCount())),
                profileRow("Member Since",      member.createdAt() != null
                        ? member.createdAt().toLocalDate().toString() : "—")
        );

        if (member.companyName() != null && !member.companyName().isBlank()) {
            detailsCard.getChildren().add(2, profileRow("Company", member.companyName()));
        }

        // ── Status badge ──────────────────────────────────────────────────
        Label statusBadge = buildStatusBadge(member.membershipStatus());

        HBox statusRow = new HBox(12,
                new Label("Current Status:") {{
                    setStyle(AppStyles.bodyMuted());
                }},
                statusBadge
        );
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(0, 0, 4, 0));

        // ── Actions card ──────────────────────────────────────────────────
        Label actionsLabel = new Label(member.isGuest() ? "Next Step" : "Account Actions");
        actionsLabel.setStyle(AppStyles.sectionTitle());

        Button changePassBtn = new Button(member.isGuest() ? "Create an Account" : "Change Password");
        changePassBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 11 24;");
        changePassBtn.setOnMouseEntered(e -> changePassBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 11 24; -fx-opacity: 0.9;"));
        changePassBtn.setOnMouseExited(e -> changePassBtn.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 11 24;"));
        changePassBtn.setOnAction(e -> {
            if (member.isGuest()) {
                stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
                stage.setTitle("IPOS-PU | Registration");
            } else {
                stage.getScene().setRoot(new ChangePasswordScreen(stage, member));
                stage.setTitle("IPOS-PU | Update Password");
            }
        });

        Label changePassDesc = new Label(member.isGuest()
                ? "Register or sign in with a member account if you want saved order history, account management, and loyalty rewards."
                : "Update your login credentials. You will be redirected to the password reset screen.");
        changePassDesc.setStyle(AppStyles.bodyMuted());
        changePassDesc.setWrapText(true);

        VBox actionsCard = new VBox(12, changePassDesc, changePassBtn);
        actionsCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + ";" +
                             "-fx-background-radius: 12; -fx-padding: 28;");
        actionsCard.setEffect(AppStyles.subtleShadow());

        // ── Page assembly ─────────────────────────────────────────────────
        VBox page = new VBox(28,
                pageHeader,
                statusRow,
                new VBox(14, sectionLabel, detailsCard),
                new VBox(14, actionsLabel, actionsCard)
        );
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    private HBox profileRow(String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;" +
                          "-fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
        keyLabel.setMinWidth(180);

        Label valueLabel = new Label(value != null && !value.isBlank() ? value : "—");
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        HBox row = new HBox(16, keyLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setStyle("-fx-border-color: transparent transparent " + AppStyles.SURFACE_LOW +
                     " transparent; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private Label buildStatusBadge(String status) {
        String bg, fg;
        switch (status) {
            case "APPROVED"  -> { bg = "#d4edda"; fg = AppStyles.SURFACE_TINT; }
            case "PENDING"   -> { bg = AppStyles.TERT_FIXED; fg = AppStyles.ON_TERT_VAR; }
            case "REJECTED"  -> { bg = AppStyles.ERROR_CONT; fg = AppStyles.ON_ERROR_CONT; }
            case "GUEST"     -> { bg = AppStyles.SURFACE_CONTAINER; fg = AppStyles.ON_SURFACE_VAR; }
            default          -> { bg = AppStyles.SURFACE_HIGH; fg = AppStyles.ON_SURFACE_VAR; }
        }
        Label badge = new Label(status);
        badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;" +
                       "-fx-background-color: " + bg + ";" +
                       "-fx-text-fill: " + fg + ";" +
                       "-fx-background-radius: 20; -fx-padding: 4 12;");
        return badge;
    }

    private String formatMemberType(String type) {
        return switch (type) {
            case "COMMERCIAL" -> "Commercial";
            case "GUEST" -> "Guest";
            default -> "Non-Commercial";
        };
    }
}
