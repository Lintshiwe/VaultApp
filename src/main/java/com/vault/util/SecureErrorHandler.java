package com.vault.util;

import com.vault.config.SecurityConfig;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.sql.SQLException;
import javax.crypto.BadPaddingException;

/**
 * Secure error handler that prevents information leakage
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
public class SecureErrorHandler {
    
    private static final Logger LOGGER = Logger.getLogger(SecureErrorHandler.class.getName());
    
    /**
     * Handle authentication errors securely
     */
    public static String handleAuthenticationError(Exception e) {
        SecurityConfig.secureLog(Level.WARNING, "Authentication failed: {0}", e.getClass().getSimpleName());
        return "Authentication failed. Please check your credentials.";
    }
    
    /**
     * Handle file operation errors securely
     */
    public static String handleFileError(Exception e) {
        if (e instanceof IOException) {
            SecurityConfig.secureLog(Level.WARNING, "File operation failed: {0}", e.getMessage());
            return "File operation failed. Please check file permissions and disk space.";
        } else if (e instanceof SecurityException) {
            SecurityConfig.secureLog(Level.WARNING, "File access denied: {0}", e.getMessage());
            return "Access denied. Insufficient permissions.";
        } else {
            SecurityConfig.secureLog(Level.SEVERE, "Unexpected file error: {0}", e.getClass().getSimpleName());
            return "An unexpected error occurred during file operation.";
        }
    }
    
    /**
     * Handle database errors securely
     */
    public static String handleDatabaseError(Exception e) {
        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            SecurityConfig.secureLog(Level.SEVERE, "Database error - Code: {0}, State: {1}", 
                sqlEx.getErrorCode(), sqlEx.getSQLState());
            
            // Return generic message to prevent SQL injection reconnaissance
            return "Database operation failed. Please try again later.";
        } else {
            SecurityConfig.secureLog(Level.SEVERE, "Database connection error: {0}", e.getClass().getSimpleName());
            return "Database connection failed. Please check your configuration.";
        }
    }
    
    /**
     * Handle encryption/decryption errors securely
     */
    public static String handleCryptoError(Exception e) {
        if (e instanceof BadPaddingException) {
            SecurityConfig.secureLog(Level.INFO, "Decryption failed - invalid key or corrupted data");
            return "Decryption failed. Invalid credentials or corrupted file.";
        } else {
            SecurityConfig.secureLog(Level.SEVERE, "Cryptographic error: {0}", e.getClass().getSimpleName());
            return "Encryption/decryption operation failed.";
        }
    }
    
    /**
     * Handle validation errors securely
     */
    public static String handleValidationError(String field, String issue) {
        SecurityConfig.secureLog(Level.INFO, "Validation failed for field: {0}", field);
        return String.format("Invalid %s: %s", field, issue);
    }
    
    /**
     * Handle unexpected errors securely
     */
    public static String handleUnexpectedError(Exception e) {
        SecurityConfig.secureLog(Level.SEVERE, "Unexpected error: {0}", e.getClass().getSimpleName());
        return "An unexpected error occurred. Please try again or contact support.";
    }
    
    /**
     * Log security events
     */
    public static void logSecurityEvent(String event, Object... details) {
        SecurityConfig.secureLog(Level.WARNING, "Security Event: {0}", event);
        for (Object detail : details) {
            SecurityConfig.secureLog(Level.INFO, "Detail: {0}", detail);
        }
    }
    
    /**
     * Check if an error might be security-related
     */
    public static boolean isSecurityRelated(Exception e) {
        return e instanceof SecurityException ||
               e instanceof BadPaddingException ||
               e.getMessage() != null && (
                   e.getMessage().toLowerCase().contains("access denied") ||
                   e.getMessage().toLowerCase().contains("unauthorized") ||
                   e.getMessage().toLowerCase().contains("authentication")
               );
    }
    
    /**
     * Handle general application errors
     */
    public static void handleApplicationError(Exception e) {
        SecurityConfig.secureLog(Level.SEVERE, "Application error occurred: {0}", sanitizeErrorMessage(e.getMessage()));
    }
    
    /**
     * Sanitize error messages to prevent information disclosure
     */
    private static String sanitizeErrorMessage(String message) {
        if (message == null) return "Unknown error";
        
        // Remove potential sensitive information
        return message.replaceAll("(?i)(password|key|token|secret)", "[REDACTED]")
                     .replaceAll("(?i)(file:///|[A-Za-z]:\\\\)", "[PATH]")
                     .replaceAll("(?i)(jdbc:[^\\s]+)", "[DATABASE_URL]");
    }
}
