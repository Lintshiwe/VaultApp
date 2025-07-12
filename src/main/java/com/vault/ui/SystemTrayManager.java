/*
 * Secure Vault Application
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
package com.vault.ui;

import com.vault.service.VaultService;
import com.vault.util.SecureErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * System tray integration for professional desktop experience
 */
public class SystemTrayManager {
    private static SystemTrayManager instance;
    private TrayIcon trayIcon;
    private MainWindow mainWindow;
    private VaultService vaultService;
    
    private SystemTrayManager() {
        // Private constructor for singleton
    }
    
    public static SystemTrayManager getInstance() {
        if (instance == null) {
            instance = new SystemTrayManager();
        }
        return instance;
    }
    
    public void initialize(MainWindow mainWindow, VaultService vaultService) {
        this.mainWindow = mainWindow;
        this.vaultService = vaultService;
        
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported on this platform");
            return;
        }
        
        SystemTray tray = SystemTray.getSystemTray();
        Image image = createTrayIcon();
        
        // Create popup menu
        PopupMenu popup = createPopupMenu();
        
        // Create tray icon
        trayIcon = new TrayIcon(image, "Secure Vault Application™", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Secure Vault Application™ - Click to open");
        
        // Add double-click listener
        trayIcon.addActionListener(e -> showMainWindow());
        
        try {
            tray.add(trayIcon);
            showTrayNotification("Secure Vault Started", 
                "Application is running in system tray", 
                TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            SecureErrorHandler.handleApplicationError(e);
        }
    }
    
    private Image createTrayIcon() {
        // Create a simple lock icon for the system tray
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw lock icon
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRoundRect(3, 6, 10, 8, 2, 2);
        
        g2d.setColor(new Color(100, 150, 200));
        g2d.drawRoundRect(5, 3, 6, 6, 3, 3);
        g2d.fillOval(7, 9, 2, 2);
        
        g2d.dispose();
        return image;
    }
    
    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        
        // Show/Hide window
        MenuItem showHide = new MenuItem("Show Vault");
        showHide.addActionListener(e -> toggleMainWindow());
        popup.add(showHide);
        
        popup.addSeparator();
        
        // Quick actions
        MenuItem quickAdd = new MenuItem("Quick Add File...");
        quickAdd.addActionListener(e -> showQuickAddDialog());
        popup.add(quickAdd);
        
        MenuItem vaultStats = new MenuItem("Vault Statistics");
        vaultStats.addActionListener(e -> showVaultStats());
        popup.add(vaultStats);
        
        popup.addSeparator();
        
        // Security actions
        MenuItem lockVault = new MenuItem("Lock Vault");
        lockVault.addActionListener(e -> lockVault());
        popup.add(lockVault);
        
        MenuItem changePassword = new MenuItem("Change Password...");
        changePassword.addActionListener(e -> showChangePassword());
        popup.add(changePassword);
        
        popup.addSeparator();
        
        // Settings and help
        MenuItem settings = new MenuItem("Settings...");
        settings.addActionListener(e -> showSettings());
        popup.add(settings);
        
        MenuItem about = new MenuItem("About");
        about.addActionListener(e -> showAbout());
        popup.add(about);
        
        popup.addSeparator();
        
        // Exit
        MenuItem exit = new MenuItem("Exit Secure Vault");
        exit.addActionListener(e -> exitApplication());
        popup.add(exit);
        
        return popup;
    }
    
    private void showMainWindow() {
        if (mainWindow != null) {
            mainWindow.setVisible(true);
            mainWindow.setExtendedState(JFrame.NORMAL);
            mainWindow.toFront();
            mainWindow.requestFocus();
        }
    }
    
    private void hideMainWindow() {
        if (mainWindow != null) {
            mainWindow.setVisible(false);
            showTrayNotification("Secure Vault Minimized", 
                "Application continues running in system tray", 
                TrayIcon.MessageType.INFO);
        }
    }
    
    private void toggleMainWindow() {
        if (mainWindow != null) {
            if (mainWindow.isVisible()) {
                hideMainWindow();
            } else {
                showMainWindow();
            }
        }
    }
    
    private void showQuickAddDialog() {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null && vaultService != null) {
                showMainWindow();
                // Trigger add file dialog
                mainWindow.showAddFileDialog();
            }
        });
    }
    
    private void showVaultStats() {
        SwingUtilities.invokeLater(() -> {
            if (vaultService != null) {
                try {
                    var stats = vaultService.getVaultStatistics();
                    String message = String.format(
                        "Vault Statistics:\\n\\n" +
                        "📁 Total Files: %d\\n" +
                        "💾 Total Size: %s\\n" +
                        "🔒 Encryption: AES-256\\n" +
                        "📊 Available Space: %s",
                        stats.getTotalFiles(),
                        formatFileSize(stats.getTotalSize()),
                        formatFileSize(stats.getAvailableSpace())
                    );
                    
                    showTrayNotification("Vault Statistics", message, TrayIcon.MessageType.INFO);
                } catch (Exception e) {
                    showTrayNotification("Error", "Unable to retrieve vault statistics", 
                        TrayIcon.MessageType.ERROR);
                }
            }
        });
    }
    
    private void lockVault() {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.dispose();
                showTrayNotification("Vault Locked", 
                    "Vault has been locked for security", 
                    TrayIcon.MessageType.WARNING);
                
                // Show login window
                new LoginWindow().setVisible(true);
            }
        });
    }
    
    private void showChangePassword() {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                showMainWindow();
                mainWindow.showChangePasswordDialog();
            }
        });
    }
    
    private void showSettings() {
        SwingUtilities.invokeLater(() -> {
            SettingsDialog settings = new SettingsDialog(mainWindow);
            settings.setVisible(true);
        });
    }
    
    private void showAbout() {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                showMainWindow();
                new AboutDialog(mainWindow).setVisible(true);
            }
        });
    }
    
    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to exit Secure Vault?\\n\\nAll unsaved work will be lost.",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Clean shutdown
            if (trayIcon != null) {
                SystemTray.getSystemTray().remove(trayIcon);
            }
            System.exit(0);
        }
    }
    
    public void showTrayNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }
    
    public void minimizeToTray() {
        hideMainWindow();
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    public void cleanup() {
        if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}
