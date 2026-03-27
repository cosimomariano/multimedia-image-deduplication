package com.tdm.deduplication;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.CsvReportService;
import com.tdm.deduplication.service.impl.CsvReportServiceImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvReportServiceTest {

    private final CsvReportService csvReportService = new CsvReportServiceImpl();

    @Test
    void shouldWriteCsvFileWithDuplicateGroups() {
        ImageModel img1 = new ImageModel(Path.of("image1.jpg"));
        img1.setFileSize(1000);
        img1.setWidth(800);
        img1.setHeight(600);
        img1.setFileFormat("JPG");
        img1.setOriginalDate(LocalDateTime.now());
        img1.setBestDistance(0);

        ImageModel img2 = new ImageModel(Path.of("image2.jpg"));
        img2.setFileSize(1200);
        img2.setWidth(800);
        img2.setHeight(600);
        img2.setFileFormat("JPG");
        img2.setOriginalDate(LocalDateTime.now());
        img2.setBestDistance(10);

        DuplicateGroup group = new DuplicateGroup("GROUP_1");
        group.addImage(img1);
        group.addImage(img2);

        String outputPath = "test-output.csv";

        csvReportService.writeReport(List.of(group), outputPath);

        assertTrue(java.nio.file.Files.exists(Path.of(outputPath)));
    }
}