package com.vault.util;

import com.vault.model.VaultFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Direct file access utility - bypasses authentication
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
public class DirectFileAccess {
    
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.securevault";
    private static final String FILES_DIR = VAULT_DIR + "/files";
    private static final String OUTPUT_DIR = System.getProperty("user.home") + "/Desktop/VaultOutput";
    
    public static void main(String[] args) {
        System.out.println("=== Direct Vault File Access ===");
        System.out.println("Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.");
        System.out.println();
        
        // Create output directory
        createOutputDirectory();
        
        // Known working credentials from diagnostic
        String username = "user";
        String password = "december";
        
        System.out.println("Using credentials: " + username + "/" + password);
        System.out.println("Output directory: " + OUTPUT_DIR);
        System.out.println();
        
        try {
            // Generate encryption key
            SecretKey key = SecurityUtil.generateKeyFromPassword(password, username);
            System.out.println("Encryption key generated successfully");
            
            // List all encrypted files in vault directory
            File vaultDir = new File(FILES_DIR);
            if (!vaultDir.exists()) {
                System.out.println("Vault directory not found: " + FILES_DIR);
                return;
            }
            
            File[] encryptedFiles = vaultDir.listFiles((dir, name) -> name.endsWith(".enc"));
            if (encryptedFiles == null || encryptedFiles.length == 0) {
                System.out.println("No encrypted files found in vault");
                return;
            }
            
            System.out.println("Found " + encryptedFiles.length + " encrypted files:");
            for (File file : encryptedFiles) {
                System.out.println("  - " + file.getName() + " (" + (file.length() / 1024 / 1024) + " MB)");
            }
            System.out.println();
            
            // Try to decrypt each file
            for (File encryptedFile : encryptedFiles) {
                decryptAndSaveFile(encryptedFile, key);
            }
            
            // Also try to get files from database
            tryDatabaseFiles(key);
            
        } catch (Exception e) {
            System.err.println("Error in direct file access: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createOutputDirectory() {
        try {
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                System.out.println("Created output directory: " + OUTPUT_DIR);
            }
        } catch (Exception e) {
            System.err.println("Failed to create output directory: " + e.getMessage());
        }
    }
    
    private static void decryptAndSaveFile(File encryptedFile, SecretKey key) {
        System.out.println("Processing: " + encryptedFile.getName());
        
        try {
            // Read encrypted data
            byte[] encryptedData = Files.readAllBytes(encryptedFile.toPath());
            System.out.println("  Read " + encryptedData.length + " bytes");
            
            // Decrypt data
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, key);
            System.out.println("  Decrypted " + decryptedData.length + " bytes");
            
            // Determine output filename (remove .enc extension and add timestamp)
            String originalName = encryptedFile.getName().replace(".enc", "");
            String outputName = originalName + "_decrypted_" + System.currentTimeMillis();
            
            // Try to guess file extension based on file header
            String extension = guessFileExtension(decryptedData);
            if (!extension.isEmpty()) {
                outputName += "." + extension;
            }
            
            File outputFile = new File(OUTPUT_DIR, outputName);
            
            // Write decrypted file
            Files.write(outputFile.toPath(), decryptedData);
            
            System.out.println("  ✓ SUCCESS: Saved to " + outputFile.getAbsolutePath());
            System.out.println("  File size: " + (outputFile.length() / 1024 / 1024) + " MB");
            
            // Try to open the file with default application
            tryOpenFile(outputFile);
            
        } catch (Exception e) {
            System.out.println("  ❌ FAILED: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void tryDatabaseFiles(SecretKey key) {
        System.out.println("Checking database for vault files...");
        
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            List<VaultFile> vaultFiles = dbManager.getAllVaultFiles();
            
            System.out.println("Found " + vaultFiles.size() + " files in database:");
            
            for (VaultFile vaultFile : vaultFiles) {
                System.out.println("Database file: " + vaultFile.getOriginalName());
                System.out.println("  Encrypted path: " + vaultFile.getEncryptedPath());
                System.out.println("  Size: " + (vaultFile.getFileSize() / 1024 / 1024) + " MB");
                System.out.println("  Type: " + vaultFile.getFileType());
                
                // Try to decrypt this file
                File encryptedFile = new File(vaultFile.getEncryptedPath());
                if (encryptedFile.exists()) {
                    // Use original name for output
                    decryptDatabaseFile(vaultFile, encryptedFile, key);
                } else {
                    System.out.println("  ❌ Encrypted file not found: " + vaultFile.getEncryptedPath());
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            System.out.println("Error accessing database: " + e.getMessage());
        }
    }
    
    private static void decryptDatabaseFile(VaultFile vaultFile, File encryptedFile, SecretKey key) {
        try {
            // Read encrypted data
            byte[] encryptedData = Files.readAllBytes(encryptedFile.toPath());
            
            // Decrypt data
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, key);
            
            // Use original filename
            String outputName = vaultFile.getOriginalName();
            File outputFile = new File(OUTPUT_DIR, outputName);
            
            // Ensure unique filename
            int counter = 1;
            while (outputFile.exists()) {
                String nameWithoutExt = getFileNameWithoutExtension(vaultFile.getOriginalName());
                String extension = getFileExtension(vaultFile.getOriginalName());
                String newName = nameWithoutExt + "_" + counter + 
                               (extension.isEmpty() ? "" : "." + extension);
                outputFile = new File(OUTPUT_DIR, newName);
                counter++;
            }
            
            // Write decrypted file
            Files.write(outputFile.toPath(), decryptedData);
            
            System.out.println("  ✓ SUCCESS: Restored " + outputFile.getAbsolutePath());
            
            // Try to open the file
            tryOpenFile(outputFile);
            
        } catch (Exception e) {
            System.out.println("  ❌ FAILED to decrypt: " + e.getMessage());
        }
    }
    
    private static String guessFileExtension(byte[] data) {
        if (data.length < 8) return "";
        
        // Check file signatures
        String hex = bytesToHex(data, 8);
        
        if (hex.startsWith("FFD8FF")) return "jpg";
        if (hex.startsWith("89504E47")) return "png";
        if (hex.startsWith("47494638")) return "gif";
        if (hex.startsWith("25504446")) return "pdf";
        if (hex.startsWith("504B0304")) return "zip"; // or docx, xlsx
        if (hex.startsWith("D0CF11E0")) return "doc"; // or xls
        if (hex.startsWith("00000018") || hex.startsWith("00000020")) return "mp4";
        if (hex.startsWith("52494646")) return "wav"; // or avi
        if (hex.startsWith("49443303") || hex.startsWith("FFFB")) return "mp3";
        
        return "";
    }
    
    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(length, bytes.length); i++) {
            result.append(String.format("%02X", bytes[i]));
        }
        return result.toString();
    }
    
    private static void tryOpenFile(File file) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
                System.out.println("  📂 Opened file with default application");
            }
        } catch (Exception e) {
            System.out.println("  ⚠️ Could not open file automatically: " + e.getMessage());
        }
    }
    
    private static String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
    
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}
