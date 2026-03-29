package com.tdm.deduplication;

import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.CsvReportService;
import com.tdm.deduplication.service.impl.CsvReportServiceImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvReportServiceTest {

    private final CsvReportService csvReportService = new CsvReportServiceImpl();

    @Test
    void shouldWriteCsvFileWithAllImagesAndClassification() throws IOException {
        ImageModel exactDuplicate = new ImageModel(Path.of("image1.jpg"));
        exactDuplicate.setFileSize(1000);
        exactDuplicate.setWidth(800);
        exactDuplicate.setHeight(600);
        exactDuplicate.setFileFormat("JPG");
        exactDuplicate.setOriginalDate(LocalDateTime.now());
        exactDuplicate.setGroupId("GROUP_1");
        exactDuplicate.setBestDistance(0);

        ImageModel nearDuplicate = new ImageModel(Path.of("image2.jpg"));
        nearDuplicate.setFileSize(1200);
        nearDuplicate.setWidth(800);
        nearDuplicate.setHeight(600);
        nearDuplicate.setFileFormat("JPG");
        nearDuplicate.setOriginalDate(LocalDateTime.now());
        nearDuplicate.setGroupId("GROUP_1");
        nearDuplicate.setBestDistance(10);

        ImageModel uniqueImage = new ImageModel(Path.of("image3.jpg"));
        uniqueImage.setFileSize(900);
        uniqueImage.setWidth(640);
        uniqueImage.setHeight(480);
        uniqueImage.setFileFormat("PNG");
        uniqueImage.setOriginalDate(LocalDateTime.now());

        Path outputPath = Path.of("test-output.csv");

        csvReportService.writeReport(
                List.of(exactDuplicate, nearDuplicate, uniqueImage),
                outputPath.toString()
        );

        assertTrue(Files.exists(outputPath));

        String csvContent = Files.readString(outputPath);

        assertTrue(csvContent.contains("match_type"));
        assertTrue(csvContent.contains("EXACT_DUPLICATE"));
        assertTrue(csvContent.contains("NEAR_DUPLICATE"));
        assertTrue(csvContent.contains("UNIQUE"));
        assertTrue(csvContent.contains("image1.jpg"));
        assertTrue(csvContent.contains("image2.jpg"));
        assertTrue(csvContent.contains("image3.jpg"));

        Files.deleteIfExists(outputPath);
    }
}