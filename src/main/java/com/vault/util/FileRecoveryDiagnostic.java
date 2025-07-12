package com.vault.util;

import com.vault.model.Admin;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;

/**
 * Diagnostic tool to help recover encrypted files by trying different passwords
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
public class FileRecoveryDiagnostic {
    
    private static final String VAULT_DIR = System.getProperty("user.home") + "/.securevault";
    private static final String FILES_DIR = VAULT_DIR + "/files";
    
    public static void main(String[] args) {
        System.out.println("=== Secure Vault File Recovery Diagnostic ===");
        System.out.println("Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            // List encrypted files
            File filesDir = new File(FILES_DIR);
            if (!filesDir.exists()) {
                System.out.println("No vault files directory found.");
                return;
            }
            
            File[] encFiles = filesDir.listFiles((dir, name) -> name.endsWith(".enc"));
            if (encFiles == null || encFiles.length == 0) {
                System.out.println("No encrypted files found in vault.");
                return;
            }
            
            System.out.println("Found " + encFiles.length + " encrypted files:");
            for (int i = 0; i < encFiles.length; i++) {
                System.out.println((i + 1) + ". " + encFiles[i].getName() + 
                    " (" + (encFiles[i].length() / 1024) + " KB)");
            }
            
            System.out.print("\nSelect file number to diagnose (1-" + encFiles.length + "): ");
            int choice = scanner.nextInt() - 1;
            scanner.nextLine(); // consume newline
            
            if (choice < 0 || choice >= encFiles.length) {
                System.out.println("Invalid selection.");
                return;
            }
            
            File selectedFile = encFiles[choice];
            System.out.println("\nDiagnosing: " + selectedFile.getName());
            System.out.println("File size: " + selectedFile.length() + " bytes");
            
            // Read encrypted data
            byte[] encryptedData = Files.readAllBytes(selectedFile.toPath());
            System.out.println("Successfully read " + encryptedData.length + " bytes");
            
            // Try different password combinations
            System.out.println("\nTrying common passwords...");
            
            String[] commonPasswords = {
                "admin", "password", "123456", "vault", "secure",
                "Lintshiwe", "lintshiwe", "Admin", "Password",
                "admin123", "vault123", "secure123"
            };
            
            String[] commonUsernames = {
                "admin", "Lintshiwe", "lintshiwe", "user", "vault"
            };
            
            boolean recovered = false;
            
            // Try current admin usernames from database with common passwords
            try {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                List<Admin> admins = dbManager.getAllAdmins();
                System.out.println("Found " + admins.size() + " admin(s) in database");
                
                for (Admin admin : admins) {
                    String username = admin.getUsername();
                    System.out.println("Trying passwords for database admin: " + username);
                    
                    for (String password : commonPasswords) {
                        if (tryDecrypt(encryptedData, username, password)) {
                            System.out.println("✓ SUCCESS: File can be decrypted with " + username + "/" + password);
                            recovered = true;
                            break;
                        }
                    }
                    if (recovered) break;
                }
            } catch (Exception e) {
                System.out.println("Error reading database: " + e.getMessage());
            }
            
            if (!recovered) {
                // Try common password combinations
                for (String username : commonUsernames) {
                    for (String password : commonPasswords) {
                        System.out.println("Trying: " + username + "/" + password);
                        if (tryDecrypt(encryptedData, username, password)) {
                            System.out.println("✓ SUCCESS: File can be decrypted with " + username + "/" + password);
                            recovered = true;
                            break;
                        }
                    }
                    if (recovered) break;
                }
            }
            
            if (!recovered) {
                System.out.println("\n❌ Could not recover file with any common passwords.");
                System.out.println("The file may have been encrypted with a custom password.");
                System.out.print("Would you like to try a custom password? (y/n): ");
                String response = scanner.nextLine();
                
                if (response.toLowerCase().startsWith("y")) {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    
                    if (tryDecrypt(encryptedData, username, password)) {
                        System.out.println("✓ SUCCESS: File can be decrypted with " + username + "/" + password);
                        
                        // Offer to show the working password
                        System.out.println("✓ SUCCESS: File can be decrypted with " + username + "/" + password);
                        System.out.println("You can use this password to access the file manually.");
                        System.out.println("Consider updating your admin password to match this one if needed.");
                    } else {
                        System.out.println("❌ Custom password also failed.");
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error during diagnostic: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
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
