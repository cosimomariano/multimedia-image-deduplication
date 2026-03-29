package com.tdm.deduplication.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageModel {

    private Path filePath;
    private long fileSize;
    private int width;
    private int height;
    private String fileFormat;
    private LocalDateTime originalDate;

    private String differentialSignature;
    private String groupId;
    private Double  bestDistance;

    private double[] luminanceSignature;
    private double[] chrominanceCbSignature;
    private double[] chrominanceCrSignature;

    private Map<String, String> exifMetadata = new LinkedHashMap<>();

    public double[] getLuminanceSignature() {
        return luminanceSignature;
    }

    public void setLuminanceSignature(double[] luminanceSignature) {
        this.luminanceSignature = luminanceSignature;
    }

    public Map<String, String> getExifMetadata() {
        return exifMetadata;
    }

    public void setExifMetadata(Map<String, String> exifMetadata) {
        this.exifMetadata = exifMetadata;
    }

    public double[] getChrominanceCbSignature() {
        return chrominanceCbSignature;
    }

    public void setChrominanceCbSignature(double[] chrominanceCbSignature) {
        this.chrominanceCbSignature = chrominanceCbSignature;
    }

    public double[] getChrominanceCrSignature() {
        return chrominanceCrSignature;
    }

    public void setChrominanceCrSignature(double[] chrominanceCrSignature) {
        this.chrominanceCrSignature = chrominanceCrSignature;
    }

    public ImageModel(Path filePath) {
        this.filePath = filePath;
    }

    public ImageModel() {
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public LocalDateTime getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(LocalDateTime originalDate) {
        this.originalDate = originalDate;
    }

    public String getDifferentialSignature() {
        return differentialSignature;
    }

    public void setDifferentialSignature(String differentialSignature) {
        this.differentialSignature = differentialSignature;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Double getBestDistance() {
        return bestDistance;
    }

    public void setBestDistance(Double bestDistance) {
        this.bestDistance = bestDistance;
    }

    @Override
    public String toString() {
        return "ImageModel{" +
                "\n" + "filePath=" + filePath +
                "\n" + ", fileSize=" + fileSize +
                "\n" + ", width=" + width +
                "\n" + ", height=" + height +
                "\n" + ", fileFormat='" + fileFormat + '\'' +
                "\n" + ", originalDate=" + originalDate +
                "\n" + ", exifMetadata=" + exifMetadata +
                "\n" + "}\n";
    }
}