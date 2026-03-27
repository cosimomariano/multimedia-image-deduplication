package com.tdm.deduplication.service;

import com.tdm.deduplication.model.DuplicateGroup;
import com.tdm.deduplication.model.ImageModel;

import java.util.List;

public interface DeduplicationService {
    List<DuplicateGroup> findDuplicates(List<ImageModel> images);
}
