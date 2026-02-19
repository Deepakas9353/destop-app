package com.printapp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating QR codes dynamically.
 * Uses ZXing library — works fully offline with no backend dependency.
 */
public class QrCodeService {

    private static final String BASE_MOBILE_URL = "https://deepakas9353.github.io/Mobile-web-app/"; // Hosted mobile app
                                                                                                    // URL
    private static final int DEFAULT_SIZE = 300;

    // Dark navy foreground for a premium look
    private static final int QR_FOREGROUND = 0xFF1E293B;
    private static final int QR_BACKGROUND = 0xFFFFFFFF;

    /**
     * Generates a QR code as a JavaFX WritableImage for display in the UI.
     *
     * @param printerId optional printer ID to append as query param
     * @return WritableImage containing the QR code
     */
    public WritableImage generateQrImage(String printerId) {
        return generateQrImage(printerId, DEFAULT_SIZE);
    }

    /**
     * Generates a QR code as a JavaFX WritableImage with custom size.
     */
    public WritableImage generateQrImage(String printerId, int size) {
        String url = buildUrl(printerId);
        try {
            BitMatrix bitMatrix = createBitMatrix(url, size);
            return toWritableImage(bitMatrix);
        } catch (WriterException e) {
            System.err.println("[QrCodeService] Failed to generate QR code: " + e.getMessage());
            return createErrorImage(size);
        }
    }

    /**
     * Saves the QR code as a PNG file for download.
     *
     * @param printerId optional printer ID
     * @param file      target file to save
     * @return true if saved successfully
     */
    public boolean saveQrAsPng(String printerId, File file) {
        return saveQrAsPng(printerId, file, DEFAULT_SIZE);
    }

    /**
     * Saves the QR code as a PNG file with custom size.
     */
    public boolean saveQrAsPng(String printerId, File file, int size) {
        String url = buildUrl(printerId);
        try {
            BitMatrix bitMatrix = createBitMatrix(url, size);
            BufferedImage bufferedImage = toBufferedImage(bitMatrix);
            ImageIO.write(bufferedImage, "PNG", file);
            System.out.println("[QrCodeService] QR code saved to: " + file.getAbsolutePath());
            return true;
        } catch (WriterException | IOException e) {
            System.err.println("[QrCodeService] Failed to save QR code: " + e.getMessage());
            return false;
        }
    }

    /**
     * Builds the mobile web URL with optional printer ID.
     */
    private String buildUrl(String printerId) {
        if (printerId != null && !printerId.trim().isEmpty()) {
            return BASE_MOBILE_URL + "?printerId=" + printerId.trim();
        }
        return BASE_MOBILE_URL;
    }

    /**
     * Creates a ZXing BitMatrix for the given URL.
     */
    private BitMatrix createBitMatrix(String url, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        return writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
    }

    /**
     * Converts BitMatrix to a JavaFX WritableImage.
     */
    private WritableImage toWritableImage(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        Color fg = Color.web("#1E293B");
        Color bg = Color.WHITE;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? fg : bg);
            }
        }
        return image;
    }

    /**
     * Converts BitMatrix to a BufferedImage for PNG export.
     */
    private BufferedImage toBufferedImage(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? QR_FOREGROUND : QR_BACKGROUND);
            }
        }
        return image;
    }

    /**
     * Creates a simple error placeholder image when QR generation fails.
     */
    private WritableImage createErrorImage(int size) {
        WritableImage image = new WritableImage(size, size);
        PixelWriter pixelWriter = image.getPixelWriter();
        Color errorBg = Color.web("#FEE2E2");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                pixelWriter.setColor(x, y, errorBg);
            }
        }
        return image;
    }
}
