package com.tdm.deduplication.runner;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.CsvReportService;
import com.tdm.deduplication.service.DeduplicationService;
import com.tdm.deduplication.service.ImageScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication(scanBasePackages = "com.tdm.deduplication")
public class DeduplicationApplication implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(DeduplicationApplication.class);

    @Autowired
    private ImageScannerService imageScanService;

    @Autowired
    private DeduplicationService deduplicationService;

    @Autowired
    private CsvReportService csvReportService;

    public static void main(String[] args) {
        SpringApplication.run(DeduplicationApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("Uso: java -jar app.jar <directory_immagini>");
            return;
        }

        String inputDirectory = args[0];
        String outputCsv = "deduplication-report.csv";

        List<ImageModel> images = imageScanService.scan(inputDirectory);
        System.out.println("Immagini trovate: " + images.size());
        images.forEach(image -> logger.info("Immagine: {}", image));

        List<DuplicateGroup> groups = deduplicationService.findDuplicates(images);
        System.out.println("Gruppi duplicati trovati: " + groups.size());

        csvReportService.writeReport(groups, outputCsv);
        System.out.println("Report scritto in: " + outputCsv);
    }
}