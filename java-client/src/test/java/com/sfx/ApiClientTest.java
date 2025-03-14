package com.sfx;

import com.sfx.api.ApiClient;
import com.sfx.api.FileListResponse;
import com.sfx.api.FileUploadResponse;
import com.sfx.api.KeyExchangeResponse;
import com.sfx.crypto.DHKeyExchange;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test class to demonstrate API client functionality
 * Run this with the Python server running to see the interaction
 */
public class ApiClientTest {

    private static final String SERVER_URL = "http://localhost:8000";
    private static final String USER_ID = "java-test-user";

    public static void main(String[] args) {
        try {
            System.out.println("Starting SecureFileXchange API Client Test");
            
            // Initialize API client
            ApiClient apiClient = new ApiClient(SERVER_URL);
            
            // Test health check
            System.out.println("\n1. Testing health check...");
            boolean isHealthy = apiClient.healthCheck();
            System.out.println("Health check result: " + (isHealthy ? "Healthy" : "Not healthy"));
            
            if (!isHealthy) {
                System.err.println("Server is not healthy. Make sure the Python server is running.");
                System.exit(1);
            }
            
            // Test key exchange
            System.out.println("\n2. Testing key exchange...");
            DHKeyExchange keyExchange = new DHKeyExchange();
            KeyExchangeResponse keyResponse = apiClient.initiateKeyExchange();
            
            System.out.println("Key exchange initiated with session ID: " + keyResponse.getSessionId());
            System.out.println("Server public key: " + keyResponse.getPublicKey().substring(0, 20) + "...");
            System.out.println("Client public key: " + keyExchange.getPublicKeyBase64().substring(0, 20) + "...");
            
            // Complete the key exchange process
            boolean keyExchangeCompleted = apiClient.completeKeyExchange(keyResponse.getSessionId(), keyExchange.getPublicKeyBase64());
            System.out.println("Key exchange completed: " + keyExchangeCompleted);
            
            // Derive shared secret (would be used for encryption in a real implementation)
            byte[] sharedSecret = keyExchange.computeSharedSecret(keyResponse.getPublicKey());
            System.out.println("Shared secret established: " + (sharedSecret != null && sharedSecret.length > 0));
            
            // Create a test file
            System.out.println("\n3. Creating test file for upload...");
            Path testFilePath = Files.createTempFile("sfx-test-", ".txt");
            String testContent = "This is a test file for SecureFileXchange at " + System.currentTimeMillis();
            Files.writeString(testFilePath, testContent);
            
            File testFile = testFilePath.toFile();
            System.out.println("Created test file: " + testFile.getAbsolutePath());
            
            // Upload test file
            System.out.println("\n4. Testing file upload...");
            FileUploadResponse uploadResponse = apiClient.uploadFile(testFile, USER_ID);
            System.out.println("Upload successful. Original filename: " + uploadResponse.getFilename());
            System.out.println("Stored on server as: " + uploadResponse.getStoredFilename());
            
            // List files
            System.out.println("\n5. Testing file listing...");
            FileListResponse listResponse = apiClient.listFiles(USER_ID);
            System.out.println("Files on server:");
            
            if (listResponse.getFiles() != null && !listResponse.getFiles().isEmpty()) {
                for (FileListResponse.FileInfo fileInfo : listResponse.getFiles()) {
                    System.out.println(" - " + fileInfo.getFilename() + " (" + fileInfo.getSizeBytes() + " bytes)");
                }
                
                // Download file
                System.out.println("\n6. Testing file download...");
                String fileToDownload = uploadResponse.getStoredFilename();
                Path downloadPath = Files.createTempFile("sfx-download-", ".txt");
                System.out.println("Downloading " + fileToDownload + " to " + downloadPath);
                
                boolean downloadSuccess = apiClient.downloadFile(fileToDownload, downloadPath.toFile(), USER_ID);
                if (downloadSuccess) {
                    String downloadedContent = Files.readString(downloadPath);
                    System.out.println("Download successful!");
                    System.out.println("Content: " + downloadedContent);
                    
                    // Verify content matches
                    if (testContent.equals(downloadedContent)) {
                        System.out.println("Content verification: SUCCESS");
                    } else {
                        System.out.println("Content verification: FAILED");
                    }
                } else {
                    System.out.println("Download failed");
                }
                
                // Clean up
                Files.deleteIfExists(downloadPath);
            } else {
                System.out.println("No files found on server");
            }
            
            // Clean up test file
            Files.deleteIfExists(testFilePath);
            
            System.out.println("\nTest completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
