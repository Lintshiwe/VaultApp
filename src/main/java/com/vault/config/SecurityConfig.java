package com.vault.config;

import java.security.SecureRandom;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Security configuration and utilities for the Vault Application
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
public class SecurityConfig {
    
    private static final Logger LOGGER = Logger.getLogger(SecurityConfig.class.getName());
    
    // Security constants
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int SALT_LENGTH = 32;
    public static final int KEY_LENGTH = 256;
    public static final int IV_LENGTH = 16;
    
    // File size limits (in bytes)
    public static final long MAX_FILE_SIZE = 500L * 1024 * 1024; // 500MB
    public static final long MAX_TOTAL_VAULT_SIZE = 5L * 1024 * 1024 * 1024; // 5GB
    
    // Rate limiting
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_LOCKOUT_TIME = 15 * 60 * 1000; // 15 minutes
    
    // Secure random instance
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Generate cryptographically secure random bytes
     */
    public static byte[] generateSecureRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Validate password strength
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isWhitespace(c)) hasSpecial = true;
        }
        
        // Require at least 3 of the 4 character types
        int types = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        return types >= 3;
    }
    
    /**
     * Sanitize input to prevent injection attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove or escape potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "")
                   .trim()
                   .substring(0, Math.min(input.length(), 255));
    }
    
    /**
     * Validate file extension against allowed types
     */
    public static boolean isFileTypeAllowed(String filename) {
        if (filename == null || filename.isEmpty()) return false;
        
        String extension = getFileExtension(filename).toLowerCase();
        
        // Allowed file extensions
        String[] allowedExtensions = {
            "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "jpg", "jpeg", "png", "gif", "bmp", "tiff",
            "mp3", "wav", "flac", "aac",
            "mp4", "avi", "mkv", "mov", "wmv",
            "zip", "rar", "7z", "tar", "gz"
        };
        
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get file extension from filename
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
    
    /**
     * Secure logging that doesn't expose sensitive data
     */
    public static void secureLog(Level level, String message, Object... params) {
        // Sanitize parameters to ensure no sensitive data is logged
        Object[] sanitizedParams = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            sanitizedParams[i] = sanitizeForLogging(params[i]);
        }
        
        LOGGER.log(level, message, sanitizedParams);
    }
    
    /**
     * Sanitize objects for logging to prevent sensitive data exposure
     */
    private static Object sanitizeForLogging(Object obj) {
        if (obj == null) return null;
        
        String str = obj.toString();
        
        // Mask potential passwords or sensitive data
        if (str.toLowerCase().contains("password") || 
            str.toLowerCase().contains("secret") ||
            str.toLowerCase().contains("key")) {
            return "[REDACTED]";
        }
        
        // Truncate long strings
        if (str.length() > 100) {
            return str.substring(0, 97) + "...";
        }
        
        return obj;
    }
}
