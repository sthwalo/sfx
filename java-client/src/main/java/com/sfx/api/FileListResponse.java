package com.sfx.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response model for listing files
 */
public class FileListResponse {
    @JsonProperty("files")
    private List<FileInfo> files;

    // Default constructor for Jackson
    public FileListResponse() {}

    public FileListResponse(List<FileInfo> files) {
        this.files = files;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    /**
     * Information about a file on the server
     */
    public static class FileInfo {
        @JsonProperty("filename")
        private String filename;
        
        @JsonProperty("size_bytes")
        private long sizeBytes;
        
        @JsonProperty("created_at")
        private String createdAt;

        // Default constructor for Jackson
        public FileInfo() {}

        public FileInfo(String filename, long sizeBytes, String createdAt) {
            this.filename = filename;
            this.sizeBytes = sizeBytes;
            this.createdAt = createdAt;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
