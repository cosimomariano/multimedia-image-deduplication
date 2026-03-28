package com.tdm.deduplication;

import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.ExifExtractionService;
import com.tdm.deduplication.service.impl.ExifExtractionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExifExtractionServiceImplTest {

    private final ExifExtractionService exifExtractionService = new ExifExtractionServiceImpl();

    @TempDir
    Path tempDir;

    @Test
    void shouldInitializeEmptyExifMetadataWhenImageHasNoExif() throws Exception {
        Path imagePath = tempDir.resolve("test-no-exif.png");
        createTestPng(imagePath, 32, 32);

        ImageModel imageModel = new ImageModel();

        exifExtractionService.populateMetadata(imagePath, imageModel);

        assertNotNull(imageModel.getExifMetadata());
        assertTrue(imageModel.getExifMetadata().isEmpty());
        assertNull(imageModel.getOriginalDate());
    }

    @Test
    void shouldHandleNonImageFileGracefully() throws Exception {
        Path invalidFile = tempDir.resolve("not-an-image.jpg");
        Files.writeString(invalidFile, "this is not a valid image");

        ImageModel imageModel = new ImageModel();

        assertDoesNotThrow(() -> exifExtractionService.populateMetadata(invalidFile, imageModel));

        assertNotNull(imageModel.getExifMetadata());
        assertTrue(imageModel.getExifMetadata().isEmpty());
        assertNull(imageModel.getOriginalDate());
    }

    @Test
    void shouldHandleMissingFileGracefully() {
        Path missingFile = tempDir.resolve("missing.jpg");
        ImageModel imageModel = new ImageModel();

        assertDoesNotThrow(() -> exifExtractionService.populateMetadata(missingFile, imageModel));

        assertNotNull(imageModel.getExifMetadata());
        assertTrue(imageModel.getExifMetadata().isEmpty());
        assertNull(imageModel.getOriginalDate());
    }

    private void createTestPng(Path outputPath, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", outputPath.toFile());
    }
}