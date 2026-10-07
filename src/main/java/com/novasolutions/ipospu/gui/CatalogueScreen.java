package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.controller.CatalogueController;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.Product;
import com.novasolutions.ipospu.service.CartService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CatalogueScreen extends BorderPane {

    private final CatalogueController catalogueController = new CatalogueController();
    private final CartService cartService = new CartService();

    public CatalogueScreen(Stage stage, Member member) {
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "catalogue"));
        setCenter(buildContent(stage, member));
    }

    private ScrollPane buildContent(Stage stage, Member member) {

        // ── Back button + page header ─────────────────────────────────────
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppStyles.PRIMARY + ";" +
                         "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: transparent;");
        backBtn.setOnAction(e -> {
            stage.getScene().setRoot(new DashboardScreen(stage, member));
            stage.setTitle("IPOS-PU | Dashboard");
        });

        Label pageTitle = new Label("Material Catalogue");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label("Browse and procure materials for active project ledgers. Prices are live from vendor streams.");
        pageSub.setStyle(AppStyles.bodyMuted());
        pageSub.setWrapText(true);

        VBox pageHeader = new VBox(10, backBtn, pageTitle, pageSub);

        // ── Filter bar ────────────────────────────────────────────────────
        Label searchLabel = new Label("KEYWORD SEARCH");
        searchLabel.setStyle(AppStyles.fieldLabel());

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or code…");
        searchField.setStyle(AppStyles.inputField());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        VBox searchGroup = new VBox(6, searchLabel, searchField);
        HBox.setHgrow(searchGroup, Priority.ALWAYS);

        HBox filterBar = new HBox(searchGroup);
        filterBar.setAlignment(Pos.BOTTOM_LEFT);
        filterBar.setSpacing(16);
        filterBar.setPadding(new Insets(20));
        filterBar.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOW + "; -fx-background-radius: 12;");

        // ── Table ─────────────────────────────────────────────────────────
        ObservableList<Product> data = FXCollections.observableArrayList();
        FilteredList<Product> filtered = new FilteredList<>(data, p -> true);

        // Load products in background so screen renders immediately
        new Thread(() -> {
            java.util.List<Product> products = catalogueController.loadProducts();
            javafx.application.Platform.runLater(() -> data.setAll(products));
        }).start();

        searchField.textProperty().addListener((obs, old, val) ->
                filtered.setPredicate(p -> {
                    if (val == null || val.isBlank()) return true;
                    String lower = val.toLowerCase();
                    return p.getName().toLowerCase().contains(lower)
                        || p.getCode().toLowerCase().contains(lower)
                        || p.getPackageType().toLowerCase().contains(lower);
                }));

        TableView<Product> table = buildTable(stage, member);
        table.setItems(filtered);
        table.setMinHeight(400);

        // ── Table container card ──────────────────────────────────────────
        Label itemCount = new Label();
        filtered.addListener((javafx.collections.ListChangeListener<Product>) change ->
                itemCount.setText("Showing " + filtered.size() + " item" + (filtered.size() == 1 ? "" : "s")));
        itemCount.setText("Showing " + filtered.size() + " item" + (filtered.size() == 1 ? "" : "s"));
        itemCount.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        VBox tableCard = new VBox(0, table, buildTableFooter(itemCount));
        tableCard.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST + "; -fx-background-radius: 12;");
        tableCard.setEffect(AppStyles.subtleShadow());

        // ── Page assembly ─────────────────────────────────────────────────
        VBox page = new VBox(24, pageHeader, filterBar, tableCard);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    @SuppressWarnings("unchecked")
    private TableView<Product> buildTable(Stage stage, Member member) {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");

        // Custom row factory for hover styling
        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            String base  = "-fx-background-color: " + AppStyles.SURFACE_LOWEST + ";";
            String hover = "-fx-background-color: " + AppStyles.SURFACE_LOW + ";";
            row.setOnMouseEntered(e -> { if (!row.isEmpty()) row.setStyle(hover); });
            row.setOnMouseExited(e  -> row.setStyle(base));
            return row;
        });

        table.getColumns().addAll(
                col("Name",          "name",          280),
                codeCol(),
                col("Package Type",  "packageType",   120),
                col("Unit",          "unit",           80),
                intCol("Units/Pack", "unitsPerPack",   90),
                priceCol(),
                stockCol(),
                addToCartCol(stage, member)
        );

        table.setPlaceholder(buildEmptyState());

        return table;
    }

    private <T> TableColumn<Product, T> col(String header, String property, double minWidth) {
        TableColumn<Product, T> col = new TableColumn<>(header);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMinWidth(minWidth);
        col.setStyle("-fx-font-size: 13px;");
        styleHeader(col);
        return col;
    }

    private TableColumn<Product, Integer> intCol(String header, String property, double minWidth) {
        TableColumn<Product, Integer> col = new TableColumn<>(header);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setMinWidth(minWidth);
        styleHeader(col);
        return col;
    }

    private TableColumn<Product, String> codeCol() {
        TableColumn<Product, String> col = new TableColumn<>("Code");
        col.setCellValueFactory(new PropertyValueFactory<>("code"));
        col.setMinWidth(120);
        styleHeader(col);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;" +
                                   "-fx-background-color: " + AppStyles.SURFACE_CONTAINER + ";" +
                                   "-fx-background-radius: 4; -fx-padding: 3 8;" +
                                   "-fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        return col;
    }

    private TableColumn<Product, Double> priceCol() {
        TableColumn<Product, Double> col = new TableColumn<>("Price");
        col.setCellValueFactory(new PropertyValueFactory<>("price"));
        col.setMinWidth(90);
        styleHeader(col);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("£%.2f", item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");
                }
            }
        });
        return col;
    }

    private TableColumn<Product, Integer> stockCol() {
        TableColumn<Product, Integer> col = new TableColumn<>("Stock");
        col.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        col.setMinWidth(90);
        styleHeader(col);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    String bg, fg;
                    if (item <= 0) {
                        bg = AppStyles.ERROR_CONT; fg = AppStyles.ON_ERROR_CONT;
                    } else if (item < 10) {
                        bg = AppStyles.TERT_FIXED; fg = AppStyles.ON_TERT_VAR;
                    } else {
                        bg = "#d4edda"; fg = AppStyles.SURFACE_TINT;
                    }
                    Label badge = new Label(item <= 0 ? "Out of Stock" : String.valueOf(item));
                    badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;" +
                                   "-fx-background-color: " + bg + ";" +
                                   "-fx-text-fill: " + fg + ";" +
                                   "-fx-background-radius: 20; -fx-padding: 3 10;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        return col;
    }

    private void styleHeader(TableColumn<?, ?> col) {
        col.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
    }

    private HBox buildTableFooter(Label itemCount) {
        HBox footer = new HBox(itemCount);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle("-fx-background-color: " + AppStyles.SURFACE_HIGH + "; -fx-background-radius: 0 0 12 12;");
        return footer;
    }

    private TableColumn<Product, Void> addToCartCol(Stage stage, Member member) {
        TableColumn<Product, Void> col = new TableColumn<>("");
        col.setMinWidth(130);
        col.setSortable(false);
        styleHeader(col);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Add to Cart");
            private final Label  msg = new Label();
            private final VBox   box = new VBox(4, btn, msg);
            {
                btn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 6 14; -fx-font-size: 12px;");
                msg.setStyle("-fx-font-size: 10px;");
                box.setAlignment(Pos.CENTER);

                PauseTransition clearMsg = new PauseTransition(Duration.millis(800));
                clearMsg.setOnFinished(ev -> msg.setText(""));

                btn.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    clearMsg.stop();
                    msg.setText("");

                    if (p.getStockQuantity() <= 0) {
                        msg.setText("Out of stock");
                        msg.setStyle(AppStyles.errorStyle());
                        return;
                    }

                    btn.setDisable(true);
                    new Thread(() -> {
                        CartService.AddResult result = cartService.addToCart(member, p.getId(), 1);
                        javafx.application.Platform.runLater(() -> {
                            btn.setDisable(false);
                            switch (result) {
                                case SUCCESS -> {
                                    msg.setText("Added!");
                                    msg.setStyle(AppStyles.successStyle());
                                }
                                case OUT_OF_STOCK -> {
                                    msg.setText("Out of stock");
                                    msg.setStyle(AppStyles.errorStyle());
                                }
                                case INSUFFICIENT_STOCK -> {
                                    msg.setText("Not enough stock");
                                    msg.setStyle(AppStyles.errorStyle());
                                }
                            }
                            clearMsg.playFromStart();
                        });
                    }).start();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); }
                else {
                    msg.setText("");
                    setGraphic(box);
                }
            }
        });
        return col;
    }

    private VBox buildEmptyState() {
        Label icon = new Label("📦");
        icon.setStyle("-fx-font-size: 36px;");

        Label msg = new Label("No products found");
        msg.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label sub = new Label("Try adjusting your search, or check the database connection.");
        sub.setStyle(AppStyles.bodyMuted());

        VBox empty = new VBox(8, icon, msg, sub);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(48));
        return empty;
    }
}
