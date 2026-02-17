package com.printapp;

import com.printapp.model.PrintConfig;
import com.printapp.service.PrinterService;
import com.printapp.service.SettingsService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class App extends Application {

    private final PrinterService printerService = new PrinterService();
    private final PrintConfig config = new PrintConfig();
    private File selectedFile;

    private Label statusLabel;
    private ComboBox<String> printerDropdown;
    private ComboBox<Integer> copiesDropdown;
    private ComboBox<String> layoutDropdown;
    private ComboBox<String> sideDropdown;
    private Label fileNameLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Elite Print Utility");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setPrefWidth(450);
        root.setStyle("-fx-background-color: #f4f7f6; -fx-font-family: 'Segoe UI', sans-serif;");

        // Header
        Label title = new Label("Elite Print Utility");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        root.getChildren().add(title);

        // Printer Selection
        VBox printerBox = createFieldBox("Select Printer", printerDropdown = new ComboBox<>());
        refreshPrinters();
        root.getChildren().add(printerBox);

        // Copies and Layout Row
        HBox row1 = new HBox(20);
        copiesDropdown = new ComboBox<>(FXCollections.observableArrayList(makeRange(1, 50)));
        copiesDropdown.setValue(1);
        row1.getChildren().add(createFieldBox("Copies", copiesDropdown));

        layoutDropdown = new ComboBox<>(FXCollections.observableArrayList(
                "1 Page per Sheet", "2 Pages per Sheet (1x2)", "4 Pages per Sheet (1x4)"));
        layoutDropdown.setValue("1 Page per Sheet");
        row1.getChildren().add(createFieldBox("Page Layout", layoutDropdown));
        root.getChildren().add(row1);

        // Side Option
        sideDropdown = new ComboBox<>(FXCollections.observableArrayList(
                "Single Side", "Front and Back (Duplex)"));
        sideDropdown.setValue("Single Side");
        root.getChildren().add(createFieldBox("Print Side", sideDropdown));

        // File Upload
        VBox fileBox = new VBox(10);
        Button uploadBtn = new Button("Upload PDF / Image");
        uploadBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5;");
        fileNameLabel = new Label("No file selected");
        fileNameLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-italic: true;");

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Support Files", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
            selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                fileNameLabel.setText(selectedFile.getName());
                config.setFileToPrint(selectedFile);
            }
        });
        fileBox.getChildren().addAll(new Label("Document"), uploadBtn, fileNameLabel);
        root.getChildren().add(fileBox);

        // Print Button
        Button printBtn = new Button("PRINT NOW");
        printBtn.setMaxWidth(Double.MAX_VALUE);
        printBtn.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 5;");
        printBtn.setOnAction(e -> handlePrint());
        root.getChildren().add(printBtn);

        // Status Label
        statusLabel = new Label("System Ready");
        statusLabel.setStyle("-fx-text-fill: #16a085;");
        root.getChildren().add(statusLabel);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createFieldBox(String labelText, Control control) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        control.setPrefWidth(200);
        box.getChildren().addAll(label, control);
        return box;
    }

    private List<Integer> makeRange(int start, int end) {
        return java.util.stream.IntStream.rangeClosed(start, end).boxed().toList();
    }

    private final SettingsService settingsService = new SettingsService();

    private void refreshPrinters() {
        List<String> printers = printerService.getAvailablePrinters();
        printerDropdown.setItems(FXCollections.observableArrayList(printers));

        String lastPrinter = settingsService.getLastPrinter();
        if (lastPrinter != null && printers.contains(lastPrinter)) {
            printerDropdown.setValue(lastPrinter);
        } else if (!printers.isEmpty()) {
            printerDropdown.setValue(printers.get(0));
        }
    }

    private void handlePrint() {
        try {
            if (selectedFile == null) {
                showAlert("Error", "Please select a file first.");
                return;
            }

            String printer = printerDropdown.getValue();
            config.setSelectedPrinter(printer);
            config.setCopies(copiesDropdown.getValue());
            config.setLayout(layoutDropdown.getValue());
            config.setSideOption(sideDropdown.getValue());

            // Save last used printer
            settingsService.saveLastPrinter(printer);

            statusLabel.setText("Sending to printer...");
            statusLabel.setStyle("-fx-text-fill: #d35400;");

            printerService.print(config);

            statusLabel.setText("Print job sent successfully!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            statusLabel.setStyle("-fx-text-fill: #c0392b;");
            showAlert("Print Error", ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
