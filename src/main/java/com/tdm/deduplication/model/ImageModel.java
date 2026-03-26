package com.tdm.deduplication.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class ImageModel {

    private Path filePath;
    private long fileSize;
    private int width;
    private int height;
    private String fileFormat;
    private LocalDateTime originalDate;

    public ImageModel(Path filePath) {
        this.filePath = filePath;
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

    @Override
    public String toString() {
        return "ImageModel{" +
                "filePath=" + filePath +
                ", fileSize=" + fileSize +
                ", width=" + width +
                ", height=" + height +
                ", fileFormat='" + fileFormat + '\'' +
                ", originalDate=" + originalDate +
                '}';
    }
}