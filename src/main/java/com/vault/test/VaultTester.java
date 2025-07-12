package com.vault.test;

import com.vault.model.Admin;
import com.vault.model.VaultFile;
import com.vault.service.VaultService;
import com.vault.util.DatabaseManager;
import com.vault.util.SecurityUtil;

import java.io.File;
import java.util.List;

/**
 * Comprehensive test utility to verify vault functionality
 */
public class VaultTester {
    
    public static void main(String[] args) {
        System.out.println("=== VAULT COMPREHENSIVE TEST ===");
        System.out.println("Testing the three scenarios:\n");
        
        try {
            // Initialize components
            DatabaseManager dbManager = DatabaseManager.getInstance();
            VaultService vaultService = VaultService.getInstance();
            
            System.out.println("1. TEST CURRENT STATE: Try opening files with current login");
            System.out.println("=================================================================");
            
            // Test authentication with default credentials
            Admin admin = dbManager.authenticateAdmin("admin", "admin123");
            if (admin == null) {
                System.out.println("❌ FAILED: Cannot authenticate with admin:admin123");
                System.out.println("   This suggests credentials have been changed.");
                return;
            }
            
            System.out.println("✅ SUCCESS: Authentication successful");
            System.out.println("   Username: " + admin.getUsername());
            System.out.println("   Salt: " + admin.getSalt().substring(0, 20) + "...");
            
            // Set encryption key
            vaultService.setEncryptionKey("admin123", admin.getSalt());
            System.out.println("✅ SUCCESS: Encryption key set");
            
            // Get all vault files
            List<VaultFile> files = vaultService.getAllVaultFiles();
            System.out.println("✅ SUCCESS: Found " + files.size() + " files in vault");
            
            if (files.isEmpty()) {
                System.out.println("   No files to test. Upload some files first.");
            } else {
                // Test opening the first file
                VaultFile testFile = files.get(0);
                System.out.println("   Testing file: " + testFile.getOriginalName());
                System.out.println("   Encrypted path: " + testFile.getEncryptedPath());
                
                File encryptedFile = new File(testFile.getEncryptedPath());
                if (!encryptedFile.exists()) {
                    System.out.println("❌ FAILED: Encrypted file does not exist on disk");
                } else {
                    System.out.println("✅ SUCCESS: Encrypted file exists (" + encryptedFile.length() + " bytes)");
                    
                    try {
                        // Try to decrypt the file
                        String tempDir = System.getProperty("java.io.tmpdir");
                        File decryptedFile = vaultService.retrieveFile(testFile, tempDir);
                        
                        if (decryptedFile != null && decryptedFile.exists()) {
                            System.out.println("✅ SUCCESS: File decryption successful!");
                            System.out.println("   Temp file: " + decryptedFile.getAbsolutePath());
                            System.out.println("   Size: " + decryptedFile.length() + " bytes");
                            
                            // Clean up
                            decryptedFile.delete();
                        } else {
                            System.out.println("❌ FAILED: File decryption returned null or file doesn't exist");
                        }
                    } catch (Exception e) {
                        System.out.println("❌ FAILED: File decryption failed");
                        System.out.println("   Error: " + e.getMessage());
                        if (e.getMessage().contains("padding") || e.getMessage().contains("decrypt")) {
                            System.out.println("   This indicates the file was encrypted with different credentials!");
                        }
                    }
                }
            }
            
            System.out.println("\n2. TEST PASSWORD CHANGE: Change admin password and verify files still work");
            System.out.println("=======================================================================");
            
            String newPassword = "newpass123";
            System.out.println("   Attempting to change password from 'admin123' to '" + newPassword + "'");
            
            // This would normally be done through the UI, but we can test the underlying logic
            try {
                // Check if re-encryption works
                if (!files.isEmpty()) {
                    boolean canReEncrypt = vaultService.reEncryptAllFiles(
                        "admin123", 
                        admin.getSalt(), 
                        newPassword, 
                        SecurityUtil.generateSalt()
                    );
                    
                    if (canReEncrypt) {
                        System.out.println("✅ SUCCESS: File re-encryption completed");
                        System.out.println("   All " + files.size() + " files have been re-encrypted with new password");
                    } else {
                        System.out.println("❌ FAILED: File re-encryption failed");
                    }
                } else {
                    System.out.println("⚠️  SKIPPED: No files to re-encrypt");
                }
            } catch (Exception e) {
                System.out.println("❌ FAILED: Re-encryption process failed");
                System.out.println("   Error: " + e.getMessage());
            }
            
            System.out.println("\n3. TEST RECOVERY: Automatic recovery when decryption fails");
            System.out.println("==========================================================");
            
            if (!files.isEmpty()) {
                VaultFile testFile = files.get(0);
                System.out.println("   Testing recovery for: " + testFile.getOriginalName());
                
                // Simulate wrong credentials by changing the encryption key
                try {
                    String wrongPassword = "wrongpassword";
                    String wrongSalt = SecurityUtil.generateSalt();
                    vaultService.setEncryptionKey(wrongPassword, wrongSalt);
                    System.out.println("   Set wrong encryption key to simulate credential mismatch");
                    
                    // Try to decrypt - this should trigger recovery
                    String tempDir = System.getProperty("java.io.tmpdir");
                    File recoveredFile = vaultService.retrieveFile(testFile, tempDir);
                    
                    if (recoveredFile != null && recoveredFile.exists()) {
                        System.out.println("✅ SUCCESS: Automatic recovery worked!");
                        System.out.println("   File was recovered despite wrong credentials");
                        recoveredFile.delete();
                    } else {
                        System.out.println("❌ FAILED: Recovery did not work");
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ FAILED: Recovery attempt failed");
                    System.out.println("   Error: " + e.getMessage());
                    
                    // Check if recovery was attempted
                    if (e.getMessage().contains("recovery")) {
                        System.out.println("   Recovery was attempted but failed");
                    } else {
                        System.out.println("   Recovery might not have been triggered");
                    }
                }
            } else {
                System.out.println("⚠️  SKIPPED: No files to test recovery with");
            }
            
            System.out.println("\n=== TEST SUMMARY ===");
            System.out.println("All tests completed. Check results above for any failures.");
            System.out.println("If any tests failed, the 'Failed to decrypt data' issue may still exist.");
            
        } catch (Exception e) {
            System.out.println("❌ FATAL: Test suite failed to run");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
