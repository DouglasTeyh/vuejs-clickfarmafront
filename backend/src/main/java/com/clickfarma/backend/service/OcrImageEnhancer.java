package com.clickfarma.backend.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * Utilitário para melhorar imagens para OCR (contraste, escala de cinza, limpeza).
 */
public class OcrImageEnhancer {

    public static BufferedImage decodeBase64(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    public static String encodePngBase64(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static BufferedImage enhanceForHandwritingOcr(BufferedImage original) {
        if (original == null) return null;
        BufferedImage gray = toGrayscale(original);
        return applyContrast(gray, 1.5f, 10.0f);
    }

    public static BufferedImage toGrayscale(BufferedImage original) {
        BufferedImage gray = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return gray;
    }

    public static BufferedImage applyContrast(BufferedImage image, float scale, float offset) {
        BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = (int) (r * scale + offset);
                g = (int) (g * scale + offset);
                b = (int) (b * scale + offset);

                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                res.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return res;
    }

    public static BufferedImage normalizeToRgbWhiteBackground(BufferedImage original) {
        BufferedImage res = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = res.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, original.getWidth(), original.getHeight());
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return res;
    }

    public static BufferedImage cropForPrescription(BufferedImage image, float bottomFraction) {
        int newHeight = (int) (image.getHeight() * bottomFraction);
        if (newHeight <= 0) return image;
        return image.getSubimage(0, 0, image.getWidth(), newHeight);
    }

    public static BufferedImage adaptiveMeanBinary(BufferedImage gray, int blockSize, int constant) {
        // Implementação simplificada de threshold adaptativo (média local)
        BufferedImage res = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int x = 0; x < gray.getWidth(); x++) {
            for (int y = 0; y < gray.getHeight(); y++) {
                int sum = 0;
                int count = 0;
                for (int dx = -blockSize/2; dx <= blockSize/2; dx++) {
                    for (int dy = -blockSize/2; dy <= blockSize/2; dy++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < gray.getWidth() && ny >= 0 && ny < gray.getHeight()) {
                            sum += (gray.getRGB(nx, ny) & 0xFF);
                            count++;
                        }
                    }
                }
                int mean = sum / count;
                int pixel = (gray.getRGB(x, y) & 0xFF);
                if (pixel < mean - constant) {
                    res.setRGB(x, y, 0); // Black
                } else {
                    res.setRGB(x, y, 0xFFFFFF); // White
                }
            }
        }
        return res;
    }

    public static BufferedImage upscaleByFactor(BufferedImage image, int factor) {
        int w = image.getWidth() * factor;
        int h = image.getHeight() * factor;
        BufferedImage res = new BufferedImage(w, h, image.getType());
        Graphics2D g = res.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        return res;
    }

    public static BufferedImage toBinary(BufferedImage gray) {
        BufferedImage res = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = res.createGraphics();
        g.drawImage(gray, 0, 0, null);
        g.dispose();
        return res;
    }
}
