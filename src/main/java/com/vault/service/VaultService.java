package com.vault.service;

import com.vault.model.VaultFile;
import com.vault.util.DatabaseManager;
import com.vault.util.SecurityUtil;
import org.apache.commons.io.FileUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service class for managing vault files
 */
public class VaultService {
    
    private static VaultService instance;
    private final DatabaseManager dbManager;
    private final String vaultDirectory;
    private SecretKey encryptionKey;
    
    private VaultService() {
        this.dbManager = DatabaseManager.getInstance();
        this.vaultDirectory = createVaultDirectory();
    }
    
    public static synchronized VaultService getInstance() {
        if (instance == null) {
            instance = new VaultService();
        }
        return instance;
    }
    
    /**
     * Set encryption key for file operations
     */
    public void setEncryptionKey(String password, String salt) {
        this.encryptionKey = SecurityUtil.generateKeyFromPassword(password, salt);
    }
    
    private String createVaultDirectory() {
        String userHome = System.getProperty("user.home");
        String vaultPath = userHome + File.separator + ".securevault" + File.separator + "files";
        
        try {
            Files.createDirectories(Paths.get(vaultPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create vault directory", e);
        }
        
        return vaultPath;
    }
    
    /**
     * Store a file in the vault
     */
    public VaultFile storeFile(File sourceFile, String description, String tags) {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        try {
            // Read source file
            byte[] fileData = FileUtils.readFileToByteArray(sourceFile);
            
            // Encrypt file data
            byte[] encryptedData = SecurityUtil.encrypt(fileData, encryptionKey);
            
            // Generate secure filename
            String encryptedFileName = SecurityUtil.generateSecureFileName() + ".enc";
            String encryptedPath = vaultDirectory + File.separator + encryptedFileName;
            
            // Write encrypted file
            FileUtils.writeByteArrayToFile(new File(encryptedPath), encryptedData);
            
            // Create vault file record
            VaultFile vaultFile = new VaultFile(
                sourceFile.getName(),
                encryptedPath,
                getFileExtension(sourceFile.getName()),
                sourceFile.length(),
                description,
                tags
            );
            
            // Save to database
            int fileId = dbManager.saveVaultFile(vaultFile);
            vaultFile.setId(fileId);
            
            return vaultFile;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieve and decrypt a file from the vault
     */
    public File retrieveFile(VaultFile vaultFile, String outputDirectory) {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        try {
            // Read encrypted file
            byte[] encryptedData = FileUtils.readFileToByteArray(new File(vaultFile.getEncryptedPath()));
            
            // Decrypt file data
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, encryptionKey);
            
            // Create output file
            String outputPath = outputDirectory + File.separator + vaultFile.getOriginalName();
            File outputFile = new File(outputPath);
            
            // Ensure unique filename
            int counter = 1;
            while (outputFile.exists()) {
                String nameWithoutExt = getFileNameWithoutExtension(vaultFile.getOriginalName());
                String extension = getFileExtension(vaultFile.getOriginalName());
                String newName = nameWithoutExt + "_" + counter + 
                               (extension.isEmpty() ? "" : "." + extension);
                outputFile = new File(outputDirectory + File.separator + newName);
                counter++;
            }
            
            // Write decrypted file
            FileUtils.writeByteArrayToFile(outputFile, decryptedData);
            
            return outputFile;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a file from the vault
     */
    public boolean deleteFile(VaultFile vaultFile) {
        try {
            // Delete encrypted file
            File encryptedFile = new File(vaultFile.getEncryptedPath());
            if (encryptedFile.exists()) {
                encryptedFile.delete();
            }
            
            // Delete from database
            return dbManager.deleteVaultFile(vaultFile.getId());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all vault files
     */
    public List<VaultFile> getAllFiles() {
        return dbManager.getAllVaultFiles();
    }
    
    /**
     * Search vault files
     */
    public List<VaultFile> searchFiles(String searchTerm) {
        return dbManager.searchVaultFiles(searchTerm);
    }
    
    /**
     * Check if a file exists in the vault storage
     */
    public boolean fileExists(VaultFile vaultFile) {
        return new File(vaultFile.getEncryptedPath()).exists();
    }
    
    /**
     * Get vault storage statistics
     */
    public VaultStats getVaultStats() {
        List<VaultFile> allFiles = getAllFiles();
        long totalSize = allFiles.stream().mapToLong(VaultFile::getFileSize).sum();
        
        return new VaultStats(allFiles.size(), totalSize);
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    
    /**
     * Inner class for vault statistics
     */
    public static class VaultStats {
        private final int fileCount;
        private final long totalSize;
        
        public VaultStats(int fileCount, long totalSize) {
            this.fileCount = fileCount;
            this.totalSize = totalSize;
        }
        
        public int getFileCount() { return fileCount; }
        public long getTotalSize() { return totalSize; }
        
        public String getFormattedTotalSize() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
            if (totalSize < 1024 * 1024 * 1024) return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
            return String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
