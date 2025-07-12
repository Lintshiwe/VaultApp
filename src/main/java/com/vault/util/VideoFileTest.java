package com.vault.util;

import com.vault.util.SecurityUtil;

import java.io.File;
import java.nio.file.Files;

/**
 * Quick test for the video file with found credentials
 */
public class VideoFileTest {
    
    public static void main(String[] args) {
        String filename = "Z-KhnmhiOzFMR0CIRd0_XA.enc";
        String username = "user";
        String password = "secure";
        
        System.out.println("=== Testing Video File Recovery ===");
        System.out.println("File: " + filename);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println();
        
        try {
            String filesDir = System.getProperty("user.home") + "/.securevault/files";
            File file = new File(filesDir, filename);
            
            if (!file.exists()) {
                System.out.println("❌ File not found: " + file.getAbsolutePath());
                return;
            }
            
            System.out.println("File size: " + (file.length() / 1024 / 1024) + " MB");
            
            byte[] encryptedData = Files.readAllBytes(file.toPath());
            System.out.println("Successfully read " + encryptedData.length + " bytes");
            
            javax.crypto.SecretKey key = SecurityUtil.generateKeyFromPassword(password, username);
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, key);
            
            if (decryptedData != null && decryptedData.length > 0) {
                System.out.println();
                System.out.println("🎉 SUCCESS! Video file can be decrypted!");
                System.out.println("Decrypted size: " + (decryptedData.length / 1024 / 1024) + " MB");
                System.out.println("Username: " + username);
                System.out.println("Password: " + password);
                System.out.println();
                System.out.println("You can now use these credentials to access the file in the main vault application!");
            } else {
                System.out.println("❌ Decryption failed - returned null or empty data");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
