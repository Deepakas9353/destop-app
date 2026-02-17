package com.printapp.model;

import java.io.File;

public class PrintConfig {
    private String selectedPrinter;
    private int copies = 1;
    private String layout = "1 Page per Sheet";
    private String sideOption = "Single Side";
    private String colorMode = "Black & White";
    private File fileToPrint;

    // Getters and Setters
    public String getColorMode() {
        return colorMode;
    }

    public void setColorMode(String colorMode) {
        this.colorMode = colorMode;
    }

    public String getSelectedPrinter() {
        return selectedPrinter;
    }

    public void setSelectedPrinter(String selectedPrinter) {
        this.selectedPrinter = selectedPrinter;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getSideOption() {
        return sideOption;
    }

    public void setSideOption(String sideOption) {
        this.sideOption = sideOption;
    }

    public File getFileToPrint() {
        return fileToPrint;
    }

    public void setFileToPrint(File fileToPrint) {
        this.fileToPrint = fileToPrint;
    }
}
