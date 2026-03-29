package com.tdm.deduplication;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.DeduplicationService;
import com.tdm.deduplication.service.impl.DeduplicationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicationServiceTest {

    private final DeduplicationService deduplicationService = new DeduplicationServiceImpl();

    @Test
    void testFindDuplicates_WithIdenticalImages(@TempDir Path tempDir) throws IOException {
        File img1 = tempDir.resolve("originale.png").toFile();
        File img2 = tempDir.resolve("copia_identica.png").toFile();

        createNoiseImage(img1, 100);
        createNoiseImage(img2, 100);

        List<ImageModel> records = Arrays.asList(
                new ImageModel(img1.toPath()),
                new ImageModel(img2.toPath())
        );

        List<DuplicateGroup> groups = deduplicationService.findDuplicates(records);

        assertEquals(1, groups.size(), "Dovrebbe trovare esattamente 1 gruppo di duplicati");

        DuplicateGroup group = groups.get(0);
        assertEquals(2, group.getImages().size(), "Il gruppo dovrebbe contenere 2 immagini");

        for (ImageModel image : group.getImages()) {
            assertNotNull(image.getGroupId(), "Ogni immagine del gruppo deve avere un groupId");
            assertNotNull(image.getDifferentialSignature(), "Ogni immagine deve avere una firma differenziale");
        }

        long zeroDistanceCount = group.getImages().stream()
                .filter(image -> image.getBestDistance() == 0.0)
                .count();

        assertTrue(zeroDistanceCount >= 1, "Almeno un'immagine del gruppo deve avere distanza 0");
    }


    @Test
    void testFindDuplicates_DoesNotCreateResizedFilesNextToOriginals(@TempDir Path tempDir) throws IOException {
        File img1 = tempDir.resolve("originale.png").toFile();
        File img2 = tempDir.resolve("copia.png").toFile();

        createNoiseImage(img1, 555);
        createNoiseImage(img2, 555);

        long filesBefore;
        try (var files = Files.list(tempDir)) {
            filesBefore = files.count();
        }

        List<ImageModel> records = Arrays.asList(
                new ImageModel(img1.toPath()),
                new ImageModel(img2.toPath())
        );

        deduplicationService.findDuplicates(records);

        long filesAfter;
        try (var files = Files.list(tempDir)) {
            filesAfter = files.count();
        }

        assertEquals(filesBefore, filesAfter,
                "Il servizio non deve creare copie ridimensionate nella cartella delle immagini originali");
    }


    @Test
    void testFindDuplicates_WithCompletelyDifferentImages(@TempDir Path tempDir) throws IOException {
        File img1 = tempDir.resolve("foto_A.png").toFile();
        File img2 = tempDir.resolve("foto_B.png").toFile();

        createNoiseImage(img1, 123);
        createNoiseImage(img2, 999);

        List<ImageModel> records = Arrays.asList(
                new ImageModel(img1.toPath()),
                new ImageModel(img2.toPath())
        );

        List<DuplicateGroup> groups = deduplicationService.findDuplicates(records);

        assertTrue(groups.isEmpty(), "Non dovrebbe creare nessun gruppo se le immagini sono diverse");
    }

    @Test
    void testFindDuplicates_ThrowsExceptionOnInvalidFile(@TempDir Path tempDir) throws IOException {
        File textFile = tempDir.resolve("falso.jpg").toFile();
        final boolean isFileCreated = textFile.createNewFile();

        if (isFileCreated){
            List<ImageModel> records = List.of(new ImageModel(textFile.toPath()));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    deduplicationService.findDuplicates(records)
            );

            assertTrue(
                    exception.getMessage().contains("Errore nella generazione delle caratteristiche"),
                    "Il messaggio deve indicare un errore nella generazione delle caratteristiche"
            );
        }
    }

    private void createNoiseImage(File file, long seed) throws IOException {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(seed);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Color color = new Color(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256)
                );
                image.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(image, "png", file);
    }
}