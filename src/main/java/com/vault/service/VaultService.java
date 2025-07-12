package com.vault.service;

import com.vault.model.VaultFile;
import com.vault.model.Admin;
import com.vault.util.DatabaseManager;
import com.vault.util.SecurityUtil;
import com.vault.config.SecurityConfig;
import com.vault.util.SecureErrorHandler;
import org.apache.commons.io.FileUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
            // Validate inputs
            if (vaultFile == null || vaultFile.getEncryptedPath() == null) {
                throw new IllegalArgumentException("Invalid vault file");
            }
            
            File encryptedFile = new File(vaultFile.getEncryptedPath());
            if (!encryptedFile.exists()) {
                throw new RuntimeException("Encrypted file not found: " + vaultFile.getEncryptedPath());
            }
            
            // System.out.println("Reading encrypted file: " + vaultFile.getEncryptedPath());
            // System.out.println("File size: " + encryptedFile.length() + " bytes");
            
            // Read encrypted file
            byte[] encryptedData = FileUtils.readFileToByteArray(encryptedFile);
            // System.out.println("Read " + encryptedData.length + " bytes of encrypted data");
            
            // Decrypt file data
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, encryptionKey);
            System.out.println("Decrypted " + decryptedData.length + " bytes of data");
            
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
            System.out.println("Successfully wrote decrypted file: " + outputFile.getAbsolutePath());
            
            return outputFile;
            
        } catch (IOException e) {
            SecureErrorHandler.handleDatabaseError(e);
            throw new RuntimeException("Failed to retrieve file: " + e.getMessage(), e);
        } catch (Exception e) {
            SecureErrorHandler.handleCryptoError(e);
            
            // Disable automatic recovery to prevent error messages
            // if (e.getMessage().contains("decrypt") || e.getMessage().contains("padding")) {
            //     System.out.println("Decryption failed. Attempting file recovery...");
            //     try {
            //         File recoveredFile = tryRecoveryDecryption(vaultFile, outputDirectory);
            //         if (recoveredFile != null && recoveredFile.exists()) {
            //             System.out.println("Recovery successful!");
            //             return recoveredFile;
            //         }
            //     } catch (Exception recoveryException) {
            //         System.err.println("File recovery also failed: " + recoveryException.getMessage());
            //     }
            // }
            
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
     * Get all vault files from the database
     */
    public List<VaultFile> getAllVaultFiles() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            return dbManager.getAllFiles();
        } catch (Exception e) {
            System.err.println("Failed to get vault files: " + e.getMessage());
            return new ArrayList<>();
        }
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
     * Get dynamic space requirements and usage information
     */
    public SpaceInfo getSpaceInfo() {
        VaultStats stats = getVaultStats();
        
        // Calculate overhead for encryption and metadata (approximately 20% overhead)
        long encryptionOverhead = (long) (stats.getTotalSize() * 0.2);
        long totalUsedSpace = stats.getTotalSize() + encryptionOverhead;
        
        // Recommended free space: current usage + 50% buffer for new files
        long recommendedFreeSpace = (long) (totalUsedSpace * 1.5);
        
        // Minimum required space: current usage + 100MB buffer
        long minimumRequiredSpace = totalUsedSpace + (100L * 1024L * 1024L);
        
        return new SpaceInfo(stats.getFileCount(), stats.getTotalSize(), 
                           totalUsedSpace, recommendedFreeSpace, minimumRequiredSpace);
    }
    
    /**
     * Check available disk space in vault directory
     */
    public DiskSpaceStatus checkDiskSpace() {
        try {
            File vaultDir = new File(vaultDirectory);
            long freeSpace = vaultDir.getFreeSpace();
            long totalSpace = vaultDir.getTotalSpace();
            long usableSpace = vaultDir.getUsableSpace();
            
            SpaceInfo spaceInfo = getSpaceInfo();
            
            boolean hasEnoughSpace = freeSpace >= spaceInfo.getMinimumRequiredSpace();
            boolean hasRecommendedSpace = freeSpace >= spaceInfo.getRecommendedFreeSpace();
            
            return new DiskSpaceStatus(freeSpace, totalSpace, usableSpace, 
                                     hasEnoughSpace, hasRecommendedSpace, spaceInfo);
        } catch (Exception e) {
            // Return default status if unable to check
            return new DiskSpaceStatus(0, 0, 0, false, false, getSpaceInfo());
        }
    }
    
    /**
     * Get estimated space needed for a new file
     */
    public long getEstimatedSpaceForFile(long fileSize) {
        // Encryption overhead + metadata storage
        return (long) (fileSize * 1.2) + 1024; // 20% overhead + 1KB metadata
    }
    
    /**
     * Check if there's enough space for a new file
     */
    public boolean canStoreFile(long fileSize) {
        try {
            File vaultDir = new File(vaultDirectory);
            long freeSpace = vaultDir.getFreeSpace();
            long estimatedSpace = getEstimatedSpaceForFile(fileSize);
            
            return freeSpace >= estimatedSpace;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if there's enough space for a new file (overloaded for File parameter)
     */
    public boolean canStoreFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        return canStoreFile(file.length());
    }
    
    /**
     * Re-encrypt all vault files with a new password
     * This is called when the admin changes their password
     */
    public boolean reEncryptAllFiles(String oldPassword, String oldSalt, String newPassword, String newSalt) {
        try {
            // Get all vault files
            List<VaultFile> allFiles = getAllVaultFiles();
            
            if (allFiles.isEmpty()) {
                return true; // No files to re-encrypt
            }
            
            // Generate old and new encryption keys
            SecretKey oldKey = SecurityUtil.generateKeyFromPassword(oldPassword, oldSalt);
            SecretKey newKey = SecurityUtil.generateKeyFromPassword(newPassword, newSalt);
            
            System.out.println("Re-encrypting " + allFiles.size() + " files with new password...");
            
            for (VaultFile vaultFile : allFiles) {
                try {
                    File encryptedFile = new File(vaultFile.getEncryptedPath());
                    if (!encryptedFile.exists()) {
                        System.err.println("Warning: Encrypted file not found: " + vaultFile.getEncryptedPath());
                        continue;
                    }
                    
                    // Read and decrypt with old key
                    byte[] encryptedData = FileUtils.readFileToByteArray(encryptedFile);
                    byte[] decryptedData = SecurityUtil.decrypt(encryptedData, oldKey);
                    
                    // Encrypt with new key
                    byte[] newEncryptedData = SecurityUtil.encrypt(decryptedData, newKey);
                    
                    // Write back to the same file
                    FileUtils.writeByteArrayToFile(encryptedFile, newEncryptedData);
                    
                    System.out.println("Re-encrypted: " + vaultFile.getOriginalName());
                    
                } catch (Exception e) {
                    System.err.println("Failed to re-encrypt file: " + vaultFile.getOriginalName() + " - " + e.getMessage());
                    return false;
                }
            }
            
            // Update the current encryption key
            this.encryptionKey = newKey;
            
            System.out.println("Successfully re-encrypted all files.");
            return true;
            
        } catch (Exception e) {
            SecureErrorHandler.handleCryptoError(e);
            return false;
        }
    }

    /**
     * Simple recovery method that tries to decrypt a file with default credentials
     * This method does NOT call retrieveFile to avoid recursion
     */
    private File tryRecoveryDecryption(VaultFile vaultFile, String outputDirectory) {
        try {
            System.out.println("Attempting recovery decryption for: " + vaultFile.getOriginalName());
            
            // Try with default admin credentials (admin123)
            String defaultPassword = "admin123";
            String defaultUsername = "admin";
            
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            // First, try to get all admins to find one that might work
            List<Admin> allAdmins = dbManager.getAllAdmins();
            
            // Try with default credentials on all admins (in case username changed)
            for (Admin admin : allAdmins) {
                try {
                    System.out.println("Trying recovery with user: " + admin.getUsername());
                    
                    // Try with the admin's current salt but default password
                    SecretKey recoveryKey = SecurityUtil.generateKeyFromPassword(defaultPassword, admin.getSalt());
                    
                    if (tryDecryptWithKey(vaultFile, outputDirectory, recoveryKey, "default_password_" + admin.getUsername())) {
                        this.encryptionKey = recoveryKey;
                        return getLastRecoveredFile();
                    }
                    
                } catch (Exception e) {
                    System.out.println("Recovery attempt failed for " + admin.getUsername() + ": " + e.getMessage());
                }
            }
            
            // If that fails, try authenticating with default credentials
            Admin defaultAdmin = dbManager.authenticateAdmin(defaultUsername, defaultPassword);
            if (defaultAdmin != null) {
                System.out.println("Trying recovery with authenticated default admin...");
                
                SecretKey recoveryKey = SecurityUtil.generateKeyFromPassword(defaultPassword, defaultAdmin.getSalt());
                
                if (tryDecryptWithKey(vaultFile, outputDirectory, recoveryKey, "default_auth")) {
                    this.encryptionKey = recoveryKey;
                    return getLastRecoveredFile();
                }
            }
            
            // If all fails, try with a standard salt (in case file was encrypted with standard settings)
            try {
                System.out.println("Trying recovery with standard salt...");
                String standardSalt = "VaultAppSalt"; // Default salt as string
                SecretKey recoveryKey = SecurityUtil.generateKeyFromPassword(defaultPassword, standardSalt);
                
                if (tryDecryptWithKey(vaultFile, outputDirectory, recoveryKey, "standard_salt")) {
                    this.encryptionKey = recoveryKey;
                    return getLastRecoveredFile();
                }
            } catch (Exception e) {
                System.out.println("Standard salt recovery failed: " + e.getMessage());
            }
            
            System.out.println("All recovery attempts failed for: " + vaultFile.getOriginalName());
            return null;
            
        } catch (Exception e) {
            SecureErrorHandler.handleCryptoError(e);
            return null;
        }
    }
    
    private File lastRecoveredFile = null;
    
    private boolean tryDecryptWithKey(VaultFile vaultFile, String outputDirectory, SecretKey key, String method) {
        try {
            // Try to decrypt directly without calling retrieveFile
            File encryptedFile = new File(vaultFile.getEncryptedPath());
            if (!encryptedFile.exists()) {
                System.out.println("Encrypted file not found: " + vaultFile.getEncryptedPath());
                return false;
            }
            
            // Read and decrypt
            byte[] encryptedData = FileUtils.readFileToByteArray(encryptedFile);
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, key);
            
            // Create output file
            String outputPath = outputDirectory + File.separator + vaultFile.getOriginalName();
            File outputFile = new File(outputPath);
            
            // Ensure unique filename
            int counter = 1;
            while (outputFile.exists()) {
                String nameWithoutExt = getFileNameWithoutExtension(vaultFile.getOriginalName());
                String extension = getFileExtension(vaultFile.getOriginalName());
                String newName = nameWithoutExt + "_recovery_" + method + "_" + counter + 
                               (extension.isEmpty() ? "" : "." + extension);
                outputFile = new File(outputDirectory + File.separator + newName);
                counter++;
            }
            
            // Write decrypted file
            FileUtils.writeByteArrayToFile(outputFile, decryptedData);
            System.out.println("Recovery successful with method '" + method + "'! File: " + outputFile.getAbsolutePath());
            
            lastRecoveredFile = outputFile;
            return true;
            
        } catch (Exception e) {
            System.out.println("Decryption failed with method '" + method + "': " + e.getMessage());
            return false;
        }
    }
    
    private File getLastRecoveredFile() {
        return lastRecoveredFile;
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
    
    /**
     * Inner class for space information
     */
    public static class SpaceInfo {
        private final int fileCount;
        private final long originalSize;
        private final long totalUsedSpace;
        private final long recommendedFreeSpace;
        private final long minimumRequiredSpace;
        
        public SpaceInfo(int fileCount, long originalSize, long totalUsedSpace, 
                        long recommendedFreeSpace, long minimumRequiredSpace) {
            this.fileCount = fileCount;
            this.originalSize = originalSize;
            this.totalUsedSpace = totalUsedSpace;
            this.recommendedFreeSpace = recommendedFreeSpace;
            this.minimumRequiredSpace = minimumRequiredSpace;
        }
        
        public int getFileCount() { return fileCount; }
        public long getOriginalSize() { return originalSize; }
        public long getTotalUsedSpace() { return totalUsedSpace; }
        public long getRecommendedFreeSpace() { return recommendedFreeSpace; }
        public long getMinimumRequiredSpace() { return minimumRequiredSpace; }
        
        public String getFormattedOriginalSize() { return formatSize(originalSize); }
        public String getFormattedUsedSpace() { return formatSize(totalUsedSpace); }
        public String getFormattedRecommendedSpace() { return formatSize(recommendedFreeSpace); }
        public String getFormattedMinimumSpace() { return formatSize(minimumRequiredSpace); }
        
        private String formatSize(long size) {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Inner class for disk space status
     */
    public static class DiskSpaceStatus {
        private final long freeSpace;
        private final long totalSpace;
        private final long usableSpace;
        private final boolean hasEnoughSpace;
        private final boolean hasRecommendedSpace;
        private final SpaceInfo spaceInfo;
        
        public DiskSpaceStatus(long freeSpace, long totalSpace, long usableSpace,
                              boolean hasEnoughSpace, boolean hasRecommendedSpace,
                              SpaceInfo spaceInfo) {
            this.freeSpace = freeSpace;
            this.totalSpace = totalSpace;
            this.usableSpace = usableSpace;
            this.hasEnoughSpace = hasEnoughSpace;
            this.hasRecommendedSpace = hasRecommendedSpace;
            this.spaceInfo = spaceInfo;
        }
        
        public long getFreeSpace() { return freeSpace; }
        public long getTotalSpace() { return totalSpace; }
        public long getUsableSpace() { return usableSpace; }
        public boolean hasEnoughSpace() { return hasEnoughSpace; }
        public boolean hasRecommendedSpace() { return hasRecommendedSpace; }
        public SpaceInfo getSpaceInfo() { return spaceInfo; }
        
        public String getFormattedFreeSpace() { return spaceInfo.formatSize(freeSpace); }
        public String getFormattedTotalSpace() { return spaceInfo.formatSize(totalSpace); }
        public String getFormattedUsableSpace() { return spaceInfo.formatSize(usableSpace); }
        
        public double getUsagePercentage() {
            if (totalSpace == 0) return 0.0;
            return ((double) (totalSpace - freeSpace) / totalSpace) * 100.0;
        }
    }
}
