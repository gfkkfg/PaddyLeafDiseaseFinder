package com.example.paddyleafdiseasedetection;

import java.util.Date;

public class HistoryItem {
    private String disease;
    private Date timestamp;
    private float confidence;  // Confidence score for the detected disease
    private String imageUrl;   // URL of the uploaded/detected image (optional)
    private String location;   // Location where the detection was made (optional)

    public HistoryItem(String disease, Date timestamp, float confidence, String imageUrl, String location) {
        this.disease = disease;
        this.timestamp = timestamp;
        this.confidence = confidence;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public String getDisease() {
        return disease;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocation() {
        return location;
    }
}


