package com.tdm.deduplication.service;

import com.tdm.deduplication.model.ImageModel;

import java.util.List;

public interface ImageScannerService {
    List<ImageModel> scan(String directory);
}