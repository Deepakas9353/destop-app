package com.printapp;

import com.printapp.model.PrintConfig;
import com.printapp.model.PrintJobDto;
import com.printapp.model.PrintJobRecord;
import com.printapp.service.ApiService;
import com.printapp.service.PrinterService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private final ObservableList<PrintJobRecord> printJobs = FXCollections.observableArrayList();
    private List<String> availablePrinters;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Elite Print Grid Utility");

        // Load available printers
        availablePrinters = printerService.getAvailablePrinters();

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8fafc; -fx-font-family: 'Segoe UI', sans-serif;");

        // Header
        Label title = new Label("Print Management Grid");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Manage and execute your print jobs efficiently");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        // Refresh Button
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 20;");
        refreshBtn.setOnAction(e -> handleRefresh());

        HBox topBar = new HBox(20, new VBox(5, title, subtitle), new Region(), refreshBtn);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().add(topBar);

        // TableView Setup
        TableView<PrintJobRecord> table = new TableView<>(printJobs);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-background-radius: 10; -fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 5;");
        table.setPrefHeight(400);

        // Columns
        TableColumn<PrintJobRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<PrintJobRecord, Integer> copiesCol = new TableColumn<>("Copies");
        copiesCol.setCellValueFactory(new PropertyValueFactory<>("copies"));
        copiesCol.setPrefWidth(80);

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

        TableColumn<PrintJobRecord, Integer> duplexCol = new TableColumn<>("Duplex Mode");
        duplexCol.setCellValueFactory(new PropertyValueFactory<>("duplexMode"));
        duplexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == 2 ? "Duplex (2)" : "Single (1)");
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
        uploadCol.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Upload");
            private final Label fileLabel = new Label();
            private final HBox container = new HBox(10, btn, fileLabel);
            {
                container.setAlignment(Pos.CENTER_LEFT);
                btn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
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
                btn.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 15;");
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
        root.getChildren().add(table);

        // Footer / Status
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label statusInfo = new Label("System Synchronized | Refreshing every 10s");
        statusInfo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        footer.getChildren().add(statusInfo);
        root.getChildren().add(footer);

        Scene scene = new Scene(root, 1100, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial Load
        handleRefresh();
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

    public static void main(String[] args) {
        launch(args);
    }
}
