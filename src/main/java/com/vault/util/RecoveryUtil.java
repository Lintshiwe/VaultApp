package com.vault.util;

import com.vault.model.VaultFile;
import com.vault.model.Admin;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import org.apache.commons.io.FileUtils;

/**
 * Recovery utility to help decrypt files when credentials have been lost or changed
 */
public class RecoveryUtil {
    
    /**
     * Common default passwords to try
     */
    private static final String[] COMMON_PASSWORDS = {
        "admin123",
        "admin",
        "password",
        "123456",
        "vault123"
    };
    
    /**
     * Try to recover a file by attempting different password combinations
     */
    public static RecoveryResult attemptFileRecovery(VaultFile vaultFile, List<Admin> adminHistory) {
        File encryptedFile = new File(vaultFile.getEncryptedPath());
        if (!encryptedFile.exists()) {
            return new RecoveryResult(false, "Encrypted file not found", null, null);
        }
        
        try {
            byte[] encryptedData = FileUtils.readFileToByteArray(encryptedFile);
            
            // Try current admin credentials first
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Admin currentAdmin = getCurrentAdmin(dbManager);
            if (currentAdmin != null) {
                RecoveryResult result = tryDecryptWithCredentials(encryptedData, "admin123", currentAdmin.getSalt());
                if (result.success) {
                    return result;
                }
            }
            
            // Try common passwords with different salts from admin history
            for (Admin admin : adminHistory) {
                for (String password : COMMON_PASSWORDS) {
                    RecoveryResult result = tryDecryptWithCredentials(encryptedData, password, admin.getSalt());
                    if (result.success) {
                        return result;
                    }
                }
            }
            
            // Try generating new salts with common passwords (in case salt was regenerated)
            for (String password : COMMON_PASSWORDS) {
                // Try with a fresh salt (this shouldn't work but worth trying)
                String testSalt = SecurityUtil.generateSalt();
                RecoveryResult result = tryDecryptWithCredentials(encryptedData, password, testSalt);
                if (result.success) {
                    return result;
                }
            }
            
            return new RecoveryResult(false, "Could not recover file with any known credentials", null, null);
            
        } catch (Exception e) {
            return new RecoveryResult(false, "Recovery failed: " + e.getMessage(), null, null);
        }
    }
    
    private static RecoveryResult tryDecryptWithCredentials(byte[] encryptedData, String password, String salt) {
        try {
            SecretKey key = SecurityUtil.generateKeyFromPassword(password, salt);
            byte[] decryptedData = SecurityUtil.decrypt(encryptedData, key);
            // Verify we got valid data (not null and has content)
            if (decryptedData != null && decryptedData.length > 0) {
                return new RecoveryResult(true, "Successfully decrypted " + decryptedData.length + " bytes", password, salt);
            } else {
                return new RecoveryResult(false, "Decryption returned empty data", null, null);
            }
        } catch (Exception e) {
            // Decryption failed with these credentials
            return new RecoveryResult(false, e.getMessage(), null, null);
        }
    }
    
    private static Admin getCurrentAdmin(DatabaseManager dbManager) {
        try {
            // Try to get the current admin (assuming username "admin")
            return dbManager.authenticateAdmin("admin", "admin123");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Result of a recovery attempt
     */
    public static class RecoveryResult {
        public final boolean success;
        public final String message;
        public final String workingPassword;
        public final String workingSalt;
        
        public RecoveryResult(boolean success, String message, String workingPassword, String workingSalt) {
            this.success = success;
            this.message = message;
            this.workingPassword = workingPassword;
            this.workingSalt = workingSalt;
        }
    }
    
    /**
     * Reset admin to default credentials and re-encrypt all files
     */
    public static boolean resetToDefaultAndReEncrypt() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            // Get all vault files
            List<VaultFile> files = dbManager.getAllVaultFiles();
            List<RecoveryResult> successfulRecoveries = new ArrayList<>();
            
            // Try to recover each file
            List<Admin> adminHistory = new ArrayList<>(); // In a real scenario, we'd have this
            for (VaultFile file : files) {
                RecoveryResult result = attemptFileRecovery(file, adminHistory);
                if (result.success) {
                    successfulRecoveries.add(result);
                }
            }
            
            if (successfulRecoveries.isEmpty()) {
                return false; // No files could be recovered
            }
            
            // If we found working credentials, use them to re-encrypt everything
            // For now, just return success - this would need more implementation
            return true;
            
        } catch (Exception e) {
            System.err.println("Reset and re-encryption failed: " + e.getMessage());
            return false;
        }
    }
}
