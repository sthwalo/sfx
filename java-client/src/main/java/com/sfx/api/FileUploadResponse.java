package com.sfx.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for file upload
 */
public class FileUploadResponse {
    @JsonProperty("filename")
    private String filename;
    
    @JsonProperty("stored_filename")
    private String storedFilename;
    
    @JsonProperty("message")
    private String message;

    // Default constructor for Jackson
    public FileUploadResponse() {}

    public FileUploadResponse(String filename, String storedFilename, String message) {
        this.filename = filename;
        this.storedFilename = storedFilename;
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
