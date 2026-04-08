package com.tdm.deduplication;

import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.ImageScannerService;
import com.tdm.deduplication.service.impl.ImageScannerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageScanServiceTest {

    private final ImageScannerService imageScanService = new ImageScannerServiceImpl();

    @Test
    void testDirectoryScanningAndFiltering(@TempDir Path tempDir) throws IOException {
        File img1 = tempDir.resolve("foto_vacanza.jpg").toFile();
        File img2 = tempDir.resolve("screenshot.PNG").toFile(); //Testo la resistenza al case-sensitive nelle estensioni dei file
        File txtFile = tempDir.resolve("appunti.txt").toFile();
        File subDir = tempDir.resolve("sottocartella").toFile();

        subDir.mkdir();
        File img3 = new File(subDir, "nascosta.jpeg"); // Testo la ricorsivita nel caso delle sottocartelle

        createDummyImage(img1, "jpg");
        createDummyImage(img2, "png");
        createDummyImage(img3, "jpeg");
        txtFile.createNewFile(); // Creo questo file per verificare che il sistema lo scarti essendo un formato testuale e non una immagine

        List<ImageModel> results = imageScanService.scan(tempDir.toString());

        assertNotNull(results, "La lista dei risultati non deve essere null");
        assertEquals(3, results.size(), "Deve trovare esattamente 3 immagini supportate, scartando il .txt");

        boolean hiddenImageFound = results.stream()
                .anyMatch(record -> record.getFilePath().toString().contains("nascosta"));
        assertTrue(hiddenImageFound, "La scansione ricorsiva deve trovare l'immagine nella sottocartella");
    }

    private void createDummyImage(File file, String format) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, format, file);
    }
}