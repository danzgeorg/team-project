package com.novasolutions.ipospu.gui;

import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.PromotionCampaign;
import com.novasolutions.ipospu.model.report.CampaignEngagementRow;
import com.novasolutions.ipospu.model.report.CampaignProductSaleRow;
import com.novasolutions.ipospu.model.report.CampaignReportRow;
import com.novasolutions.ipospu.model.report.SalesReportRow;
import com.novasolutions.ipospu.service.PromotionService;
import com.novasolutions.ipospu.service.ReportService;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsScreen extends BorderPane {

    private final Stage stage;
    private final Member member;
    private final ReportService reportService = new ReportService();
    private final PromotionService promotionService = new PromotionService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReportsScreen(Stage stage, Member member) {
        this.stage  = stage;
        this.member = member;
        setTop(new TopBar(stage, member));
        setLeft(new SideBar(stage, member, "reports"));
        setCenter(buildContent());
    }

    private ScrollPane buildContent() {
        Label pageTitle = new Label("Reports");
        pageTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        Label pageSub = new Label("Generate IPOS-PU operational reports.");
        pageSub.setStyle(AppStyles.bodyMuted());

        VBox header = new VBox(6, pageTitle, pageSub);

        VBox report1 = buildSalesReportSection();
        VBox report2 = buildCampaignReportSection();
        VBox report3 = buildEngagementReportSection();

        VBox page = new VBox(32, header, report1, report2, report3);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: " + AppStyles.SURFACE + ";");

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: " + AppStyles.SURFACE + "; -fx-background: " + AppStyles.SURFACE + ";");
        return scroll;
    }

    // ─── Report 1: Sales Report ──────────────────────────────────────────────────

    private VBox buildSalesReportSection() {
        Label title = new Label("Sales Report");
        title.setStyle(AppStyles.sectionTitle());

        Label sub = new Label("Quantity of goods sold, unit price and total per item for a date range. (Appendix 8)");
        sub.setStyle(AppStyles.bodyMuted());
        sub.setWrapText(true);

        DatePicker fromPicker = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker toPicker   = new DatePicker(LocalDate.now());
        fromPicker.setStyle(AppStyles.inputField());
        toPicker.setStyle(AppStyles.inputField());

        Label fromLbl = new Label("From:");
        Label toLbl   = new Label("To:");
        fromLbl.setStyle(AppStyles.bodyMuted());
        toLbl.setStyle(AppStyles.bodyMuted());

        Button generateBtn = new Button("Generate");
        generateBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 8 20;");

        Button printBtn = new Button("Print");
        printBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        printBtn.setDisable(true);

        Button downloadBtn = new Button("Download");
        downloadBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        downloadBtn.setDisable(true);

        HBox controls = new HBox(12, fromLbl, fromPicker, toLbl, toPicker, generateBtn, printBtn, downloadBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox resultsBox = new VBox(0);

        generateBtn.setOnAction(e -> {
            LocalDate from = fromPicker.getValue();
            LocalDate to   = toPicker.getValue();
            if (from == null || to == null || from.isAfter(to)) {
                resultsBox.getChildren().setAll(errorLabel("Please select a valid date range."));
                printBtn.setDisable(true);
                downloadBtn.setDisable(true);
                return;
            }
            generateBtn.setDisable(true);
            printBtn.setDisable(true);
            downloadBtn.setDisable(true);
            new Thread(() -> {
                List<SalesReportRow> rows = reportService.getSalesReport(from, to);
                javafx.application.Platform.runLater(() -> {
                    generateBtn.setDisable(false);
                    VBox table = buildSalesTable(rows, from, to);
                    resultsBox.getChildren().setAll(table);
                    printBtn.setDisable(rows.isEmpty());
                    downloadBtn.setDisable(rows.isEmpty());
                    printBtn.setOnAction(pe -> printNode(table, "IPOS-PU Sales Report"));
                    downloadBtn.setOnAction(de -> downloadNode(table, "ipos-pu-sales-report"));
                });
            }).start();
        });

        VBox section = new VBox(16, title, sub, controls, resultsBox);
        section.setPadding(new Insets(24));
        section.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST +
                         "; -fx-background-radius: 12;");
        section.setEffect(AppStyles.subtleShadow());
        return section;
    }

    private VBox buildSalesTable(List<SalesReportRow> rows, LocalDate from, LocalDate to) {
        Label period = new Label("Period: " + from.format(DATE_FMT) + " – " + to.format(DATE_FMT));
        period.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        if (rows.isEmpty()) {
            return new VBox(8, period, errorLabel("No sales found for this period."));
        }

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);

        String[] headers = {"Item ID", "Description", "Sold (packs)", "Unit Price (£)", "Total (£)"};
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
            grid.add(h, i, 0);
        }

        int totalPacks = 0;
        double totalRevenue = 0;

        for (int r = 0; r < rows.size(); r++) {
            SalesReportRow row = rows.get(r);
            totalPacks   += row.getSoldPacks();
            totalRevenue += row.getTotal();

            grid.add(cell(row.getItemId()), 0, r + 1);
            grid.add(cell(row.getDescription()), 1, r + 1);
            grid.add(cellRight(String.valueOf(row.getSoldPacks())), 2, r + 1);
            grid.add(cellRight(String.format("%.2f", row.getUnitPrice())), 3, r + 1);
            grid.add(cellRight(String.format("%.2f", row.getTotal())), 4, r + 1);
        }

        Separator sep = new Separator();

        Label totalsLine = new Label(String.format(
                "Total online sales for period:  %d packs   |   £%.2f", totalPacks, totalRevenue));
        totalsLine.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        return new VBox(12, period, grid, sep, totalsLine);
    }

    // ─── Report 2: Advertising Campaigns Report ──────────────────────────────────

    private VBox buildCampaignReportSection() {
        Label title = new Label("Advertising Campaigns Report");
        title.setStyle(AppStyles.sectionTitle());

        Label sub = new Label("Campaigns active in a period with per-product sales breakdown. (Appendix 9)");
        sub.setStyle(AppStyles.bodyMuted());
        sub.setWrapText(true);

        DatePicker fromPicker = new DatePicker(LocalDate.now().minusMonths(3));
        DatePicker toPicker   = new DatePicker(LocalDate.now());
        fromPicker.setStyle(AppStyles.inputField());
        toPicker.setStyle(AppStyles.inputField());

        Label fromLbl = new Label("From:");
        Label toLbl   = new Label("To:");
        fromLbl.setStyle(AppStyles.bodyMuted());
        toLbl.setStyle(AppStyles.bodyMuted());

        Button generateBtn = new Button("Generate");
        generateBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 8 20;");

        Button printBtn = new Button("Print");
        printBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        printBtn.setDisable(true);

        Button downloadBtn = new Button("Download");
        downloadBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        downloadBtn.setDisable(true);

        HBox controls = new HBox(12, fromLbl, fromPicker, toLbl, toPicker, generateBtn, printBtn, downloadBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox resultsBox = new VBox(0);

        generateBtn.setOnAction(e -> {
            LocalDate from = fromPicker.getValue();
            LocalDate to   = toPicker.getValue();
            if (from == null || to == null || from.isAfter(to)) {
                resultsBox.getChildren().setAll(errorLabel("Please select a valid date range."));
                printBtn.setDisable(true);
                downloadBtn.setDisable(true);
                return;
            }
            generateBtn.setDisable(true);
            printBtn.setDisable(true);
            downloadBtn.setDisable(true);
            new Thread(() -> {
                List<CampaignReportRow> campaigns = reportService.getCampaignReport(from, to);
                javafx.application.Platform.runLater(() -> {
                    generateBtn.setDisable(false);
                    VBox table = buildCampaignTable(campaigns, from, to);
                    resultsBox.getChildren().setAll(table);
                    printBtn.setDisable(campaigns.isEmpty());
                    downloadBtn.setDisable(campaigns.isEmpty());
                    printBtn.setOnAction(pe -> printNode(table, "IPOS-PU Advertising Campaigns Report"));
                    downloadBtn.setOnAction(de -> downloadNode(table, "ipos-pu-advertising-campaigns-report"));
                });
            }).start();
        });

        VBox section = new VBox(16, title, sub, controls, resultsBox);
        section.setPadding(new Insets(24));
        section.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST +
                         "; -fx-background-radius: 12;");
        section.setEffect(AppStyles.subtleShadow());
        return section;
    }

    private VBox buildCampaignTable(List<CampaignReportRow> campaigns, LocalDate from, LocalDate to) {
        Label period = new Label("Period: " + from.format(DATE_FMT) + " – " + to.format(DATE_FMT));
        period.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        if (campaigns.isEmpty()) {
            return new VBox(8, period, errorLabel("No campaigns found for this period."));
        }

        VBox all = new VBox(20, period);
        for (CampaignReportRow c : campaigns) {
            VBox campBlock = buildCampaignBlock(c);
            all.getChildren().add(campBlock);
        }
        return all;
    }

    private VBox buildCampaignBlock(CampaignReportRow c) {
        Label campHeader = new Label(String.format("%s   |   %s – %s   |   %d items   |   %.0f%% discount",
                c.getName(),
                c.getStartDate().format(DATE_FMT),
                c.getEndDate().format(DATE_FMT),
                c.getItemCount(),
                c.getDiscountPercent()));
        campHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(6);
        grid.setPadding(new Insets(8, 0, 4, 0));

        String[] hdrs = {"Item ID", "Description", "Discount", "Items Sold", "Total Sales (£)"};
        for (int i = 0; i < hdrs.length; i++) {
            Label h = new Label(hdrs[i]);
            h.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
            grid.add(h, i, 0);
        }

        List<CampaignProductSaleRow> products = c.getProducts();
        for (int r = 0; r < products.size(); r++) {
            CampaignProductSaleRow p = products.get(r);
            grid.add(cell(p.getItemId()), 0, r + 1);
            grid.add(cell(p.getDescription()), 1, r + 1);
            grid.add(cellRight(String.format("%.0f%%", p.getDiscountPercent())), 2, r + 1);
            grid.add(cellRight(String.valueOf(p.getItemsSold())), 3, r + 1);
            grid.add(cellRight(String.format("%.2f", p.getTotalSales())), 4, r + 1);
        }

        Label campTotal = new Label(String.format("Total Sales in campaign:  £%.2f", c.getTotalCampaignSales()));
        campTotal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.SURFACE_TINT + ";");

        VBox block = new VBox(8, campHeader, grid, new Separator(), campTotal);
        block.setPadding(new Insets(16));
        block.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOW + "; -fx-background-radius: 8;");
        return block;
    }

    // ─── Report 3: Customer Campaign Engagement ──────────────────────────────────

    private VBox buildEngagementReportSection() {
        Label title = new Label("Customer Campaign Engagement Report");
        title.setStyle(AppStyles.sectionTitle());

        Label sub = new Label("Hit counts, purchases and conversion rate per campaign. (Appendix 10)");
        sub.setStyle(AppStyles.bodyMuted());
        sub.setWrapText(true);

        List<PromotionCampaign> campaigns = promotionService.getAllCampaigns();

        ComboBox<PromotionCampaign> campaignPicker = new ComboBox<>();
        campaignPicker.getItems().addAll(campaigns);
        campaignPicker.setButtonCell(new CampaignListCell());
        campaignPicker.setCellFactory(lv -> new CampaignListCell());
        campaignPicker.setPromptText("Select a campaign…");
        campaignPicker.setStyle(AppStyles.inputField() + "-fx-min-width: 240px;");

        if (!campaigns.isEmpty()) campaignPicker.getSelectionModel().selectFirst();

        Button generateBtn = new Button("Generate");
        generateBtn.setStyle(AppStyles.ghostGradBtn() + "-fx-padding: 8 20;");

        Button printBtn = new Button("Print");
        printBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        printBtn.setDisable(true);

        Button downloadBtn = new Button("Download");
        downloadBtn.setStyle(AppStyles.secondaryBtn() + "-fx-padding: 8 20;");
        downloadBtn.setDisable(true);

        HBox controls = new HBox(12, new Label("Campaign:"), campaignPicker, generateBtn, printBtn, downloadBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        ((Label) controls.getChildren().get(0)).setStyle(AppStyles.bodyMuted());

        VBox resultsBox = new VBox(0);

        generateBtn.setOnAction(e -> {
            PromotionCampaign selected = campaignPicker.getValue();
            if (selected == null) {
                resultsBox.getChildren().setAll(errorLabel("Please select a campaign."));
                printBtn.setDisable(true);
                downloadBtn.setDisable(true);
                return;
            }
            generateBtn.setDisable(true);
            printBtn.setDisable(true);
            downloadBtn.setDisable(true);
            new Thread(() -> {
                List<CampaignEngagementRow> rows = reportService.getCampaignEngagement(selected.id());
                javafx.application.Platform.runLater(() -> {
                    generateBtn.setDisable(false);
                    VBox table = buildEngagementTable(rows, selected);
                    resultsBox.getChildren().setAll(table);
                    printBtn.setDisable(rows.isEmpty());
                    downloadBtn.setDisable(rows.isEmpty());
                    printBtn.setOnAction(pe -> printNode(table, "IPOS-PU Customer Engagement Report"));
                    downloadBtn.setOnAction(de -> downloadNode(table, "ipos-pu-customer-engagement-report"));
                });
            }).start();
        });

        VBox section = new VBox(16, title, sub, controls, resultsBox);
        section.setPadding(new Insets(24));
        section.setStyle("-fx-background-color: " + AppStyles.SURFACE_LOWEST +
                         "; -fx-background-radius: 12;");
        section.setEffect(AppStyles.subtleShadow());
        return section;
    }

    private VBox buildEngagementTable(List<CampaignEngagementRow> rows, PromotionCampaign campaign) {
        Label campLabel = new Label("Campaign: " + campaign.name());
        campLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");

        if (rows.isEmpty()) {
            return new VBox(8, campLabel, errorLabel("No tracking data recorded for this campaign yet."));
        }

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);

        String[] headers = {"Counter", "Description", "Hits", "Purchases", "Conversion Rate"};
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + ";");
            grid.add(h, i, 0);
        }

        int itemIndex = 1;
        for (int r = 0; r < rows.size(); r++) {
            CampaignEngagementRow row = rows.get(r);

            String counterId, description, purchases, conversion;

            if (row.isCampaignLevel()) {
                counterId   = campaign.name();
                description = "Campaign hits";
                purchases   = "N/A";
                conversion  = "N/A";
            } else {
                counterId   = "Item(" + itemIndex + ") hits";
                description = "\"" + (row.getProductName() != null ? row.getProductName() : "Unknown") + "\" hits";
                purchases   = String.valueOf(row.getItemsPurchasedCount());
                conversion  = row.getConversionRate() != null
                        ? String.format("%.0f / %d = %.2f%%",
                            (double) row.getItemsPurchasedCount(),
                            row.getClickCount(),
                            row.getConversionRate())
                        : "N/A";
                itemIndex++;
            }

            grid.add(cell(counterId), 0, r + 1);
            grid.add(cell(description), 1, r + 1);
            grid.add(cellRight(String.valueOf(row.getClickCount())), 2, r + 1);
            grid.add(cellRight(purchases), 3, r + 1);
            grid.add(cell(conversion), 4, r + 1);
        }

        return new VBox(12, campLabel, grid);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private Label cell(String text) {
        Label l = new Label(text != null ? text : "—");
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_SURFACE + ";");
        return l;
    }

    private Label cellRight(String text) {
        Label l = cell(text);
        l.setAlignment(Pos.CENTER_RIGHT);
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    private Label errorLabel(String msg) {
        Label l = new Label(msg);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppStyles.ON_SURFACE_VAR + "; -fx-padding: 8 0 0 0;");
        return l;
    }

    private void printNode(javafx.scene.Node node, String jobTitle) {
        // Snapshot first so we print the full content regardless of clipping/scroll state
        javafx.scene.image.WritableImage snapshot = node.snapshot(
                new javafx.scene.SnapshotParameters(), null);
        javafx.scene.image.ImageView printable = new javafx.scene.image.ImageView(snapshot);

        if (javafx.print.Printer.getDefaultPrinter() == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Print");
            alert.setHeaderText("No printer found");
            alert.setContentText("No printer is configured on this system.\nTo test, add a PDF printer (e.g. 'Microsoft Print to PDF' or macOS PDF option in the print dialog).");
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;
        job.getJobSettings().setJobName(jobTitle);

        // Scale image to fit the printable page
        javafx.print.PageLayout layout = job.getJobSettings().getPageLayout();
        double pageW = layout.getPrintableWidth();
        double pageH = layout.getPrintableHeight();
        double imgW  = snapshot.getWidth();
        double imgH  = snapshot.getHeight();
        double scale = Math.min(pageW / imgW, pageH / imgH);
        printable.setFitWidth(imgW * scale);
        printable.setFitHeight(imgH * scale);
        printable.setPreserveRatio(true);

        boolean proceed = job.showPrintDialog(stage);
        if (proceed) {
            boolean printed = job.printPage(printable);
            if (printed) job.endJob();
        }
    }

    private void downloadNode(javafx.scene.Node node, String defaultFileName) {
        javafx.scene.image.WritableImage snapshot = node.snapshot(
                new javafx.scene.SnapshotParameters(), null);

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Download Report");
        chooser.setInitialFileName(defaultFileName + ".png");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File target = chooser.showSaveDialog(stage);
        if (target == null) {
            return;
        }

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", target);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Download");
            alert.setHeaderText("Could not save report");
            alert.setContentText("Failed to save the report image.\n" + e.getMessage());
            alert.initOwner(stage);
            alert.showAndWait();
        }
    }

    private static class CampaignListCell extends ListCell<PromotionCampaign> {
        @Override
        protected void updateItem(PromotionCampaign item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item.name() + " (" + item.status() + ")");
        }
    }
}
