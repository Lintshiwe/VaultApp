/*
 * Secure Vault Application
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
package com.vault.ui;

import com.vault.config.SecurityConfig;
import com.vault.util.SecureErrorHandler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Professional settings dialog for application configuration
 */
public class SettingsDialog extends JDialog {
    private static final String PREF_AUTO_LOCK = "auto_lock_enabled";
    private static final String PREF_AUTO_LOCK_TIME = "auto_lock_time";
    private static final String PREF_BACKUP_ENABLED = "backup_enabled";
    private static final String PREF_BACKUP_LOCATION = "backup_location";
    private static final String PREF_MINIMIZE_TO_TRAY = "minimize_to_tray";
    private static final String PREF_START_MINIMIZED = "start_minimized";
    private static final String PREF_DARK_THEME = "dark_theme";
    
    private Preferences prefs;
    
    // Security settings
    private JCheckBox autoLockCheckBox;
    private JSpinner autoLockTimeSpinner;
    private JCheckBox requireStrongPasswordCheckBox;
    private JCheckBox enableLoggingCheckBox;
    
    // Backup settings
    private JCheckBox backupEnabledCheckBox;
    private JTextField backupLocationField;
    private JButton browseBackupButton;
    private JSpinner backupIntervalSpinner;
    
    // UI settings
    private JCheckBox minimizeToTrayCheckBox;
    private JCheckBox startMinimizedCheckBox;
    private JCheckBox darkThemeCheckBox;
    private JSlider fontSizeSlider;
    
    // Advanced settings
    private JCheckBox enableEncryptionCheckBox;
    private JComboBox<String> encryptionMethodCombo;
    private JCheckBox enableCompressionCheckBox;
    
    public SettingsDialog(Frame parent) {
        super(parent, "Settings - Secure Vault Application™", true);
        this.prefs = Preferences.userNodeForPackage(SettingsDialog.class);
        
        initializeComponents();
        setupLayout();
        loadSettings();
        setupEventHandlers();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Security panel components
        autoLockCheckBox = new JCheckBox("Enable auto-lock");
        autoLockTimeSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 60, 1));
        requireStrongPasswordCheckBox = new JCheckBox("Require strong passwords");
        enableLoggingCheckBox = new JCheckBox("Enable security logging");
        
        // Backup panel components
        backupEnabledCheckBox = new JCheckBox("Enable automatic backups");
        backupLocationField = new JTextField(20);
        browseBackupButton = new JButton("Browse...");
        backupIntervalSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
        
        // UI panel components
        minimizeToTrayCheckBox = new JCheckBox("Minimize to system tray");
        startMinimizedCheckBox = new JCheckBox("Start minimized");
        darkThemeCheckBox = new JCheckBox("Use dark theme");
        fontSizeSlider = new JSlider(8, 24, 12);
        fontSizeSlider.setMajorTickSpacing(4);
        fontSizeSlider.setPaintTicks(true);
        fontSizeSlider.setPaintLabels(true);
        
        // Advanced panel components
        enableEncryptionCheckBox = new JCheckBox("Enable file encryption");
        enableEncryptionCheckBox.setSelected(true);
        enableEncryptionCheckBox.setEnabled(false); // Always enabled for security
        
        encryptionMethodCombo = new JComboBox<>(new String[]{
            "AES-256 (Recommended)", "AES-192", "AES-128"
        });
        encryptionMethodCombo.setSelectedIndex(0);
        
        enableCompressionCheckBox = new JCheckBox("Enable file compression");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Security tab
        JPanel securityPanel = createSecurityPanel();
        tabbedPane.addTab("🔒 Security", securityPanel);
        
        // Backup tab
        JPanel backupPanel = createBackupPanel();
        tabbedPane.addTab("💾 Backup", backupPanel);
        
        // UI tab
        JPanel uiPanel = createUIPanel();
        tabbedPane.addTab("🎨 Interface", uiPanel);
        
        // Advanced tab
        JPanel advancedPanel = createAdvancedPanel();
        tabbedPane.addTab("⚙️ Advanced", advancedPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Auto-lock settings
        JPanel autoLockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        autoLockPanel.setBorder(new TitledBorder("Auto-Lock Settings"));
        
        autoLockPanel.add(autoLockCheckBox);
        autoLockPanel.add(new JLabel("Lock after"));
        autoLockPanel.add(autoLockTimeSpinner);
        autoLockPanel.add(new JLabel("minutes of inactivity"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(autoLockPanel, gbc);
        
        // Password settings
        JPanel passwordPanel = new JPanel(new GridLayout(2, 1));
        passwordPanel.setBorder(new TitledBorder("Password Settings"));
        passwordPanel.add(requireStrongPasswordCheckBox);
        
        gbc.gridy = 1;
        panel.add(passwordPanel, gbc);
        
        // Logging settings
        JPanel loggingPanel = new JPanel(new GridLayout(1, 1));
        loggingPanel.setBorder(new TitledBorder("Security Logging"));
        loggingPanel.add(enableLoggingCheckBox);
        
        gbc.gridy = 2;
        panel.add(loggingPanel, gbc);
        
        return panel;
    }
    
    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Backup enabled
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(backupEnabledCheckBox, gbc);
        
        // Backup location
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Backup Location:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(backupLocationField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(browseBackupButton, gbc);
        
        // Backup interval
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Backup Interval (hours):"), gbc);
        
        gbc.gridx = 1;
        panel.add(backupIntervalSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createUIPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // System tray settings
        JPanel trayPanel = new JPanel(new GridLayout(2, 1));
        trayPanel.setBorder(new TitledBorder("System Tray"));
        trayPanel.add(minimizeToTrayCheckBox);
        trayPanel.add(startMinimizedCheckBox);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(trayPanel, gbc);
        
        // Theme settings
        JPanel themePanel = new JPanel(new GridLayout(1, 1));
        themePanel.setBorder(new TitledBorder("Theme"));
        themePanel.add(darkThemeCheckBox);
        
        gbc.gridy = 1;
        panel.add(themePanel, gbc);
        
        // Font settings
        JPanel fontPanel = new JPanel(new BorderLayout());
        fontPanel.setBorder(new TitledBorder("Font Size"));
        fontPanel.add(new JLabel("Small"), BorderLayout.WEST);
        fontPanel.add(fontSizeSlider, BorderLayout.CENTER);
        fontPanel.add(new JLabel("Large"), BorderLayout.EAST);
        
        gbc.gridy = 2;
        panel.add(fontPanel, gbc);
        
        return panel;
    }
    
    private JPanel createAdvancedPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Encryption settings
        JPanel encryptionPanel = new JPanel(new GridBagLayout());
        encryptionPanel.setBorder(new TitledBorder("Encryption"));
        
        GridBagConstraints encGbc = new GridBagConstraints();
        encGbc.insets = new Insets(2, 2, 2, 2);
        encGbc.anchor = GridBagConstraints.WEST;
        
        encGbc.gridx = 0; encGbc.gridy = 0;
        encGbc.gridwidth = 2;
        encryptionPanel.add(enableEncryptionCheckBox, encGbc);
        
        encGbc.gridy = 1;
        encGbc.gridwidth = 1;
        encryptionPanel.add(new JLabel("Method:"), encGbc);
        
        encGbc.gridx = 1;
        encryptionPanel.add(encryptionMethodCombo, encGbc);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(encryptionPanel, gbc);
        
        // Compression settings
        JPanel compressionPanel = new JPanel(new GridLayout(1, 1));
        compressionPanel.setBorder(new TitledBorder("Compression"));
        compressionPanel.add(enableCompressionCheckBox);
        
        gbc.gridy = 1;
        panel.add(compressionPanel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton applyButton = new JButton("Apply");
        JButton resetButton = new JButton("Reset to Defaults");
        
        okButton.addActionListener(e -> {
            saveSettings();
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        applyButton.addActionListener(e -> saveSettings());
        
        resetButton.addActionListener(e -> resetToDefaults());
        
        panel.add(resetButton);
        panel.add(cancelButton);
        panel.add(applyButton);
        panel.add(okButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        browseBackupButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                backupLocationField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        autoLockCheckBox.addActionListener(e -> 
            autoLockTimeSpinner.setEnabled(autoLockCheckBox.isSelected()));
        
        backupEnabledCheckBox.addActionListener(e -> {
            boolean enabled = backupEnabledCheckBox.isSelected();
            backupLocationField.setEnabled(enabled);
            browseBackupButton.setEnabled(enabled);
            backupIntervalSpinner.setEnabled(enabled);
        });
    }
    
    private void loadSettings() {
        // Security settings
        autoLockCheckBox.setSelected(prefs.getBoolean(PREF_AUTO_LOCK, true));
        autoLockTimeSpinner.setValue(prefs.getInt(PREF_AUTO_LOCK_TIME, 15));
        requireStrongPasswordCheckBox.setSelected(true); // Always enforced
        enableLoggingCheckBox.setSelected(prefs.getBoolean("enable_logging", true));
        
        // Backup settings
        backupEnabledCheckBox.setSelected(prefs.getBoolean(PREF_BACKUP_ENABLED, false));
        backupLocationField.setText(prefs.get(PREF_BACKUP_LOCATION, 
            System.getProperty("user.home") + File.separator + "VaultBackups"));
        backupIntervalSpinner.setValue(prefs.getInt("backup_interval", 24));
        
        // UI settings
        minimizeToTrayCheckBox.setSelected(prefs.getBoolean(PREF_MINIMIZE_TO_TRAY, true));
        startMinimizedCheckBox.setSelected(prefs.getBoolean(PREF_START_MINIMIZED, false));
        darkThemeCheckBox.setSelected(prefs.getBoolean(PREF_DARK_THEME, true));
        fontSizeSlider.setValue(prefs.getInt("font_size", 12));
        
        // Advanced settings
        enableCompressionCheckBox.setSelected(prefs.getBoolean("enable_compression", true));
        
        // Update component states
        autoLockTimeSpinner.setEnabled(autoLockCheckBox.isSelected());
        boolean backupEnabled = backupEnabledCheckBox.isSelected();
        backupLocationField.setEnabled(backupEnabled);
        browseBackupButton.setEnabled(backupEnabled);
        backupIntervalSpinner.setEnabled(backupEnabled);
    }
    
    private void saveSettings() {
        try {
            // Security settings
            prefs.putBoolean(PREF_AUTO_LOCK, autoLockCheckBox.isSelected());
            prefs.putInt(PREF_AUTO_LOCK_TIME, (Integer) autoLockTimeSpinner.getValue());
            prefs.putBoolean("enable_logging", enableLoggingCheckBox.isSelected());
            
            // Backup settings
            prefs.putBoolean(PREF_BACKUP_ENABLED, backupEnabledCheckBox.isSelected());
            prefs.put(PREF_BACKUP_LOCATION, backupLocationField.getText());
            prefs.putInt("backup_interval", (Integer) backupIntervalSpinner.getValue());
            
            // UI settings
            prefs.putBoolean(PREF_MINIMIZE_TO_TRAY, minimizeToTrayCheckBox.isSelected());
            prefs.putBoolean(PREF_START_MINIMIZED, startMinimizedCheckBox.isSelected());
            prefs.putBoolean(PREF_DARK_THEME, darkThemeCheckBox.isSelected());
            prefs.putInt("font_size", fontSizeSlider.getValue());
            
            // Advanced settings
            prefs.putBoolean("enable_compression", enableCompressionCheckBox.isSelected());
            
            // Flush preferences
            prefs.flush();
            
            JOptionPane.showMessageDialog(this,
                "Settings saved successfully.\\nSome changes may require restart to take effect.",
                "Settings Saved",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            SecureErrorHandler.handleApplicationError(e);
            JOptionPane.showMessageDialog(this,
                "Failed to save settings. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset all settings to defaults?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            try {
                prefs.clear();
                loadSettings();
                JOptionPane.showMessageDialog(this,
                    "Settings reset to defaults successfully.",
                    "Reset Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                SecureErrorHandler.handleApplicationError(e);
            }
        }
    }
    
    public static Preferences getPreferences() {
        return Preferences.userNodeForPackage(SettingsDialog.class);
    }
}
