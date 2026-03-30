package com.tdm.deduplication.service.impl;

import com.opencsv.CSVWriter;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.CsvReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CsvReportServiceImpl implements CsvReportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReportServiceImpl.class);

    private static final double EXACT_LUMINANCE_MSE_THRESHOLD = 1.0;
    private static final double EXACT_CHROMA_DISTANCE_THRESHOLD = 1.0;

    @Override
    public void writeReport(List<ImageModel> images, String outputPath) {
        Map<String, List<ImageModel>> imagesByGroup = images.stream()
                .filter(image -> image.getGroupId() != null && !image.getGroupId().isBlank())
                .collect(Collectors.groupingBy(ImageModel::getGroupId));

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            writer.writeNext(new String[]{
                    "group_id",
                    "match_type",
                    "file_path",
                    "file_size",
                    "width",
                    "height",
                    "format",
                    "original_date",
                    "exif_metadata",
                    "distance"
            });

            for (ImageModel image : images) {
                writer.writeNext(new String[]{
                        image.getGroupId() != null ? image.getGroupId() : "",
                        determineMatchType(image, imagesByGroup),
                        image.getFilePath().toString(),
                        String.valueOf(image.getFileSize()),
                        String.valueOf(image.getWidth()),
                        String.valueOf(image.getHeight()),
                        image.getFileFormat(),
                        image.getOriginalDate() != null ? image.getOriginalDate().toString() : "",
                        serializeExifMetadata(image),
                        image.getGroupId() != null ? String.valueOf(image.getBestDistance()) : ""
                });
            }

        } catch (IOException e) {
            logger.error("Errore nella scrittura del CSV", e);
            throw new RuntimeException("Errore nella scrittura del CSV", e);
        }
    }

    private String determineMatchType(ImageModel image, Map<String, List<ImageModel>> imagesByGroup) {
        if (image.getGroupId() == null || image.getGroupId().isBlank()) {
            return "UNIQUE";
        }

        List<ImageModel> groupImages = imagesByGroup.get(image.getGroupId());

        if (groupImages == null || groupImages.size() <= 1) {
            return "UNIQUE";
        }

        boolean hasExactCompanion = groupImages.stream()
                .filter(other -> other != image)
                .anyMatch(other -> isExactDuplicate(image, other));

        if (hasExactCompanion) {
            return "EXACT_DUPLICATE";
        }

        return "NEAR_DUPLICATE";
    }

    private boolean isExactDuplicate(ImageModel first, ImageModel second) {
        if (first.getLuminanceSignature() == null || second.getLuminanceSignature() == null) {
            return false;
        }

        if (first.getChrominanceCbSignature() == null || second.getChrominanceCbSignature() == null) {
            return false;
        }

        if (first.getChrominanceCrSignature() == null || second.getChrominanceCrSignature() == null) {
            return false;
        }

        double luminanceMse = computeMeanSquaredError(
                first.getLuminanceSignature(),
                second.getLuminanceSignature()
        );

        double cbDistance = averageVectorDistance(
                first.getChrominanceCbSignature(),
                second.getChrominanceCbSignature()
        );

        double crDistance = averageVectorDistance(
                first.getChrominanceCrSignature(),
                second.getChrominanceCrSignature()
        );

        return luminanceMse <= EXACT_LUMINANCE_MSE_THRESHOLD
                && cbDistance <= EXACT_CHROMA_DISTANCE_THRESHOLD
                && crDistance <= EXACT_CHROMA_DISTANCE_THRESHOLD;
    }

    private double computeMeanSquaredError(double[] first, double[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Le firme di luminanza devono avere la stessa lunghezza");
        }

        double sum = 0.0;

        for (int i = 0; i < first.length; i++) {
            double difference = first[i] - second[i];
            sum += difference * difference;
        }

        return sum / first.length;
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

    private String serializeExifMetadata(ImageModel image) {
        if (image.getExifMetadata() == null || image.getExifMetadata().isEmpty()) {
            return "";
        }

        return image.getExifMetadata()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + " | " + right)
                .orElse("");
    }
}