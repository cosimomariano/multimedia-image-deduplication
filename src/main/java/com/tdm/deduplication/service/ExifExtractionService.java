package com.tdm.deduplication.service;

import com.tdm.deduplication.model.ImageModel;

import java.nio.file.Path;

public interface ExifExtractionService {
    void populateMetadata(Path imagePath, ImageModel imageModel);
}