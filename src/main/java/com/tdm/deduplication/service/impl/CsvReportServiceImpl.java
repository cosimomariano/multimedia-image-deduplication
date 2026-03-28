package com.tdm.deduplication.service.impl;

import com.opencsv.CSVWriter;
import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.CsvReportService;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class CsvReportServiceImpl implements CsvReportService {

    @Override
    public void writeReport(List<DuplicateGroup> groups, String outputPath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            writer.writeNext(new String[]{
                    "group_id",
                    "file_path",
                    "file_size",
                    "width",
                    "height",
                    "format",
                    "original_date",
                    "exif_metadata",
                    "distance"
            });

            for (DuplicateGroup group : groups) {
                for (ImageModel image : group.getImages()) {
                    writer.writeNext(new String[]{
                            group.getGroupId(),
                            image.getFilePath().toString(),
                            String.valueOf(image.getFileSize()),
                            String.valueOf(image.getWidth()),
                            String.valueOf(image.getHeight()),
                            image.getFileFormat(),
                            image.getOriginalDate() != null ? image.getOriginalDate().toString() : "",
                            serializeExifMetadata(image),
                            String.valueOf(image.getBestDistance())
                    });
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Errore nella scrittura del CSV", e);
        }
    }

    // METODI PRIVATI

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