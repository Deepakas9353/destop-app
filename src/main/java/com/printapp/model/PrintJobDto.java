package com.printapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintJobDto {
    private int id;
    private int copies;

    @JsonProperty("color_mode")
    private int colorMode;

    @JsonProperty("sides")
    private int duplexMode;

    @JsonProperty("pages_per_sheet")
    private int pagesPerSheet;

    @JsonProperty("printer_name")
    private String printerName;

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_base64")
    private String fileBase64;

    @JsonProperty("created_at")
    private String createdAt;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public int getColorMode() {
        return colorMode;
    }

    public void setColorMode(int colorMode) {
        this.colorMode = colorMode;
    }

    public int getDuplexMode() {
        return duplexMode;
    }

    public void setDuplexMode(int duplexMode) {
        this.duplexMode = duplexMode;
    }

    public int getPagesPerSheet() {
        return pagesPerSheet;
    }

    public void setPagesPerSheet(int pagesPerSheet) {
        this.pagesPerSheet = pagesPerSheet;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
