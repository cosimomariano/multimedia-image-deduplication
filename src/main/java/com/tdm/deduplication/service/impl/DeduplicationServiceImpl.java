package com.tdm.deduplication.service.impl;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.DeduplicationService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeduplicationServiceImpl implements DeduplicationService {

    // Costanti per gestire la componente della luminanza/generali
    private static final int NORMALIZED_WIDTH = 64;
    private static final int NORMALIZED_HEIGHT = 64;
    private static final int HAMMING_THRESHOLD = 200;

    // Costanti per gestire la componente cromatica
    private static final int CHROMA_GRID_ROWS = 8;
    private static final int CHROMA_GRID_COLS = 8;
    private static final double CHROMA_DISTANCE_THRESHOLD = 20.0;

    @Override
    public List<DuplicateGroup> findDuplicates(List<ImageModel> images) {
        for (ImageModel image : images) {
            extractFeatures(image);
        }

        List<DuplicateGroup> groups = new ArrayList<>();
        int groupCounter = 1;

        for (ImageModel current : images) {
            if (current.getGroupId() != null) {
                continue;
            }

            String groupId = "GROUP_" + groupCounter++;
            DuplicateGroup group = new DuplicateGroup(groupId);

            current.setGroupId(groupId);
            current.setBestDistance(0);
            group.addImage(current);

            for (ImageModel candidate : images) {
                if (candidate == current || candidate.getGroupId() != null) {
                    continue;
                }

                int luminanceDistance = hammingDistance(
                        current.getDifferentialSignature(),
                        candidate.getDifferentialSignature()
                );

                if (luminanceDistance <= HAMMING_THRESHOLD && isChrominanceSimilar(current, candidate)) {
                    candidate.setGroupId(groupId);
                    candidate.setBestDistance(luminanceDistance);
                    group.addImage(candidate);
                }
            }

            if (group.getImages().size() > 1) {
                groups.add(group);
            }
        }

        return groups;
    }

    private void extractFeatures(ImageModel image) {
        try {
            BufferedImage original = ImageIO.read(image.getFilePath().toFile());

            if (original == null) {
                throw new IllegalStateException("Immagine non leggibile: " + image.getFilePath());
            }

            BufferedImage resized = resize(original, NORMALIZED_WIDTH, NORMALIZED_HEIGHT);

            int height = resized.getHeight();
            int width = resized.getWidth();

            int[][] luminanceMatrix = new int[height][width];
            double[][] cbMatrix = new double[height][width];
            double[][] crMatrix = new double[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = new Color(resized.getRGB(x, y));

                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();

                    int yValue = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    double cbValue = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
                    double crValue = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;

                    luminanceMatrix[y][x] = yValue;
                    cbMatrix[y][x] = cbValue;
                    crMatrix[y][x] = crValue;
                }
            }

            image.setDifferentialSignature(buildDifferentialBinarySignature(luminanceMatrix));
            image.setChrominanceCbSignature(buildBlockChrominanceSignature(cbMatrix));
            image.setChrominanceCrSignature(buildBlockChrominanceSignature(crMatrix));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Errore nella generazione delle caratteristiche per " + image.getFilePath(),
                    e
            );
        }
    }

    private double[] buildBlockChrominanceSignature(double[][] chromaMatrix) {
        int blockHeight = chromaMatrix.length / CHROMA_GRID_ROWS;
        int blockWidth = chromaMatrix[0].length / CHROMA_GRID_COLS;

        double[] signature = new double[CHROMA_GRID_ROWS * CHROMA_GRID_COLS];
        int index = 0;

        for (int blockRow = 0; blockRow < CHROMA_GRID_ROWS; blockRow++) {
            for (int blockCol = 0; blockCol < CHROMA_GRID_COLS; blockCol++) {
                double sum = 0.0;
                int count = 0;

                int startY = blockRow * blockHeight;
                int endY = startY + blockHeight;
                int startX = blockCol * blockWidth;
                int endX = startX + blockWidth;

                for (int y = startY; y < endY; y++) {
                    for (int x = startX; x < endX; x++) {
                        sum += chromaMatrix[y][x];
                        count++;
                    }
                }

                signature[index++] = sum / count;
            }
        }

        return signature;
    }

    private boolean isChrominanceSimilar(ImageModel first, ImageModel second) {
        double cbDistance = averageVectorDistance(
                first.getChrominanceCbSignature(),
                second.getChrominanceCbSignature()
        );

        double crDistance = averageVectorDistance(
                first.getChrominanceCrSignature(),
                second.getChrominanceCrSignature()
        );

        return cbDistance <= CHROMA_DISTANCE_THRESHOLD
                && crDistance <= CHROMA_DISTANCE_THRESHOLD;
    }

    private double averageVectorDistance(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Le firme cromatiche devono avere la stessa lunghezza");
        }

        double sum = 0.0;

        for (int i = 0; i < first.length; i++) {
            sum += Math.abs(first[i] - second[i]);
        }

        return sum / first.length;
    }

    private BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();

        return resized;
    }

    private String buildDifferentialBinarySignature(int[][] luminanceMatrix) {
        int height = luminanceMatrix.length;
        int width = luminanceMatrix[0].length;

        StringBuilder signature = new StringBuilder(height * (width - 1));

        for (int y = 0; y < height; y++) {
            for (int x = 1; x < width; x++) {
                int predicted = luminanceMatrix[y][x - 1];
                int residual = luminanceMatrix[y][x] - predicted;

                if (residual > 0) {
                    signature.append('1');
                } else {
                    signature.append('0');
                }
            }
        }

        return signature.toString();
    }

    private int hammingDistance(String first, String second) {
        if (first.length() != second.length()) {
            throw new IllegalArgumentException("Le firme devono avere la stessa lunghezza");
        }

        int distance = 0;

        for (int i = 0; i < first.length(); i++) {
            if (first.charAt(i) != second.charAt(i)) {
                distance++;
            }
        }

        return distance;
    }
}