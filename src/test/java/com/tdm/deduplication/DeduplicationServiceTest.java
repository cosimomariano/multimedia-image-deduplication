package com.tdm.deduplication;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.DeduplicationService;
import com.tdm.deduplication.service.impl.DeduplicationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicationServiceTest {

    // Istanziamo il servizio da testare. Non avendo dipendenze esterne (es. database), 
    // possiamo usare una semplice "new" senza caricare il contesto di Spring.
    private final DeduplicationService deduplicationService = new DeduplicationServiceImpl();

    @Test
    void testFindDuplicates_WithIdenticalImages(@TempDir Path tempDir) throws IOException {
        // 1. SETUP: Creiamo due immagini fisiche con lo stesso esatto pattern visivo (seed = 100)
        File img1 = tempDir.resolve("originale.png").toFile();
        File img2 = tempDir.resolve("copia_identica.png").toFile();

        createNoiseImage(img1, 100);
        createNoiseImage(img2, 100);

        List<ImageModel> records = Arrays.asList(
                new ImageModel(img1.toPath()),
                new ImageModel(img2.toPath())
        );

        // 2. ESECUZIONE
        List<DuplicateGroup> groups = deduplicationService.findDuplicates(records);

        // 3. VERIFICA
        assertEquals(1, groups.size(), "Dovrebbe trovare esattamente 1 gruppo di duplicati");

        DuplicateGroup group = groups.get(0);
        assertEquals(2, group.getImages().size(), "Il gruppo dovrebbe contenere 2 immagini");

        // Verifica che la distanza di Hamming tra due immagini identiche sia 0
        ImageModel matchedImage = group.getImages().get(1);
        assertEquals(0.0, matchedImage.getBestDistance(), "La distanza tra immagini identiche deve essere 0");
    }

    @Test
    void testFindDuplicates_WithCompletelyDifferentImages(@TempDir Path tempDir) throws IOException {
        // 1. SETUP: Creiamo due immagini con pattern completamente diversi (seed diversi)
        File img1 = tempDir.resolve("foto_A.png").toFile();
        File img2 = tempDir.resolve("foto_B.png").toFile();

        createNoiseImage(img1, 123); // Pattern A
        createNoiseImage(img2, 999); // Pattern B (Molto diverso)

        List<ImageModel> records = Arrays.asList(
                new ImageModel(img1.toPath()),
                new ImageModel(img2.toPath())
        );

        // 2. ESECUZIONE
        List<DuplicateGroup> groups = deduplicationService.findDuplicates(records);

        // 3. VERIFICA
        assertTrue(groups.isEmpty(), "Non dovrebbe creare nessun gruppo se le immagini sono diverse");
    }

    @Test
    void testFindDuplicates_ThrowsExceptionOnInvalidFile(@TempDir Path tempDir) throws IOException {
        // 1. SETUP: Creiamo un file di testo fittizio spacciandolo per immagine
        File textFile = tempDir.resolve("falso.jpg").toFile();
        textFile.createNewFile(); // File vuoto, ImageIO non riuscirà a leggerlo

        List<ImageModel> records = List.of(new ImageModel(textFile.toPath()));

        // 2 & 3. ESECUZIONE e VERIFICA
        // Ci aspettiamo che il servizio lanci un'eccezione quando cerca di processare il file
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deduplicationService.findDuplicates(records);
        });

        assertTrue(exception.getMessage().contains("Errore nella generazione della firma"));
    }

    /**
     * Metodo di supporto: crea un'immagine con pattern di rumore casuale ma predicibile (tramite seed).
     * Questo è fondamentale perché l'algoritmo di deduplicazione (Modulo 4) si basa sui gradienti orizzontali.
     * Con un'immagine a tinta unita (es. tutta nera), il gradiente sarebbe sempre 0.
     */
    private void createNoiseImage(File file, long seed) throws IOException {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(seed);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Generiamo un colore RGB casuale
                Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                image.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(image, "png", file);
    }
}