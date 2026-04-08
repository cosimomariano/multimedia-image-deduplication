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

@Service
public class CsvReportServiceImpl implements CsvReportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReportServiceImpl.class);

    @Override
    public void writeReport(List<ImageModel> images, String outputPath) {
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
                        image.getMatchType() != null ? image.getMatchType() : "UNIQUE",
                        image.getFilePath().toString(),
                        String.valueOf(image.getFileSize()),
                        String.valueOf(image.getWidth()),
                        String.valueOf(image.getHeight()),
                        image.getFileFormat(),
                        image.getOriginalDate() != null ? image.getOriginalDate().toString() : "",
                        serializeExifMetadata(image),
                        image.getBestDistance() != null ? String.valueOf(image.getBestDistance()) : ""
                });
            }

        } catch (IOException e) {
            logger.error("Errore nella scrittura del CSV", e);
            throw new RuntimeException("Errore nella scrittura del CSV", e);
        }
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