package com.vault.util;

import com.vault.model.Admin;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * Automated diagnostic for the specific problematic file
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
public class AutoDiagnostic {
    
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.securevault";
    private static final String FILES_DIR = VAULT_DIR + "/files";
    private static final String PROBLEM_FILE = "Z-KhnmhiOzFMR0CIRd0_XA.enc"; // The 82MB video file
    
    public static void main(String[] args) {
        System.out.println("=== Automated Diagnostic for " + PROBLEM_FILE + " ===");
        System.out.println("Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.");
        System.out.println();
        
        try {
            File problemFile = new File(FILES_DIR, PROBLEM_FILE);
            if (!problemFile.exists()) {
                System.out.println("Problem file not found: " + problemFile.getAbsolutePath());
                return;
            }
            
            System.out.println("File: " + problemFile.getName());
            System.out.println("Size: " + (problemFile.length() / 1024 / 1024) + " MB");
            System.out.println("Path: " + problemFile.getAbsolutePath());
            System.out.println();
            
            // Read encrypted data
            byte[] encryptedData = Files.readAllBytes(problemFile.toPath());
            System.out.println("Successfully read " + encryptedData.length + " bytes");
            System.out.println();
            
            // Extended list of passwords to try
            String[] passwords = {
                // Common passwords
                "admin", "password", "123456", "vault", "secure", "default",
                "Lintshiwe", "lintshiwe", "Admin", "Password", "Vault", "Secure",
                "admin123", "vault123", "secure123", "password123",
                
                // Variations of the name
                "Lintshiwe123", "lintshiwe123", "LINTSHIWE", "Lintshiwe2025",
                "lintshiwe2025", "LintshiweNtoampi", "lintshiwentoampi",
                
                // Common combinations
                "admin2025", "vault2025", "secure2025", "password2025",
                "AdminPassword", "VaultPassword", "SecureVault",
                
                // Default application passwords
                "vaultapp", "VaultApp", "securevault", "SecureVault",
                "app123", "vault_admin", "admin_vault",
                
                // Additional common patterns
                "123", "1234", "12345", "qwerty", "abc123", "password1",
                "admin1", "test", "user", "guest", "root", "pass",
                "secret", "security", "access", "login", "key",
                
                // Date variations
                "2024", "2023", "2025", "01", "12", "january", "december",
                
                // Personal variations (add what you might remember)
                "ntoam", "Ntoam", "NTOAM", "ntoampi", "Ntoampi",
                "desktop", "Desktop", "projects", "Projects",
                
                // Simple patterns
                "aaa", "000", "111", "999", "abc", "xyz",
                "temp", "temporary", "backup", "restore",
                
                // Empty and single character
                "", " ", "a", "1", "0"
            };
            
            String[] usernames = {
                "admin", "Lintshiwe", "lintshiwe", "user", "vault", "root"
            };
            
            boolean recovered = false;
            int attempts = 0;
            
            // Get database admins first
            try {
                com.vault.util.DatabaseManager dbManager = com.vault.util.DatabaseManager.getInstance();
                java.util.List<Admin> admins = dbManager.getAllAdmins();
                System.out.println("Database admins found: " + admins.size());
                for (Admin admin : admins) {
                    System.out.println("  - " + admin.getUsername());
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println("Error reading database: " + e.getMessage());
            }
            
            // Try all combinations
            System.out.println("Testing password combinations...");
            for (String username : usernames) {
                for (String password : passwords) {
                    attempts++;
                    System.out.print("Attempt " + attempts + ": " + username + "/" + password + " ... ");
                    
                    if (tryDecrypt(encryptedData, username, password)) {
                        System.out.println("✓ SUCCESS!");
                        System.out.println();
                        System.out.println("===== RECOVERY SUCCESSFUL =====");
                        System.out.println("Username: " + username);
                        System.out.println("Password: " + password);
                        System.out.println("The file can be decrypted with these credentials.");
                        System.out.println("===============================");
                        recovered = true;
                        break;
                    } else {
                        System.out.println("Failed");
                    }
                }
                if (recovered) break;
            }
            
            if (!recovered) {
                System.out.println();
                System.out.println("===== DIAGNOSTIC COMPLETE =====");
                System.out.println("Tested " + attempts + " password combinations");
                System.out.println("❌ No working password found");
                System.out.println();
                System.out.println("Possible causes:");
                System.out.println("1. File was encrypted with a custom password not in our test list");
                System.out.println("2. File was encrypted before a password change");
                System.out.println("3. File corruption during encryption or storage");
                System.out.println("4. Different encryption method was used");
                System.out.println();
                System.out.println("Recommendations:");
                System.out.println("1. Try to remember the original password used when adding the file");
                System.out.println("2. Check if you have backups of the original file");
                System.out.println("3. Consider re-adding the file to the vault if available");
                System.out.println("===============================");
            }
            
        } catch (Exception e) {
            // Use secure error handling instead of printStackTrace
            com.vault.config.SecurityConfig.secureLog(java.util.logging.Level.SEVERE, "Diagnostic error: {0}", e.getClass().getSimpleName());
            System.out.println("Error during diagnostic: " + SecureErrorHandler.handleUnexpectedError(e));
        }
    }
    
    private static boolean tryDecrypt(byte[] encryptedData, String username, String password) {
        try {
            javax.crypto.SecretKey key = com.vault.util.SecurityUtil.generateKeyFromPassword(password, username);
            byte[] decryptedData = com.vault.util.SecurityUtil.decrypt(encryptedData, key);
            return decryptedData != null && decryptedData.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
