package com.tdm.deduplication.service.impl;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.DeduplicationService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeduplicationServiceImpl implements DeduplicationService {

    private static final int NORMALIZED_WIDTH = 64;
    private static final int NORMALIZED_HEIGHT = 64;
    private static final int HAMMING_THRESHOLD = 200;

    @Override
    public List<DuplicateGroup> findDuplicates(List<ImageModel> images) {
        for (ImageModel image : images) {
            image.setDifferentialSignature(buildSignature(image));
        }

        List<DuplicateGroup> groups = new ArrayList<>();
        int groupCounter = 1;

        for (ImageModel current : images) {
            if (current.getGroupId() != null) {
                continue;
            }

            String groupId = "GROUP_" + groupCounter++;
            DuplicateGroup group = new DuplicateGroup(groupId);

            current.setGroupId(groupId);
            current.setBestDistance(0);
            group.addImage(current);

            for (ImageModel candidate : images) {
                if (candidate == current || candidate.getGroupId() != null) {
                    continue;
                }

                int distance = hammingDistance(
                        current.getDifferentialSignature(),
                        candidate.getDifferentialSignature()
                );

                if (distance <= HAMMING_THRESHOLD) {
                    candidate.setGroupId(groupId);
                    candidate.setBestDistance(distance);
                    group.addImage(candidate);
                }
            }

            if (group.getImages().size() > 1) {
                groups.add(group);
            }
        }

        return groups;
    }

    private String buildSignature(ImageModel ImageModel) {
        try {
            BufferedImage original = ImageIO.read(ImageModel.getFilePath().toFile());

            if (original == null) {
                throw new IllegalStateException("Immagine non leggibile: " + ImageModel.getFilePath());
            }

            BufferedImage resized = resize(original, NORMALIZED_WIDTH, NORMALIZED_HEIGHT);
            int[][] gray = toGrayMatrix(resized);

            return buildDifferentialBinarySignature(gray);

        } catch (Exception e) {
            throw new RuntimeException("Errore nella generazione della firma per " + ImageModel.getFilePath(), e);
        }
    }

    private BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private int[][] toGrayMatrix(BufferedImage image) {
        int[][] gray = new int[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                int luminance = (int) (0.299 * color.getRed()
                        + 0.587 * color.getGreen()
                        + 0.114 * color.getBlue());
                gray[y][x] = luminance;
            }
        }

        return gray;
    }

    private String buildDifferentialBinarySignature(int[][] gray) {
        StringBuilder signature = new StringBuilder();

        for (int y = 0; y < gray.length; y++) {
            for (int x = 1; x < gray[y].length; x++) {
                int predicted = gray[y][x - 1];
                int residual = gray[y][x] - predicted;

                if (residual > 0) {
                    signature.append('1');
                } else {
                    signature.append('0');
                }
            }
        }

        return signature.toString();
    }

    private int hammingDistance(String first, String second) {
        if (first.length() != second.length()) {
            throw new IllegalArgumentException("Le firme devono avere la stessa lunghezza");
        }

        int distance = 0;

        for (int i = 0; i < first.length(); i++) {
            if (first.charAt(i) != second.charAt(i)) {
                distance++;
            }
        }

        return distance;
    }
}