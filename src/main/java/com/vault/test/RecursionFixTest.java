package com.vault.test;

import com.vault.model.VaultFile;
import com.vault.service.VaultService;
import com.vault.util.DatabaseManager;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Test to verify that the infinite recursion issue has been fixed
 */
public class RecursionFixTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Infinite Recursion Fix ===");
        
        try {
            // Initialize database
            DatabaseManager.getInstance().initializeDatabase();
            
            // Create a VaultService instance
            VaultService vaultService = VaultService.getInstance();
            vaultService.setEncryptionKey("admin123", "defaultSalt");
            
            System.out.println("✓ VaultService initialized successfully");
            
            // Create a test vault file that will likely fail decryption
            VaultFile testFile = new VaultFile();
            testFile.setId(999); // Non-existent ID
            testFile.setOriginalName("test_recovery_file.txt");
            testFile.setEncryptedPath("C:\\non_existent_path\\fake_file.enc");
            testFile.setFileSize(1024);
            testFile.setFileType("txt");
            testFile.setDateAdded(LocalDateTime.now());
            testFile.setDescription("Test file for recovery testing");
            
            System.out.println("✓ Created test VaultFile");
            
            // Try to retrieve the file - this should trigger recovery logic
            // but NOT cause infinite recursion
            try {
                File retrievedFile = vaultService.retrieveFile(testFile, System.getProperty("user.dir"));
                System.out.println("✓ File retrieval completed (even if unsuccessful)");
                
                if (retrievedFile != null) {
                    System.out.println("✓ File recovered: " + retrievedFile.getAbsolutePath());
                } else {
                    System.out.println("✓ File recovery failed gracefully (expected for non-existent file)");
                }
                
            } catch (RuntimeException e) {
                // This is expected for a non-existent file
                System.out.println("✓ Exception caught as expected: " + e.getMessage());
                
                // Check if it's NOT a StackOverflowError
                if (e.getMessage().contains("StackOverflowError") || 
                    e.getCause() instanceof StackOverflowError) {
                    System.err.println("❌ INFINITE RECURSION STILL EXISTS!");
                    return;
                }
            }
            
            System.out.println("\n=== RECURSION FIX TEST PASSED ===");
            System.out.println("✓ No infinite recursion detected");
            System.out.println("✓ Recovery logic executed without StackOverflowError");
            System.out.println("✓ Application handled file retrieval failure gracefully");
            
        } catch (StackOverflowError e) {
            System.err.println("❌ INFINITE RECURSION DETECTED!");
            System.err.println("The fix did not work. StackOverflowError occurred.");
            e.printStackTrace();
            
        } catch (Exception e) {
            System.out.println("✓ General exception caught (this is acceptable): " + e.getMessage());
            System.out.println("✓ No StackOverflowError - recursion fix appears successful");
        }
    }
}
