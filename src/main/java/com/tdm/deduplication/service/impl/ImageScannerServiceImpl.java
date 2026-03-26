package com.tdm.deduplication.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.model.utility.SupportedExtensionsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.tdm.deduplication.service.ImageScannerService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.*;

@Service
public class ImageScannerServiceImpl implements ImageScannerService {
    final Logger logger = LoggerFactory.getLogger(ImageScannerServiceImpl.class);

    @Override
    public List<ImageModel> scan(String directoryPath) {
        File startFolder = new File(directoryPath);

        // Controllo validità iniziale
        if (!startFolder.exists() || !startFolder.isDirectory()) {
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
            BufferedImage image = ImageIO.read(path.toFile());

            if (image == null) {
                System.err.println("File non letto come immagine: " + path);
                return null;
            }

            ImageModel record = new ImageModel(path);
            record.setFileSize(Files.size(path));
            record.setWidth(image.getWidth());
            record.setHeight(image.getHeight());
            record.setFileFormat(getExtension(path));

            readExifDate(path, record);

            return record;

        } catch (Exception e) {
            System.err.println("Errore nella lettura di " + path + ": " + e.getMessage());
            return null;
        }
    }

    /*
        Leggo i metadati EXIF del file immagine, in particolare leggo la data dalla mappa chiave valore e la salvo nel model
     */
    private void readExifDate(Path path, ImageModel record) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (exifDirectory != null && exifDirectory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                Date date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    record.setOriginalDate(
                            date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                    );
                }
            }
        } catch (Exception ignored) {
        }
    }

    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex + 1).toUpperCase();
    }
}