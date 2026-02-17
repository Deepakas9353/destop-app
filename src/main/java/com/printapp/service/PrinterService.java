package com.printapp.service;

import com.printapp.model.PrintConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;

public class PrinterService {

    // =============================
    // Get Available Printers
    // =============================
    public List<String> getAvailablePrinters() {

        List<String> printerNames = new ArrayList<>();

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService service : printServices) {
            printerNames.add(service.getName());
        }

        if (printerNames.isEmpty()) {
            printerNames.add("No Printers Found");
        }

        return printerNames;
    }

    // =============================
    // Main Print Method
    // =============================
    public void print(PrintConfig config) throws Exception {

        if (config.getFileToPrint() == null) {
            throw new Exception("No file selected for printing.");
        }

        String printerName = config.getSelectedPrinter();
        PrintService selectedService = findPrinter(printerName);

        if (selectedService == null) {
            throw new Exception("Selected printer not found.");
        }

        File file = config.getFileToPrint();
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            printPdf(file, selectedService, config);
        } else if (fileName.matches(".*\\.(png|jpg|jpeg)$")) {
            printImage(file, selectedService, config);
        } else {
            throw new Exception("Unsupported file format. Only PDF and Images supported.");
        }
    }

    // =============================
    // Find Printer Helper
    // =============================
    private PrintService findPrinter(String printerName) {

        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }

        return null;
    }

    // =============================
    // PDF Printing
    // =============================
    private void printPdf(File file,
            PrintService service,
            PrintConfig config) throws Exception {

        try (PDDocument document = PDDocument.load(file)) {

            PrinterJob job = PrinterJob.getPrinterJob();

            job.setPrintService(service);
            job.setJobName("Direct Print - " + file.getName());

            // Copies
            job.setCopies(config.getCopies());

            PDFPrintable printable = new PDFPrintable(document, Scaling.SHRINK_TO_FIT);

            job.setPrintable(printable);

            // Color Mode
            PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
            if ("Black & White".equals(config.getColorMode())) {
                attr.add(Chromaticity.MONOCHROME);
            } else if ("Color".equals(config.getColorMode())) {
                attr.add(Chromaticity.COLOR);
            }

            // Silent Print
            job.print(attr);
        }
    }

    // =============================
    // Image Printing
    // =============================
    private void printImage(File file,
            PrintService service,
            PrintConfig config) throws Exception {

        PrinterJob job = PrinterJob.getPrinterJob();

        job.setPrintService(service);
        job.setCopies(config.getCopies());

        job.setPrintable((graphics, pageFormat, pageIndex) -> {

            if (pageIndex > 0)
                return Printable.NO_SUCH_PAGE;

            try {
                Image image = ImageIO.read(file);

                Graphics2D g2d = (Graphics2D) graphics;

                double x = pageFormat.getImageableX();
                double y = pageFormat.getImageableY();

                double width = pageFormat.getImageableWidth();
                double height = pageFormat.getImageableHeight();

                g2d.drawImage(image,
                        (int) x,
                        (int) y,
                        (int) width,
                        (int) height,
                        null);

                return Printable.PAGE_EXISTS;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Color Mode
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        if ("Black & White".equals(config.getColorMode())) {
            attr.add(Chromaticity.MONOCHROME);
        } else if ("Color".equals(config.getColorMode())) {
            attr.add(Chromaticity.COLOR);
        }

        job.print(attr);
    }
}
