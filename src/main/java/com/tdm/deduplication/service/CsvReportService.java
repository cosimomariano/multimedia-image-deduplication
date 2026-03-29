package com.tdm.deduplication.service;

import com.tdm.deduplication.model.ImageModel;

import java.util.List;

public interface CsvReportService {
    void writeReport(List<ImageModel> images, String outputPath);
}