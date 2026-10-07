package com.novasolutions.ipospu.gui;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Shared design-system constants and style helpers for all screens.
 * Mirrors the "Architectural Ledger" token palette from the design spec.
 */
class AppStyles {

    // ── Surface tokens ──────────────────────────────────────────────────────
    static final String SURFACE          = "#f7f9ff";
    static final String SURFACE_LOWEST   = "#ffffff";
    static final String SURFACE_LOW      = "#edf4ff";
    static final String SURFACE_CONTAINER = "#e3efff";
    static final String SURFACE_HIGH     = "#d9eaff";
    static final String SURFACE_HIGHEST  = "#d1e4fb";

    // ── Core palette ─────────────────────────────────────────────────────────
    static final String NAVY             = "#091d2e";
    static final String PRIMARY          = "#006193";
    static final String PRIMARY_CONT     = "#207ab3";
    static final String PRIMARY_FIXED    = "#cce5ff";
    static final String ON_PRIMARY       = "#ffffff";

    // ── Text tokens ──────────────────────────────────────────────────────────
    static final String ON_SURFACE      = "#091d2e";
    static final String ON_SURFACE_VAR  = "#40484f";

    // ── Semantic tokens ──────────────────────────────────────────────────────
    static final String ERROR            = "#ba1a1a";
    static final String ERROR_CONT       = "#ffdad6";
    static final String ON_ERROR_CONT    = "#93000a";
    static final String SURFACE_TINT     = "#006497";   // success / in-stock
    static final String TERT_FIXED       = "#ffddb4";   // pending / amber
    static final String ON_TERT_VAR      = "#633f00";

    // ── Component style strings ──────────────────────────────────────────────

    static String ghostGradBtn() {
        return "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #006193, #207ab3);" +
               "-fx-text-fill: white;" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 6;" +
               "-fx-cursor: hand;";
    }

    static String secondaryBtn() {
        return "-fx-background-color: " + SURFACE_HIGH + ";" +
               "-fx-text-fill: " + PRIMARY + ";" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 6;" +
               "-fx-cursor: hand;";
    }

    static String inputField() {
        return "-fx-background-color: white;" +
               "-fx-border-color: rgba(192,199,209,0.3);" +
               "-fx-border-radius: 6;" +
               "-fx-background-radius: 6;" +
               "-fx-padding: 10 14;" +
               "-fx-font-size: 13px;" +
               "-fx-text-fill: " + ON_SURFACE + ";";
    }

    static String fieldLabel() {
        return "-fx-font-size: 10px;" +
               "-fx-font-weight: bold;" +
               "-fx-text-fill: " + ON_SURFACE_VAR + ";";
    }

    static String card() {
        return "-fx-background-color: " + SURFACE_LOWEST + ";" +
               "-fx-background-radius: 12;" +
               "-fx-padding: 40;";
    }

    static String headline() {
        return "-fx-font-size: 26px;" +
               "-fx-font-weight: bold;" +
               "-fx-text-fill: " + ON_SURFACE + ";";
    }

    static String sectionTitle() {
        return "-fx-font-size: 18px;" +
               "-fx-font-weight: bold;" +
               "-fx-text-fill: " + ON_SURFACE + ";";
    }

    static String bodyMuted() {
        return "-fx-font-size: 13px;" +
               "-fx-text-fill: " + ON_SURFACE_VAR + ";";
    }

    static String errorStyle() {
        return "-fx-font-size: 12px; -fx-text-fill: " + ERROR + ";";
    }

    static String successStyle() {
        return "-fx-font-size: 12px; -fx-text-fill: " + SURFACE_TINT + ";";
    }

    static String tableHeaderCell() {
        return "-fx-font-size: 10px;" +
               "-fx-font-weight: bold;" +
               "-fx-text-fill: " + ON_SURFACE_VAR + ";" +
               "-fx-background-color: " + SURFACE_HIGH + ";" +
               "-fx-padding: 12 16;";
    }

    // ── Effects ──────────────────────────────────────────────────────────────

    static DropShadow cardShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(NAVY, 0.07));
        shadow.setRadius(40);
        shadow.setOffsetX(0);
        shadow.setOffsetY(8);
        return shadow;
    }

    static DropShadow subtleShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(NAVY, 0.04));
        shadow.setRadius(20);
        shadow.setOffsetX(0);
        shadow.setOffsetY(4);
        return shadow;
    }
}
