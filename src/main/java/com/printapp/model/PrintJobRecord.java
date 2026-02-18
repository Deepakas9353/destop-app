package com.printapp.model;

import javafx.beans.property.*;
import java.io.File;

public class PrintJobRecord {
    private final IntegerProperty id;
    private final IntegerProperty copies;
    private final IntegerProperty colorMode;
    private final IntegerProperty duplexMode;
    private final IntegerProperty pagesPerSheet;
    private final StringProperty selectedPrinter;
    private final ObjectProperty<File> uploadedFile;

    public PrintJobRecord(int id, int copies, int colorMode, int duplexMode, int pagesPerSheet) {
        this.id = new SimpleIntegerProperty(id);
        this.copies = new SimpleIntegerProperty(copies);
        this.colorMode = new SimpleIntegerProperty(colorMode);
        this.duplexMode = new SimpleIntegerProperty(duplexMode);
        this.pagesPerSheet = new SimpleIntegerProperty(pagesPerSheet);
        this.selectedPrinter = new SimpleStringProperty("");
        this.uploadedFile = new SimpleObjectProperty<>(null);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getCopies() {
        return copies.get();
    }

    public IntegerProperty copiesProperty() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies.set(copies);
    }

    public int getColorMode() {
        return colorMode.get();
    }

    public IntegerProperty colorModeProperty() {
        return colorMode;
    }

    public void setColorMode(int colorMode) {
        this.colorMode.set(colorMode);
    }

    public int getDuplexMode() {
        return duplexMode.get();
    }

    public IntegerProperty duplexModeProperty() {
        return duplexMode;
    }

    public void setDuplexMode(int duplexMode) {
        this.duplexMode.set(duplexMode);
    }

    public int getPagesPerSheet() {
        return pagesPerSheet.get();
    }

    public IntegerProperty pagesPerSheetProperty() {
        return pagesPerSheet;
    }

    public void setPagesPerSheet(int pagesPerSheet) {
        this.pagesPerSheet.set(pagesPerSheet);
    }

    public String getSelectedPrinter() {
        return selectedPrinter.get();
    }

    public StringProperty selectedPrinterProperty() {
        return selectedPrinter;
    }

    public void setSelectedPrinter(String selectedPrinter) {
        this.selectedPrinter.set(selectedPrinter);
    }

    public File getUploadedFile() {
        return uploadedFile.get();
    }

    public ObjectProperty<File> uploadedFileProperty() {
        return uploadedFile;
    }

    public void setUploadedFile(File uploadedFile) {
        this.uploadedFile.set(uploadedFile);
    }
}
