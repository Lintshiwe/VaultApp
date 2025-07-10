package com.vault.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for encryption and security operations
 */
public class SecurityUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int IV_LENGTH = 16;
    private static final int SALT_LENGTH = 32;
    
    /**
     * Generate a secure random salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password with salt using SHA-256
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verify a password against a hash
     */
    public static boolean verifyPassword(String password, String hash, String salt) {
        String hashedPassword = hashPassword(password, salt);
        return hashedPassword.equals(hash);
    }
    
    /**
     * Generate AES key from password
     */
    public static SecretKey generateKeyFromPassword(String password, String salt) {
        try {
            // Use PBKDF2 to derive key from password
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), 
                Base64.getDecoder().decode(salt), 
                10000, 
                256
            );
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key from password", e);
        }
    }
    
    /**
     * Encrypt data using AES
     */
    public static byte[] encrypt(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Generate random IV
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encryptedData = cipher.doFinal(data);
            
            // Prepend IV to encrypted data
            byte[] result = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, result, IV_LENGTH, encryptedData.length);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Decrypt data using AES
     */
    public static byte[] decrypt(byte[] encryptedData, SecretKey key) {
        try {
            // Extract IV from encrypted data
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Extract actual encrypted data
            byte[] actualEncryptedData = new byte[encryptedData.length - IV_LENGTH];
            System.arraycopy(encryptedData, IV_LENGTH, actualEncryptedData, 0, actualEncryptedData.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            
            return cipher.doFinal(actualEncryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
    
    /**
     * Generate a secure random filename
     */
    public static String generateSecureFileName() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
