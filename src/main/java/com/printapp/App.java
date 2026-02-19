package com.printapp;

import com.printapp.model.PrintConfig;
import com.printapp.model.PrintJobDto;
import com.printapp.model.PrintJobRecord;
import com.printapp.service.ApiService;
import com.printapp.service.PrinterService;
import com.printapp.service.QrCodeService;
import com.printapp.service.WebSocketClientService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    private final PrinterService printerService = new PrinterService();
    private final ApiService apiService = new ApiService();
    private final QrCodeService qrCodeService = new QrCodeService();
    private final ObservableList<PrintJobRecord> printJobs = FXCollections.observableArrayList();
    private List<String> availablePrinters;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private WebSocketClientService webSocketClientService;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Elite Print Utility");

        // Load Application Icon
        try {
            primaryStage.getIcons()
                    .add(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/app_icon.png")));
        } catch (Exception e) {
            System.out.println("No icon found at /icons/app_icon.png, using default.");
        }

        // Load available printers
        availablePrinters = printerService.getAvailablePrinters();

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root");

        // --- HEADER BAR ---
        VBox headerContent = new VBox(5);
        Label title = new Label("Elite Print Utility");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Manage and execute your print jobs efficiently");
        subtitle.getStyleClass().add("subtitle-label");
        headerContent.getChildren().addAll(title, subtitle);

        // Static QR Code in Header
        VBox qrBox = createHeaderQrBox();

        Button refreshBtn = new Button("Refresh Data");
        refreshBtn.getStyleClass().addAll("button", "button-primary");
        refreshBtn.setOnAction(e -> handleRefresh());

        HBox headerActions = new HBox(20, qrBox, refreshBtn);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        HBox headerBar = new HBox();
        headerBar.getStyleClass().add("header-bar");
        headerBar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBar.getChildren().addAll(headerContent, spacer, headerActions);

        mainLayout.setTop(headerBar);

        // --- CENTER CONTENT (TABLE) ---
        VBox centerContainer = new VBox(20);
        centerContainer.getStyleClass().add("main-container");

        TableView<PrintJobRecord> table = new TableView<>(printJobs);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(500);

        // Columns
        TableColumn<PrintJobRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);
        idCol.setMinWidth(60);

        TableColumn<PrintJobRecord, Integer> copiesCol = new TableColumn<>("Copies");
        copiesCol.setCellValueFactory(new PropertyValueFactory<>("copies"));
        copiesCol.setMaxWidth(80);
        copiesCol.setMinWidth(80);

        TableColumn<PrintJobRecord, Integer> colorCol = new TableColumn<>("Color Mode");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("colorMode"));
        colorCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == 10 ? "Color (10)" : "B/W (1)");
                }
            }
        });

        TableColumn<PrintJobRecord, Integer> duplexCol = new TableColumn<>("Sides");
        duplexCol.setCellValueFactory(new PropertyValueFactory<>("duplexMode"));
        duplexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == 2 ? "Front & Back" : "Front");
                }
            }
        });

        TableColumn<PrintJobRecord, Integer> pagesCol = new TableColumn<>("Pages/Sheet");
        pagesCol.setCellValueFactory(new PropertyValueFactory<>("pagesPerSheet"));

        // Printer ComboBox Column
        TableColumn<PrintJobRecord, String> printerCol = new TableColumn<>("Printer");
        printerCol.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>(
                    FXCollections.observableArrayList(availablePrinters));
            {
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setOnAction(e -> {
                    if (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        PrintJobRecord record = getTableView().getItems().get(getIndex());
                        record.setSelectedPrinter(comboBox.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PrintJobRecord record = getTableView().getItems().get(getIndex());
                    if (record.getSelectedPrinter() == null || record.getSelectedPrinter().isEmpty()) {
                        if (!availablePrinters.isEmpty()) {
                            comboBox.setValue(availablePrinters.get(0));
                            record.setSelectedPrinter(availablePrinters.get(0));
                        }
                    } else {
                        comboBox.setValue(record.getSelectedPrinter());
                    }
                    setGraphic(comboBox);
                }
            }
        });

        // Upload Button Column
        TableColumn<PrintJobRecord, Void> uploadCol = new TableColumn<>("Upload File");
        uploadCol.setMinWidth(250);
        uploadCol.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Upload");
            private final Label fileLabel = new Label();
            private final HBox container = new HBox(10, btn, fileLabel);
            {
                container.setAlignment(Pos.CENTER_LEFT);
                btn.getStyleClass().addAll("button", "button-upload");
                btn.setOnAction(e -> {
                    PrintJobRecord record = getTableView().getItems().get(getIndex());
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Files", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
                    File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        record.setUploadedFile(file);
                        fileLabel.setText(file.getName());
                        fileLabel.setTooltip(new Tooltip(file.getAbsolutePath()));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PrintJobRecord record = getTableView().getItems().get(getIndex());
                    fileLabel.setText(record.getUploadedFile() != null ? record.getUploadedFile().getName() : "None");
                    setGraphic(container);
                }
            }
        });

        // Print Button Column
        TableColumn<PrintJobRecord, Void> printCol = new TableColumn<>("Actions");
        printCol.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("PRINT");
            {
                btn.getStyleClass().addAll("button", "button-success");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> {
                    PrintJobRecord record = getTableView().getItems().get(getIndex());
                    handlePrint(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        @SuppressWarnings("unchecked")
        ObservableList<TableColumn<PrintJobRecord, ?>> columns = FXCollections.observableArrayList(
                idCol, copiesCol, colorCol, duplexCol, pagesCol, printerCol, uploadCol, printCol);
        table.getColumns().addAll(columns);
        centerContainer.getChildren().add(table);
        mainLayout.setCenter(centerContainer);

        // --- FOOTER ---
        HBox footer = new HBox();
        footer.getStyleClass().add("main-container");
        footer.setPadding(new Insets(10, 30, 20, 30));
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label statusInfo = new Label("System Synchronized | Live WebSocket Connected");
        statusInfo.getStyleClass().add("status-label");
        footer.getChildren().add(statusInfo);
        mainLayout.setBottom(footer);

        Scene scene = new Scene(mainLayout, 1200, 700);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS not found, falling back to basic styles.");
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial Load
        handleRefresh();

        // Start WebSocket connection for real-time updates
        webSocketClientService = new WebSocketClientService(() -> {
            Platform.runLater(() -> {
                System.out.println("[App] WebSocket triggered grid refresh.");
                handleRefresh();
            });
        });
        webSocketClientService.connect();
    }

    private void handleRefresh() {
        executorService.submit(() -> {
            try {
                List<PrintJobDto> dtos = apiService.fetchPrintConfigs();
                if (dtos == null) {
                    Platform.runLater(() -> showAlert("Refresh Error", "Failed to fetch latest data"));
                    return;
                }

                Platform.runLater(() -> {
                    printJobs.clear();
                    for (PrintJobDto dto : dtos) {
                        PrintJobRecord record = new PrintJobRecord(
                                dto.getId(),
                                dto.getCopies(),
                                dto.getColorMode(),
                                dto.getDuplexMode(),
                                dto.getPagesPerSheet());

                        // Handle mobile uploaded file if present
                        if (dto.getFileBase64() != null && !dto.getFileBase64().isEmpty()) {
                            try {
                                File tempFile = saveBase64ToFile(dto.getFileBase64(), dto.getFileName());
                                record.setUploadedFile(tempFile);
                            } catch (Exception e) {
                                System.err.println(
                                        "Failed to save mobile file for ID " + dto.getId() + ": " + e.getMessage());
                            }
                        }

                        printJobs.add(record);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Refresh Error", "Failed to fetch latest data"));
            }
        });
    }

    @Override
    public void stop() {
        if (webSocketClientService != null) {
            webSocketClientService.disconnect();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void handlePrint(PrintJobRecord record) {
        if (record.getUploadedFile() == null) {
            showAlert("Upload Required", "Please upload a file before printing.");
            return;
        }

        try {
            PrintConfig config = new PrintConfig();
            config.setCopies(record.getCopies());
            config.setColorMode(record.getColorMode() == 10 ? "Color" : "Black & White");
            config.setSideOption(record.getDuplexMode() == 2 ? "Front and Back (Duplex)" : "Single Side");

            String layoutStr;
            switch (record.getPagesPerSheet()) {
                case 2 -> layoutStr = "2 Pages per Sheet (1x2)";
                case 4 -> layoutStr = "4 Pages per Sheet (1x4)";
                default -> layoutStr = "1 Page per Sheet";
            }
            config.setLayout(layoutStr);
            config.setSelectedPrinter(record.getSelectedPrinter());
            config.setFileToPrint(record.getUploadedFile());

            System.out.println("Printing ID: " + record.getId() + " - " + record.getUploadedFile().getName() + " on "
                    + record.getSelectedPrinter());
            printerService.print(config);

            showInfo("Success", "Print job sent successfully for " + record.getUploadedFile().getName());

        } catch (Exception ex) {
            showAlert("Print Error", "Failed to print: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * Creates a static QR code box for the header.
     */
    private VBox createHeaderQrBox() {
        WritableImage qrImage = qrCodeService.generateQrImage(null, 100);
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(100);
        qrView.setFitHeight(100);
        qrView.setPreserveRatio(true);

        StackPane qrContainer = new StackPane(qrView);
        qrContainer.getStyleClass().add("header-qr-container");

        Label qrLabel = new Label("Scan to Print");
        qrLabel.getStyleClass().add("header-qr-label");

        VBox box = new VBox(4, qrContainer, qrLabel);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private File saveBase64ToFile(String base64Data, String fileName) throws Exception {
        if (base64Data == null || base64Data.isEmpty())
            return null;

        // Remove data URI prefix if present
        String pureBase64 = base64Data;
        if (base64Data.contains(",")) {
            pureBase64 = base64Data.split(",")[1];
        }

        byte[] decodedBytes = java.util.Base64.getDecoder().decode(pureBase64);

        // Use user's temp directory
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, "print_upload_" + System.currentTimeMillis() + "_" + fileName);

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            fos.write(decodedBytes);
        }

        return file;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
