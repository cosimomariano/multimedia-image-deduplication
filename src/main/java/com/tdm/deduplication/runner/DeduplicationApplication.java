package com.tdm.deduplication.runner;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
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

    public static void main(String[] args) {
        SpringApplication.run(DeduplicationApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("_-^****************************************^-_");
        System.out.println("   *** Avvio Deduplicatore Immagini TDM ***   ");
        System.out.println("_-^****************************************^-_");

        if (args.length == 0) {
            System.err.println("Errore: Fornire il percorso della directory come argomento.");
            System.out.println("Uso: mvn spring-boot:run -Dspring-boot.run.arguments=\"/percorso/cartella\"");
            return;
        }

        String inputDirectory = args[0];
        System.out.println("Directory di input configurata: " + inputDirectory);
        System.out.println("Avvio scansione ed estrazione metadati EXIF...");

        List<ImageModel> imageList = imageScanService.scan(inputDirectory);
        System.out.println("Scansione completata. Immagini valide trovate: " + imageList.size());

        List<DuplicateGroup> groups = deduplicationService.findDuplicates(imageList);
        System.out.println("Gruppi duplicati trovati: " + groups.size());

        groups.forEach(group -> {
            group.getImages().forEach(image -> logger.info("Immagine duplicata trovata: {}", image));
        });
    }
}