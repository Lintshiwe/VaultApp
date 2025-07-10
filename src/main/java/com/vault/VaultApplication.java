package com.vault;

import com.formdev.flatlaf.FlatDarkLaf;
import com.vault.ui.LoginWindow;
import com.vault.util.DatabaseManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main application class for the Secure Vault Application
 */
public class VaultApplication {
    
    public static void main(String[] args) {
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
                    "Failed to start application: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
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
}
