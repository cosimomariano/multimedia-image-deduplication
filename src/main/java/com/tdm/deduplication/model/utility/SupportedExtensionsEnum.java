package com.tdm.deduplication.model.utility;

public enum SupportedExtensionsEnum {
    /*
        Estensioni attualmente gestite
     */
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png");

    private final String extension;

    SupportedExtensionsEnum(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static boolean isSupported(String ext) {
        if (ext == null || ext.isBlank()) {
            return false;
        }

        for (SupportedExtensionsEnum format : values()) {
            if (format.getExtension().equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
}
