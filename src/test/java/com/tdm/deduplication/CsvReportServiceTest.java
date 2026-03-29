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
        ImageModel exactDuplicate1 = new ImageModel(Path.of("image1.jpg"));
        exactDuplicate1.setFileSize(1000);
        exactDuplicate1.setWidth(800);
        exactDuplicate1.setHeight(600);
        exactDuplicate1.setFileFormat("JPG");
        exactDuplicate1.setOriginalDate(LocalDateTime.now());
        exactDuplicate1.setGroupId("GROUP_1");
        exactDuplicate1.setBestDistance(0.0);
        exactDuplicate1.setLuminanceSignature(new double[]{10.0, 20.0, 30.0, 40.0});
        exactDuplicate1.setChrominanceCbSignature(new double[]{1.0, 2.0});
        exactDuplicate1.setChrominanceCrSignature(new double[]{3.0, 4.0});

        ImageModel exactDuplicate2 = new ImageModel(Path.of("image2.jpg"));
        exactDuplicate2.setFileSize(1200);
        exactDuplicate2.setWidth(800);
        exactDuplicate2.setHeight(600);
        exactDuplicate2.setFileFormat("JPG");
        exactDuplicate2.setOriginalDate(LocalDateTime.now());
        exactDuplicate2.setGroupId("GROUP_1");
        exactDuplicate2.setBestDistance(0.0);
        exactDuplicate2.setLuminanceSignature(new double[]{10.0, 20.0, 30.0, 40.0});
        exactDuplicate2.setChrominanceCbSignature(new double[]{1.0, 2.0});
        exactDuplicate2.setChrominanceCrSignature(new double[]{3.0, 4.0});

        ImageModel nearReference = new ImageModel(Path.of("image3.jpg"));
        nearReference.setFileSize(1250);
        nearReference.setWidth(800);
        nearReference.setHeight(600);
        nearReference.setFileFormat("JPG");
        nearReference.setOriginalDate(LocalDateTime.now());
        nearReference.setGroupId("GROUP_2");
        nearReference.setBestDistance(0.0);
        nearReference.setLuminanceSignature(new double[]{50.0, 60.0, 70.0, 80.0});
        nearReference.setChrominanceCbSignature(new double[]{5.0, 6.0});
        nearReference.setChrominanceCrSignature(new double[]{7.0, 8.0});

        ImageModel nearDuplicate = new ImageModel(Path.of("image4.jpg"));
        nearDuplicate.setFileSize(1300);
        nearDuplicate.setWidth(800);
        nearDuplicate.setHeight(600);
        nearDuplicate.setFileFormat("JPG");
        nearDuplicate.setOriginalDate(LocalDateTime.now());
        nearDuplicate.setGroupId("GROUP_2");
        nearDuplicate.setBestDistance(10.0);
        nearDuplicate.setLuminanceSignature(new double[]{52.0, 62.0, 72.0, 82.0});
        nearDuplicate.setChrominanceCbSignature(new double[]{5.8, 6.8});
        nearDuplicate.setChrominanceCrSignature(new double[]{7.8, 8.8});

        ImageModel uniqueImage = new ImageModel(Path.of("image5.png"));
        uniqueImage.setFileSize(900);
        uniqueImage.setWidth(640);
        uniqueImage.setHeight(480);
        uniqueImage.setFileFormat("PNG");
        uniqueImage.setOriginalDate(LocalDateTime.now());

        Path outputPath = Path.of("test-output.csv");

        csvReportService.writeReport(
                List.of(exactDuplicate1, exactDuplicate2, nearReference, nearDuplicate, uniqueImage),
                outputPath.toString()
        );

        assertTrue(Files.exists(outputPath));

        String csvContent = Files.readString(outputPath);

        assertTrue(csvContent.contains("match_type"));
        assertTrue(csvContent.contains("EXACT_DUPLICATE"));
        assertTrue(csvContent.contains("NEAR_DUPLICATE"));
        assertTrue(csvContent.contains("UNIQUE"));

        Files.deleteIfExists(outputPath);
    }
}