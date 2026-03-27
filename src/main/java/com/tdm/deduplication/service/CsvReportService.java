package com.tdm.deduplication.service;

import com.tdm.deduplication.model.DuplicateGroup;

import java.util.List;

public interface CsvReportService {
    void writeReport(List<DuplicateGroup> groups, String outputPath);
}