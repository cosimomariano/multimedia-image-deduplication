package com.tdm.deduplication.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.tdm.deduplication.model.ImageModel;
import com.tdm.deduplication.service.ExifExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExifExtractionServiceImpl implements ExifExtractionService {

    private final static Logger logger = LoggerFactory.getLogger(ExifExtractionServiceImpl.class);


    @Override
    public void populateMetadata(Path imagePath, ImageModel imageModel) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imagePath.toFile());
            Map<String, String> exifData = new LinkedHashMap<>();

            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            ExifSubIFDDirectory subIfd = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            if (ifd0 != null) {
                putIfPresent(exifData, "make", ifd0, ExifIFD0Directory.TAG_MAKE);
                putIfPresent(exifData, "model", ifd0, ExifIFD0Directory.TAG_MODEL);
                putIfPresent(exifData, "software", ifd0, ExifIFD0Directory.TAG_SOFTWARE);
                putIfPresent(exifData, "orientation", ifd0, ExifIFD0Directory.TAG_ORIENTATION);
                putIfPresent(exifData, "x_resolution", ifd0, ExifIFD0Directory.TAG_X_RESOLUTION);
                putIfPresent(exifData, "y_resolution", ifd0, ExifIFD0Directory.TAG_Y_RESOLUTION);
                putIfPresent(exifData, "resolution_unit", ifd0, ExifIFD0Directory.TAG_RESOLUTION_UNIT);
                putIfPresent(exifData, "datetime", ifd0, ExifIFD0Directory.TAG_DATETIME);
                putIfPresent(exifData, "copyright", ifd0, ExifIFD0Directory.TAG_COPYRIGHT);
                putIfPresent(exifData, "artist", ifd0, ExifIFD0Directory.TAG_ARTIST);
            }

            if (subIfd != null) {
                putIfPresent(exifData, "datetime_original", subIfd, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                putIfPresent(exifData, "datetime_digitized", subIfd, ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
                putIfPresent(exifData, "subsec_time_original", subIfd, ExifSubIFDDirectory.TAG_SUBSECOND_TIME_ORIGINAL);
                putIfPresent(exifData, "exposure_time", subIfd, ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
                putIfPresent(exifData, "f_number", subIfd, ExifSubIFDDirectory.TAG_FNUMBER);
                putIfPresent(exifData, "iso", subIfd, ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
                putIfPresent(exifData, "focal_length", subIfd, ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
                putIfPresent(exifData, "focal_length_35mm", subIfd, ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH);
                putIfPresent(exifData, "exposure_bias", subIfd, ExifSubIFDDirectory.TAG_EXPOSURE_BIAS);
                putIfPresent(exifData, "aperture", subIfd, ExifSubIFDDirectory.TAG_APERTURE);
                putIfPresent(exifData, "max_aperture", subIfd, ExifSubIFDDirectory.TAG_MAX_APERTURE);
                putIfPresent(exifData, "metering_mode", subIfd, ExifSubIFDDirectory.TAG_METERING_MODE);
                putIfPresent(exifData, "white_balance", subIfd, ExifSubIFDDirectory.TAG_WHITE_BALANCE_MODE);
                putIfPresent(exifData, "flash", subIfd, ExifSubIFDDirectory.TAG_FLASH);
                putIfPresent(exifData, "exposure_program", subIfd, ExifSubIFDDirectory.TAG_EXPOSURE_PROGRAM);
                putIfPresent(exifData, "color_space", subIfd, ExifSubIFDDirectory.TAG_COLOR_SPACE);
                putIfPresent(exifData, "exif_image_width", subIfd, ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                putIfPresent(exifData, "exif_image_height", subIfd, ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                putIfPresent(exifData, "digital_zoom_ratio", subIfd, ExifSubIFDDirectory.TAG_DIGITAL_ZOOM_RATIO);
                putIfPresent(exifData, "scene_capture_type", subIfd, ExifSubIFDDirectory.TAG_SCENE_CAPTURE_TYPE);
                putIfPresent(exifData, "contrast", subIfd, ExifSubIFDDirectory.TAG_CONTRAST);
                putIfPresent(exifData, "saturation", subIfd, ExifSubIFDDirectory.TAG_SATURATION);
                putIfPresent(exifData, "sharpness", subIfd, ExifSubIFDDirectory.TAG_SHARPNESS);
                putIfPresent(exifData, "lens_model", subIfd, ExifSubIFDDirectory.TAG_LENS_MODEL);
                putIfPresent(exifData, "lens_make", subIfd, ExifSubIFDDirectory.TAG_LENS_MAKE);

                Date originalDate = subIfd.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (originalDate != null) {
                    imageModel.setOriginalDate(
                            originalDate.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                    );
                }
            }

            if (gpsDirectory != null) {
                GeoLocation geoLocation = gpsDirectory.getGeoLocation();

                if (geoLocation != null && !geoLocation.isZero()) {
                    exifData.put("gps_latitude", String.valueOf(geoLocation.getLatitude()));
                    exifData.put("gps_longitude", String.valueOf(geoLocation.getLongitude()));
                }

                putIfPresent(exifData, "gps_latitude_ref", gpsDirectory, GpsDirectory.TAG_LATITUDE_REF);
                putIfPresent(exifData, "gps_longitude_ref", gpsDirectory, GpsDirectory.TAG_LONGITUDE_REF);
                putIfPresent(exifData, "gps_altitude", gpsDirectory, GpsDirectory.TAG_ALTITUDE);
                putIfPresent(exifData, "gps_altitude_ref", gpsDirectory, GpsDirectory.TAG_ALTITUDE_REF);
                putIfPresent(exifData, "gps_timestamp", gpsDirectory, GpsDirectory.TAG_TIME_STAMP);
                putIfPresent(exifData, "gps_datestamp", gpsDirectory, GpsDirectory.TAG_DATE_STAMP);
            }

            imageModel.setExifMetadata(exifData);

        } catch (Exception ignored) {
            logger.error("Nessun metadato presente nell'immagine");
            imageModel.setExifMetadata(new LinkedHashMap<>());
        }
    }

    private void putIfPresent(Map<String, String> target, String key, Directory directory, int tagType) {
        if (directory.containsTag(tagType)) {
            String value = directory.getDescription(tagType);
            if (value != null && !value.isBlank()) {
                target.put(key, value);
            }
        }
    }
}