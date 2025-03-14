package com.sfx.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with the SecureFileXchange API
 */
public class ApiClient {
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Check API health
     * @return true if API is healthy
     */
    public boolean healthCheck() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/health")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return false;
            }

            String responseBody = response.body().string();
            Map<String, String> result = objectMapper.readValue(responseBody, new TypeReference<Map<String, String>>() {});
            return "ok".equals(result.get("status"));
        }
    }

    /**
     * Initiate a Diffie-Hellman key exchange with the server
     * @return KeyExchangeResponse containing session ID, public key, and parameters
     */
    public KeyExchangeResponse initiateKeyExchange() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/key-exchange/init")
                .post(RequestBody.create("", MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to initiate key exchange: " + response.code());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, KeyExchangeResponse.class);
        }
    }

    /**
     * Complete a key exchange by sending our public key to the server
     * @param sessionId Session ID returned from initiateKeyExchange
     * @param publicKey Our public key to send to the server
     * @return true if exchange was successful
     */
    public boolean completeKeyExchange(String sessionId, String publicKey) throws IOException {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "session_id", sessionId,
                "public_key", publicKey
        ));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/key-exchange/complete")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to complete key exchange: " + response.code());
            }
            return true;
        }
    }

    /**
     * Upload a file to the server
     * @param file The file to upload
     * @param userId The user ID (optional)
     * @return Information about the uploaded file
     */
    public FileUploadResponse uploadFile(File file, String userId) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .addFormDataPart("user_id", userId)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/api/files/upload")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to upload file: " + response.code());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, FileUploadResponse.class);
        }
    }

    /**
     * Download a file from the server
     * @param filename The name of the file to download
     * @param destinationFile The destination file to save the downloaded content
     * @param userId The user ID (optional)
     * @return true if download was successful
     */
    public boolean downloadFile(String filename, File destinationFile, String userId) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/api/files/download/" + filename)
                .newBuilder()
                .addQueryParameter("user_id", userId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response.code());
            }

            // Save the file
            java.nio.file.Files.copy(
                    response.body().byteStream(),
                    destinationFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return true;
        }
    }

    /**
     * Get a list of available files
     * @param userId The user ID (optional)
     * @return List of file information
     */
    public FileListResponse listFiles(String userId) throws IOException {
        HttpUrl url = HttpUrl.parse(baseUrl + "/api/files/list")
                .newBuilder()
                .addQueryParameter("user_id", userId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to list files: " + response.code());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, FileListResponse.class);
        }
    }
}
