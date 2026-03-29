package com.tdm.deduplication.service.impl;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.DeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeduplicationServiceImpl implements DeduplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DeduplicationServiceImpl.class);

    // Normalizzazione
    private static final int NORMALIZED_WIDTH = 64;
    private static final int NORMALIZED_HEIGHT = 64;

    // Similarità di luminanza
    private static final double LUMINANCE_MSE_THRESHOLD = 600.0;

    // Similarità cromatica
    private static final int CHROMA_GRID_ROWS = 8;
    private static final int CHROMA_GRID_COLS = 8;
    private static final double CHROMA_DISTANCE_THRESHOLD = 35.0;

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

            List<ImageModel> matchedImages = new ArrayList<>();
            List<Double> matchedDistances = new ArrayList<>();

            matchedImages.add(current);
            matchedDistances.add(0.0);

            for (ImageModel candidate : images) {
                if (candidate == current || candidate.getGroupId() != null) {
                    continue;
                }

                double luminanceMse = computeMeanSquaredError(
                        current.getLuminanceSignature(),
                        candidate.getLuminanceSignature()
                );

                double cbDistance = averageVectorDistance(
                        current.getChrominanceCbSignature(),
                        candidate.getChrominanceCbSignature()
                );

                double crDistance = averageVectorDistance(
                        current.getChrominanceCrSignature(),
                        candidate.getChrominanceCrSignature()
                );

                // Loggatura per verifica delle impostazioni di sogliatura
                logger.info(
                        "Confronto {} vs {} -> MSE: {}, Cb distance: {}, Cr distance: {}",
                        current.getFilePath().getFileName(),
                        candidate.getFilePath().getFileName(),
                        luminanceMse,
                        cbDistance,
                        crDistance
                );

                if (luminanceMse <= LUMINANCE_MSE_THRESHOLD
                        && cbDistance <= CHROMA_DISTANCE_THRESHOLD
                        && crDistance <= CHROMA_DISTANCE_THRESHOLD) {
                    matchedImages.add(candidate);
                    matchedDistances.add(luminanceMse);
                }
            }

            if (matchedImages.size() > 1) {
                String groupId = "GROUP_" + groupCounter++;
                DuplicateGroup group = new DuplicateGroup(groupId);

                for (int i = 0; i < matchedImages.size(); i++) {
                    ImageModel matched = matchedImages.get(i);
                    matched.setGroupId(groupId);
                    matched.setBestDistance(matchedDistances.get(i));
                    group.addImage(matched);
                }

                groups.add(group);
            }
        }

        return groups;
    }

    private void extractFeatures(ImageModel image) {
        Path temporaryResizedPath = null;

        try {
            BufferedImage original = ImageIO.read(image.getFilePath().toFile());

            if (original == null) {
                logger.error("Immagine non leggibile: {}", image.getFilePath());
                throw new IllegalArgumentException("Immagine non leggibile: " + image.getFilePath());
            }

            temporaryResizedPath = createTemporaryResizedCopy(original);
            BufferedImage resized = ImageIO.read(temporaryResizedPath.toFile());

            if (resized == null) {
                logger.error("Impossibile leggere la copia temporanea ridimensionata: {}", temporaryResizedPath);
                throw new IllegalStateException(
                        "Impossibile leggere la copia temporanea ridimensionata: " + temporaryResizedPath
                );
            }

            int height = resized.getHeight();
            int width = resized.getWidth();

            double[] luminanceSignature = new double[height * width];
            double[][] cbMatrix = new double[height][width];
            double[][] crMatrix = new double[height][width];

            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = new Color(resized.getRGB(x, y));

                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();

                    double yValue = 0.299 * r + 0.587 * g + 0.114 * b;
                    double cbValue = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
                    double crValue = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;

                    luminanceSignature[index++] = yValue;
                    cbMatrix[y][x] = cbValue;
                    crMatrix[y][x] = crValue;
                }
            }

            image.setLuminanceSignature(luminanceSignature);
            image.setChrominanceCbSignature(buildBlockChrominanceSignature(cbMatrix));
            image.setChrominanceCrSignature(buildBlockChrominanceSignature(crMatrix));

            // campi vecchi non più usati nel matching
            image.setDifferentialSignature(null);

        } catch (Exception e) {
            logger.error("Errore nella generazione delle caratteristiche", e);
            throw new RuntimeException(
                    "Errore nella generazione delle caratteristiche per " + image.getFilePath(),
                    e
            );
        } finally {
            deleteTemporaryFile(temporaryResizedPath);
        }
    }

    private Path createTemporaryResizedCopy(BufferedImage source) throws IOException {
        BufferedImage resized = resize(source);
        Path temporaryFile = Files.createTempFile("tdm-resized-", ".png");

        if (!ImageIO.write(resized, "png", temporaryFile.toFile())) {
            logger.error("Impossibile salvare la copia temporanea ridimensionata");
            throw new IOException("Impossibile salvare la copia temporanea ridimensionata");
        }

        return temporaryFile;
    }

    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
        }
    }

    private double computeMeanSquaredError(double[] first, double[] second) {
        if (first.length != second.length) {
            logger.error("Le firme di luminanza devono avere la stessa lunghezza");
            throw new IllegalArgumentException("Le firme di luminanza devono avere la stessa lunghezza");
        }

        double sum = 0.0;

        for (int i = 0; i < first.length; i++) {
            double difference = first[i] - second[i];
            sum += difference * difference;
        }

        return sum / first.length;
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
            logger.error("Le firme cromatiche devono avere la stessa lunghezza");
            throw new IllegalArgumentException("Le firme cromatiche devono avere la stessa lunghezza");
        }

        double sum = 0.0;

        for (int i = 0; i < first.length; i++) {
            sum += Math.abs(first[i] - second[i]);
        }

        return sum / first.length;
    }

    private BufferedImage resize(BufferedImage source) {
        BufferedImage resized = new BufferedImage(
                NORMALIZED_WIDTH,
                NORMALIZED_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        graphics.drawImage(source, 0, 0, NORMALIZED_WIDTH, NORMALIZED_HEIGHT, null);
        graphics.dispose();

        return resized;
    }
}