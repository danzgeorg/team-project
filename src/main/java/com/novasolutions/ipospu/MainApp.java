package com.novasolutions.ipospu;

import com.novasolutions.ipospu.gui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        LoginScreen root = new LoginScreen();

        Scene scene = new Scene(root, 900, 700);

        // Windows taskbar icon (stage.getIcons() works on Windows/Linux)
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);
        primaryStage.setTitle("IPOS-PU | Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        // Set dock/taskbar icon before JavaFX launches (AWT Taskbar API)
        try {
            if (java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                    java.awt.Image icon = java.awt.Toolkit.getDefaultToolkit()
                            .getImage(MainApp.class.getResource("/icon.png"));
                    taskbar.setIconImage(icon);
                }
            }
        } catch (Exception ignored) {}
        launch(args);
    }

}