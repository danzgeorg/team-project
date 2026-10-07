package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.controller.LoginController;
import com.novasolutions.ipospu.db.DatabaseResetDAO;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.service.LoginResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class LoginScreen extends StackPane {

    private final LoginController loginController = new LoginController();

    public LoginScreen() {
        setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        // ── Logo block ────────────────────────────────────────────────────
        Label logoText = new Label("IPOS-PU");
        logoText.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.NAVY + ";");

        Label logoSub = new Label("INTERNAL PROCUREMENT SYSTEM");
        logoSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;" +
                         "-fx-text-fill: " + AppStyles.ON_SURFACE_VAR + "; -fx-letter-spacing: 2px;");

        VBox logoBlock = new VBox(6, logoText, logoSub);
        logoBlock.setAlignment(Pos.CENTER);

        Button backLink = new Button("← Back");
        backLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";" +
                          "-fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 0;");

        // ── Error banner ──────────────────────────────────────────────────
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_ERROR_CONT + ";" +
                            "-fx-background-color: " + AppStyles.ERROR_CONT + ";" +
                            "-fx-background-radius: 6; -fx-padding: 10 14;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // ── Email field ───────────────────────────────────────────────────
        Label emailLabel = new Label("EMAIL ADDRESS");
        emailLabel.setStyle(AppStyles.fieldLabel());

        TextField emailField = new TextField();
        emailField.setPromptText("name@company.com");
        emailField.setStyle(AppStyles.inputField());

        VBox emailGroup = new VBox(6, emailLabel, emailField);

        // ── Password field ────────────────────────────────────────────────
        Label passLabel = new Label("PASSWORD");
        passLabel.setStyle(AppStyles.fieldLabel());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setStyle(AppStyles.inputField());

        VBox passGroup = new VBox(6, passLabel, passwordField);

        // ── Login button ──────────────────────────────────────────────────
        Button loginButton = new Button("Login →");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 13 0;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 13 0; -fx-opacity: 0.9;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
                AppStyles.ghostGradBtn() + "-fx-padding: 13 0;"));

        Button guestButton = new Button("Continue as Guest");
        guestButton.setMaxWidth(Double.MAX_VALUE);
        guestButton.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 13 0;");

        // ── Register link ─────────────────────────────────────────────────
        Label registerPrompt = new Label("Don't have an account?");
        registerPrompt.setStyle(AppStyles.bodyMuted());

        Button registerLink = new Button("Register");
        registerLink.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.PRIMARY + ";" +
                              "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;" +
                              "-fx-padding: 0; -fx-border-color: transparent;");

        HBox registerRow = new HBox(4, registerPrompt, registerLink);
        registerRow.setAlignment(Pos.CENTER);

        // ── Dev reset (small, tucked away) ────────────────────────────────
        Button resetBtn = new Button("Reset Dev DB");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(64,72,79,0.4);" +
                          "-fx-font-size: 10px; -fx-cursor: hand; -fx-border-color: transparent;");

        // ── Card ──────────────────────────────────────────────────────────
        VBox card = new VBox(20,
                errorLabel,
                buildCardHeader(),
                emailGroup,
                passGroup,
                loginButton,
                guestButton,
                registerRow,
                resetBtn
        );
        card.setStyle(AppStyles.card());
        card.setMaxWidth(420);
        card.setEffect(AppStyles.cardShadow());

        // ── Page container ────────────────────────────────────────────────
        VBox container = new VBox(20, backLink, logoBlock, card);
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(420);

        StackPane centeredWrapper = new StackPane(container);
        centeredWrapper.setAlignment(Pos.CENTER);
        centeredWrapper.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");
        centeredWrapper.setPadding(new Insets(40));

        ScrollPane scroll = new ScrollPane(centeredWrapper);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");

        setAlignment(Pos.CENTER);
        getChildren().add(scroll);

        // ── Event handlers ────────────────────────────────────────────────
        loginButton.setOnAction(e -> handleLogin(
                emailField.getText(), passwordField.getText(), errorLabel));

        passwordField.setOnAction(e -> handleLogin(
                emailField.getText(), passwordField.getText(), errorLabel));

        guestButton.setOnAction(e -> handleGuestLogin(errorLabel));

        registerLink.setOnAction(e -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
            stage.setTitle("IPOS-PU | Registration");
        });

        backLink.setOnAction(e -> {
            Stage stage = (Stage) getScene().getWindow();
            stage.getScene().setRoot(new RegistrationChoiceScreen(stage));
            stage.setTitle("IPOS-PU | Registration");
        });

        resetBtn.setOnAction(e -> {
            boolean ok = DatabaseResetDAO.resetPuTables();
            errorLabel.setStyle("-fx-font-size: 12px;" +
                    "-fx-background-radius: 6; -fx-padding: 10 14;" +
                    (ok ? "-fx-text-fill: " + AppStyles.SURFACE_TINT + "; -fx-background-color: #e8f5e9;"
                        : "-fx-text-fill: " + AppStyles.ON_ERROR_CONT + "; -fx-background-color: " + AppStyles.ERROR_CONT + ";"));
            errorLabel.setText(ok ? "Development database reset successfully." : "Failed to reset database.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });
    }

    private VBox buildCardHeader() {
        Label heading = new Label("Login");
        heading.setStyle(AppStyles.sectionTitle());

        Label sub = new Label("Please enter your credentials to continue, or browse the catalogue as a guest.");
        sub.setStyle(AppStyles.bodyMuted());
        sub.setWrapText(true);

        return new VBox(4, heading, sub);
    }

    private void handleLogin(String email, String password, Label errorLabel) {
        LoginResult result = loginController.login(email, password);
        finishLogin(result, errorLabel);
    }

    private void handleGuestLogin(Label errorLabel) {
        LoginResult result = loginController.loginAsGuest();
        finishLogin(result, errorLabel);
    }

    private void finishLogin(LoginResult result, Label errorLabel) {
        if (result.isSuccess()) {
            Stage stage = (Stage) getScene().getWindow();
            Member member = result.getMember();

            stage.setMinWidth(1320);
            stage.setMinHeight(900);
            stage.setWidth(1360);
            stage.setHeight(920);
            stage.centerOnScreen();

            if (!member.isGuest() && member.isFirstLogin()) {
                stage.getScene().setRoot(new ChangePasswordScreen(stage, member));
                stage.setTitle("IPOS-PU | Update Password");
            } else {
                stage.getScene().setRoot(new DashboardScreen(stage, member));
                stage.setTitle("IPOS-PU | Dashboard");
            }
        } else {
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_ERROR_CONT + ";" +
                                "-fx-background-color: " + AppStyles.ERROR_CONT + ";" +
                                "-fx-background-radius: 6; -fx-padding: 10 14;");
            errorLabel.setText(result.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }
}
