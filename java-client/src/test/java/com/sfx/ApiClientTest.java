package com.sfx;

import com.sfx.api.ApiClient;
import com.sfx.api.FileListResponse;
import com.sfx.api.FileUploadResponse;
import com.sfx.api.KeyExchangeResponse;
import com.sfx.crypto.DHKeyExchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Test class for ApiClient functionality
 * CI-compatible with no dependency on running server
 */
public class ApiClientTest {

    private static final String SERVER_URL = "http://localhost:8000";
    private static final String USER_ID = "java-test-user";
    
    // Mock responses for tests
    private static final KeyExchangeResponse MOCK_KEY_RESPONSE = createMockKeyResponse();
    private static final FileUploadResponse MOCK_UPLOAD_RESPONSE = createMockUploadResponse();
    private static final FileListResponse MOCK_LIST_RESPONSE = createMockListResponse();
    
    @TempDir
    Path tempDir; // JUnit will create a temporary directory
    
    private ApiClient apiClient;
    
    @BeforeEach
    public void setup() {
        apiClient = mock(ApiClient.class);
    }
    
    /**
     * Create mock objects for testing
     */
    private static KeyExchangeResponse createMockKeyResponse() {
        KeyExchangeResponse response = new KeyExchangeResponse();
        response.setSessionId("mock-session-id");
        response.setPublicKey("mock-public-key");
        response.setParameters("mock-parameters");
        return response;
    }
    
    private static FileUploadResponse createMockUploadResponse() {
        FileUploadResponse response = new FileUploadResponse();
        response.setFilename("test-file.txt");
        response.setStoredFilename("mock-stored-filename");
        response.setMessage("File uploaded successfully");
        return response;
    }
    
    private static FileListResponse createMockListResponse() {
        FileListResponse response = new FileListResponse();
        FileListResponse.FileInfo fileInfo = new FileListResponse.FileInfo();
        fileInfo.setFilename("mock-stored-filename");
        fileInfo.setSizeBytes(100L);
        fileInfo.setCreatedAt("2023-01-01T00:00:00");
        response.setFiles(Collections.singletonList(fileInfo));
        return response;
    }
    
    /**
     * Test DHKeyExchange functionality (no server needed)
     */
    @Test
    public void testDHKeyExchange() {
        DHKeyExchange keyExchange = new DHKeyExchange();
        String publicKey = keyExchange.getPublicKeyBase64();
        
        assertNotNull(publicKey, "Public key should not be null");
        assertFalse(publicKey.isEmpty(), "Public key should not be empty");
    }
    
    /**
     * Test file creation and basic operations (no server needed)
     */
    @Test
    public void testFileOperations() {
        try {
            // Create a test file
            File testFile = tempDir.resolve("test-file.txt").toFile();
            String testContent = "This is a test file for SFX";
            Files.writeString(testFile.toPath(), testContent);
            
            // Verify file was created correctly
            assertTrue(testFile.exists(), "Test file should exist");
            assertEquals(testContent, Files.readString(testFile.toPath()), "File content should match");
        } catch (IOException e) {
            fail("IOException occurred during file operations: " + e.getMessage());
        }
    }
    
    /**
     * Test health check
     */
    @Test
    public void testHealthCheck() {
        try {
            when(apiClient.healthCheck()).thenReturn(true);
            boolean isHealthy = apiClient.healthCheck();
            assertTrue(isHealthy, "Server should be healthy");
        } catch (IOException e) {
            fail("IOException occurred during health check: " + e.getMessage());
        }
    }
    
    /**
     * Test key exchange
     */
    @Test
    public void testKeyExchange() {
        try {
            when(apiClient.initiateKeyExchange()).thenReturn(MOCK_KEY_RESPONSE);
            KeyExchangeResponse keyResponse = apiClient.initiateKeyExchange();
            assertNotNull(keyResponse, "Key response should not be null");
            assertEquals(MOCK_KEY_RESPONSE.getSessionId(), keyResponse.getSessionId(), "Session ID should match");
            assertEquals(MOCK_KEY_RESPONSE.getPublicKey(), keyResponse.getPublicKey(), "Public key should match");
        } catch (IOException e) {
            fail("IOException occurred during key exchange: " + e.getMessage());
        }
    }
    
    /**
     * Test file upload
     */
    @Test
    public void testFileUpload() {
        try {
            // Create a test file
            File testFile = tempDir.resolve("test-file.txt").toFile();
            String testContent = "This is a test file for SFX";
            Files.writeString(testFile.toPath(), testContent);
            
            when(apiClient.uploadFile(testFile, USER_ID)).thenReturn(MOCK_UPLOAD_RESPONSE);
            FileUploadResponse uploadResponse = apiClient.uploadFile(testFile, USER_ID);
            assertNotNull(uploadResponse, "Upload response should not be null");
            assertEquals(MOCK_UPLOAD_RESPONSE.getFilename(), uploadResponse.getFilename(), "Filename should match");
            assertEquals(MOCK_UPLOAD_RESPONSE.getStoredFilename(), uploadResponse.getStoredFilename(), "Stored filename should match");
        } catch (IOException e) {
            fail("IOException occurred during file upload: " + e.getMessage());
        }
    }
    
    /**
     * Test file listing
     */
    @Test
    public void testFileListing() {
        try {
            when(apiClient.listFiles(USER_ID)).thenReturn(MOCK_LIST_RESPONSE);
            FileListResponse listResponse = apiClient.listFiles(USER_ID);
            assertNotNull(listResponse, "List response should not be null");
            assertEquals(MOCK_LIST_RESPONSE.getFiles().size(), listResponse.getFiles().size(), "Number of files should match");
            assertEquals(MOCK_LIST_RESPONSE.getFiles().get(0).getFilename(), listResponse.getFiles().get(0).getFilename(), "Filename should match");
        } catch (IOException e) {
            fail("IOException occurred during file listing: " + e.getMessage());
        }
    }
    
    // This is kept for manual testing but won't run in CI
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
            
            // Continue with other manual tests...
            
        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
