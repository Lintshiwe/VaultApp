/*
 * Secure Vault Application
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 * 
 * This software is protected by copyright law and international treaties.
 * Unauthorized reproduction, distribution, or modification is strictly prohibited.
 * 
 * Licensed under MIT License with Additional Restrictions.
 * See LICENSE file for full terms and conditions.
 * 
 * TRADEMARK NOTICE: "Secure Vault Application" is a trademark of Lintshiwe Ntoampi.
 * 
 * Contact: lintshiwe.ntoampi@example.com
 */
package com.vault;

import com.formdev.flatlaf.FlatDarkLaf;
import com.vault.ui.LoginWindow;
import com.vault.util.DatabaseManager;
import com.vault.util.SecureErrorHandler;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for the Secure Vault Application
 * 
 * IMPORTANT: This software is protected by copyright and trademark law.
 * Removal or modification of copyright notices is strictly prohibited.
 */
public class VaultApplication {
    
    private static final String COPYRIGHT = "© 2025 Lintshiwe Ntoampi. All Rights Reserved.";
    private static final String APPLICATION_NAME = "Secure Vault Application™";
    
    public static void main(String[] args) {
        // Validate copyright integrity
        if (!validateCopyright()) {
            showCopyrightViolationError();
            System.exit(1);
        }
        
        // Set system look and feel to FlatLaf Dark theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme: " + e.getMessage());
        }
        
        // Initialize database
        DatabaseManager.getInstance().initializeDatabase();
        
        // Set application properties
        System.setProperty("java.awt.headless", "false");
        
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and show login window
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
                
                // Center window on screen
                centerWindow(loginWindow);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: Application startup error", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                SecureErrorHandler.handleApplicationError(e);
            }
        });
    }
    
    /**
     * Centers a window on the screen
     */
    private static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;
        
        window.setLocation(x, y);
    }
    
    /**
     * Validates that copyright notices are intact
     * This method prevents unauthorized distribution
     */
    private static boolean validateCopyright() {
        try {
            // Check source file integrity
            String sourceFile = VaultApplication.class.getProtectionDomain()
                .getCodeSource().getLocation().toString();
            
            // Basic validation - in a real implementation, you'd use more sophisticated checks
            return COPYRIGHT.contains("2025") && 
                   APPLICATION_NAME.contains("Secure Vault Application") &&
                   !sourceFile.contains("unauthorized");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Shows copyright violation error and exits
     */
    private static void showCopyrightViolationError() {
        String message = 
            "COPYRIGHT VIOLATION DETECTED!\\n\\n" +
            "This software is protected by copyright law.\\n" +
            "Unauthorized modification or distribution is illegal.\\n\\n" +
            "Original Author: Lintshiwe Ntoampi\\n" +
            "Copyright: " + COPYRIGHT + "\\n\\n" +
            "Contact lintshiwe.ntoampi@example.com for licensing information.\\n\\n" +
            "Application will now terminate.";
            
        JOptionPane.showMessageDialog(null, message, 
            "Copyright Violation - Unauthorized Use", 
            JOptionPane.ERROR_MESSAGE);
    }
}
