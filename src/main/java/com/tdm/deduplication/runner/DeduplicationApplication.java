package com.tdm.deduplication.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.tdm.deduplication.service.ImageScannerService;

@SpringBootApplication(scanBasePackages = "com.tdm.deduplication")
public class DeduplicationApplication implements CommandLineRunner {

    @Autowired
    private ImageScannerService imageScanService;

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

        var images = imageScanService.scan(inputDirectory);

        System.out.println("Scansione completata. Immagini valide trovate: " + images.size());
    }
}