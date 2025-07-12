package com.vault.util;

import com.vault.util.SecurityUtil;

import java.io.File;
import java.nio.file.Files;

/**
 * Recovery tool for smaller encrypted files to determine password pattern
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
public class SmallFileRecovery {
    
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.securevault";
    private static final String FILES_DIR = VAULT_DIR + "/files";
    
    public static void main(String[] args) {
        System.out.println("=== Small Files Recovery Tool ===");
        System.out.println("Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.");
        System.out.println();
        
        String[] smallFiles = {
            "7u7uvVmjPBQWU4L90MEQhw.enc",
            "NQPRiNcxYW7Vvy3F6sM5bA.enc"
        };
        
        // Comprehensive password list
        String[] passwords = {
            // Basic passwords
            "", "admin", "password", "123456", "vault", "secure", "default",
            "Lintshiwe", "lintshiwe", "Admin", "Password", "Vault", "Secure",
            "admin123", "vault123", "secure123", "password123",
            
            // Name variations
            "Lintshiwe123", "lintshiwe123", "LINTSHIWE", "Lintshiwe2025",
            "lintshiwe2025", "LintshiweNtoampi", "lintshiwentoampi",
            "ntoam", "Ntoam", "NTOAM", "ntoampi", "Ntoampi",
            
            // Years and dates
            "2024", "2023", "2025", "2022", "2021", "2020",
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
            
            // Common patterns
            "123", "1234", "12345", "123456789", "qwerty", "abc123",
            "password1", "admin1", "test", "user", "guest", "root",
            
            // Application specific
            "vaultapp", "VaultApp", "securevault", "SecureVault",
            "vault_admin", "admin_vault", "app123",
            
            // Simple single characters and numbers
            "a", "b", "c", "1", "2", "3", "0", "9",
            
            // Empty and spaces
            " ", "  ", "   "
        };
        
        String[] usernames = {
            "admin", "Lintshiwe", "lintshiwe", "user", "vault", "root", "ntoam"
        };
        
        for (String filename : smallFiles) {
            System.out.println("=== Testing file: " + filename + " ===");
            
            File file = new File(FILES_DIR, filename);
            if (!file.exists()) {
                System.out.println("File not found: " + filename);
                continue;
            }
            
            try {
                byte[] encryptedData = Files.readAllBytes(file.toPath());
                System.out.println("File size: " + encryptedData.length + " bytes");
                
                boolean recovered = false;
                int attempts = 0;
                
                for (String username : usernames) {
                    for (String password : passwords) {
                        attempts++;
                        if (tryDecrypt(encryptedData, username, password)) {
                            System.out.println();
                            System.out.println("🎉 SUCCESS! File: " + filename);
                            System.out.println("Username: " + username);
                            System.out.println("Password: '" + password + "'");
                            System.out.println("Attempts: " + attempts);
                            System.out.println("=================================");
                            recovered = true;
                            break;
                        }
                        
                        if (attempts % 50 == 0) {
                            System.out.print(".");
                        }
                    }
                    if (recovered) break;
                }
                
                if (!recovered) {
                    System.out.println();
                    System.out.println("❌ Could not recover: " + filename);
                    System.out.println("Tested " + attempts + " combinations");
                }
                
            } catch (Exception e) {
                System.out.println("Error processing " + filename + ": " + e.getMessage());
            }
            
            System.out.println();
        }
        
        System.out.println("Recovery scan complete.");
    }
    
    private static boolean tryDecrypt(byte[] encryptedData, String username, String password) {
        try {
            javax.crypto.SecretKey key = SecurityUtil.generateKeyFromPassword(password, username);
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, key);
            return decryptedData != null && decryptedData.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
