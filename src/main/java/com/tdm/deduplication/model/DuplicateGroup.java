package com.tdm.deduplication.model;

import java.util.ArrayList;
import java.util.List;

public class DuplicateGroup {

    private String groupId;
    private List<ImageModel> images = new ArrayList<>();

    public DuplicateGroup(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<ImageModel> getImages() {
        return images;
    }

    public void addImage(ImageModel image) {
        this.images.add(image);
    }
}