package com.sfx.ui;

import com.sfx.api.ApiClient;
import com.sfx.api.FileListResponse;
import com.sfx.api.FileUploadResponse;
import com.sfx.api.KeyExchangeResponse;
import com.sfx.crypto.DHKeyExchange;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the main view of the SecureFileXchange application
 */
public class MainViewController {

    @FXML private TableView<FileViewModel> fileTable;
    @FXML private TableColumn<FileViewModel, String> filenameColumn;
    @FXML private TableColumn<FileViewModel, String> sizeColumn;
    @FXML private TableColumn<FileViewModel, String> dateColumn;
    
    @FXML private TextField serverUrlField;
    @FXML private TextField userIdField;
    @FXML private Button connectButton;
    @FXML private Button uploadButton;
    @FXML private Button downloadButton;
    @FXML private Button refreshButton;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    
    private ApiClient apiClient;
    private DHKeyExchange keyExchange;
    // This key will be used in future versions for file encryption/decryption
    // Currently kept as part of the key exchange implementation
    private byte[] encryptionKey; 
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ObservableList<FileViewModel> fileList = FXCollections.observableArrayList();
    private Stage primaryStage;
    
    /**
     * Initialize the controller with the primary stage
     */
    public void init(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize the table view
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        fileTable.setItems(fileList);
        
        // Default values
        serverUrlField.setText("http://localhost:8000");
        userIdField.setText("java-client-user");
        
        // Disable buttons until connected
        uploadButton.setDisable(true);
        downloadButton.setDisable(true);
        refreshButton.setDisable(true);
    }
    
    /**
     * Handle connect button click
     */
    @FXML
    private void handleConnect(ActionEvent event) {
        String serverUrl = serverUrlField.getText();
        if (serverUrl.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Server URL cannot be empty");
            return;
        }
        
        setStatus("Connecting to server...", true);
        System.out.println("Attempting to connect to: " + serverUrl);
        
        // Initialize API client
        apiClient = new ApiClient(serverUrl);
        
        executorService.submit(() -> {
            try {
                // Check server health
                System.out.println("Checking server health...");
                boolean isHealthy = apiClient.healthCheck();
                System.out.println("Server health check result: " + isHealthy);
                
                if (!isHealthy) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Connection Error", "Server is not healthy");
                        setStatus("Connection failed", false);
                    });
                    return;
                }
                
                // Initialize key exchange
                System.out.println("Initializing key exchange...");
                keyExchange = new DHKeyExchange();
                System.out.println("Key exchange initialized, calling server...");
                KeyExchangeResponse response = apiClient.initiateKeyExchange();
                System.out.println("Received key exchange response with session ID: " + response.getSessionId());
                
                // Compute shared secret and derive encryption key
                System.out.println("Computing shared secret...");
                byte[] sharedSecret = keyExchange.computeSharedSecret(response.getPublicKey());
                encryptionKey = keyExchange.deriveEncryptionKey(sharedSecret);
                System.out.println("Encryption key established: " + (encryptionKey != null ? "Yes (" + encryptionKey.length + " bytes)" : "No"));
                
                // Complete the key exchange
                System.out.println("Completing key exchange with server...");
                boolean success = apiClient.completeKeyExchange(
                        response.getSessionId(), keyExchange.getPublicKeyBase64());
                System.out.println("Key exchange completion result: " + success);
                
                if (!success) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to complete key exchange");
                        setStatus("Key exchange failed", false);
                    });
                    return;
                }
                
                // Update UI on success
                System.out.println("Connection successful, updating UI...");
                Platform.runLater(() -> {
                    connectButton.setDisable(true);
                    serverUrlField.setDisable(true);
                    uploadButton.setDisable(false);
                    downloadButton.setDisable(false);
                    refreshButton.setDisable(false);
                    setStatus("Connected", false);
                    
                    // Load file list
                    refreshFileList();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Connection error: " + e.getMessage());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect: " + e.getMessage());
                    setStatus("Connection failed", false);
                });
            }
        });
    }
    
    /**
     * Handle upload button click
     */
    @FXML
    private void handleUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        
        if (selectedFile != null) {
            setStatus("Uploading file...", true);
            
            executorService.submit(() -> {
                try {
                    String userId = userIdField.getText();
                    FileUploadResponse response = apiClient.uploadFile(selectedFile, userId);
                    
                    Platform.runLater(() -> {
                        setStatus("File uploaded: " + response.getStoredFilename(), false);
                        refreshFileList();
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Upload Error", "Failed to upload file: " + e.getMessage());
                        setStatus("Upload failed", false);
                    });
                }
            });
        }
    }
    
    /**
     * Handle download button click
     */
    @FXML
    private void handleDownload(ActionEvent event) {
        FileViewModel selected = fileTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a file to download");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(selected.getFilename());
        File saveFile = fileChooser.showSaveDialog(primaryStage);
        
        if (saveFile != null) {
            setStatus("Downloading file...", true);
            
            executorService.submit(() -> {
                try {
                    String userId = userIdField.getText();
                    boolean success = apiClient.downloadFile(selected.getFilename(), saveFile, userId);
                    
                    Platform.runLater(() -> {
                        if (success) {
                            setStatus("File downloaded: " + saveFile.getName(), false);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Download Error", "Failed to download file");
                            setStatus("Download failed", false);
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Download Error", "Failed to download file: " + e.getMessage());
                        setStatus("Download failed", false);
                    });
                }
            });
        }
    }
    
    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshFileList();
    }
    
    /**
     * Refresh the file list from the server
     */
    private void refreshFileList() {
        setStatus("Loading files...", true);
        
        executorService.submit(() -> {
            try {
                String userId = userIdField.getText();
                FileListResponse response = apiClient.listFiles(userId);
                
                Platform.runLater(() -> {
                    fileList.clear();
                    
                    if (response.getFiles() != null) {
                        for (FileListResponse.FileInfo fileInfo : response.getFiles()) {
                            fileList.add(new FileViewModel(
                                    fileInfo.getFilename(),
                                    formatFileSize(fileInfo.getSizeBytes()),
                                    fileInfo.getCreatedAt()
                            ));
                        }
                    }
                    
                    setStatus("Loaded " + fileList.size() + " files", false);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to load file list: " + e.getMessage());
                    setStatus("Refresh failed", false);
                });
            }
        });
    }
    
    /**
     * Set status message and progress indicator
     */
    private void setStatus(String message, boolean inProgress) {
        statusLabel.setText(message);
        progressBar.setVisible(inProgress);
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Format file size in human-readable format
     */
    private String formatFileSize(long bytes) {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double size = bytes;
        
        while (size > 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * View model for file table
     */
    public static class FileViewModel {
        private final String filename;
        private final String size;
        private final String date;
        
        public FileViewModel(String filename, String size, String date) {
            this.filename = filename;
            this.size = size;
            this.date = date;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public String getSize() {
            return size;
        }
        
        public String getDate() {
            return date;
        }
    }
}
