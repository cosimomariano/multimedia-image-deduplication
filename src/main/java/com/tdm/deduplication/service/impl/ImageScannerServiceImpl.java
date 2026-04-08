package com.tdm.deduplication.service.impl;

import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.model.utility.SupportedExtensionsEnum;
import com.tdm.deduplication.service.ImageScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ImageScannerServiceImpl implements ImageScannerService {

    private final static Logger logger = LoggerFactory.getLogger(ImageScannerServiceImpl.class);

    @Override
    public List<ImageModel> scan(String directoryPath) {
        File startFolder = new File(directoryPath);

        // Controllo sulla validità della cartella fornita in input
        if (!startFolder.exists() || !startFolder.isDirectory()) {
            logger.error("Cartella non valida");
            throw new IllegalArgumentException("Directory non valida: " + directoryPath);
        }

        List<ImageModel> images = new ArrayList<>();
        scanDirectory(startFolder, images);

        return images;
    }


    // METODI PRIVATI

    /*
        Funzione ricorsiva che data una cartella in input istanzia tutti i model associati alle immagini fornite al sistema
     */
    private void scanDirectory(File folder, List<ImageModel> images) {
        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        Arrays.stream(files).forEach(file -> {
            // Gestisco il caso limite nel quale un utente carichi piu sottocartelle di immagini nella cartella di input
            if (file.isDirectory()) {
                scanDirectory(file, images);

            } else if (file.isFile()) {
                Path path = file.toPath();

                if (isSupportedImage(path)) {
                    ImageModel record = readImageModel(path);
                    if (record != null) {
                        images.add(record);
                        logger.info("Immagine trovata: {}", record);
                    }
                }
            }
        });
    }

    /*
        Controllo se l'estensione fa parte dei miei tipi di file censiti che il sistema riesce a gestire
        attraverso un trim della stringa dell'attributo fileName del Path in input
     */
    private boolean isSupportedImage(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return false;
        }

        String extension = fileName.substring(dotIndex + 1);

        return SupportedExtensionsEnum.isSupported(extension);
    }

    /*
        Istanzio il model dell'immagine a partire dalla lettura del path tramite la classe BufferedImage delle librerie standard di java
     */
    private ImageModel readImageModel(Path path) {
        try {
            BufferedImage image = readImage(path);

            if (Objects.isNull(image)) {
                logger.error("File non letto come immagine: {}", path);
                return null;
            }

            ImageModel record = new ImageModel(path);
            record.setFileSize(Files.size(path));
            record.setWidth(image.getWidth());
            record.setHeight(image.getHeight());
            record.setFileFormat(getExtension(path));

            return record;

        } catch (Exception e) {
            logger.error("Errore nella lettura di {}: {}", path, e.getMessage());
            return null;
        }
    }

    private BufferedImage readImage(Path path) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                logger.error("Immagine non supportata o file non valido: {}", path);
                throw new IOException("Formato immagine non supportato o file non valido: " + path);
            }

            return image;
        } catch (Exception e) {
            return null;
        }
    }

    private String getExtension(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dotIndex = name.lastIndexOf(".");
        return dotIndex != -1 ? name.substring(dotIndex + 1) : "";
    }
}