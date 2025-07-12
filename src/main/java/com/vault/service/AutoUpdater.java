/*
 * Secure Vault Application
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
package com.vault.service;

import com.vault.util.SecureErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

/**
 * Professional auto-updater for keeping the application current
 */
public class AutoUpdater {
    private static final String UPDATE_CHECK_URL = "https://api.github.com/repos/Lintshiwe/VaultApp/releases/latest";
    private static final String CURRENT_VERSION = "1.0.1";
    private static final String PREF_LAST_UPDATE_CHECK = "last_update_check";
    private static final String PREF_AUTO_UPDATE_ENABLED = "auto_update_enabled";
    private static final long CHECK_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    
    private static AutoUpdater instance;
    private Preferences prefs;
    
    private AutoUpdater() {
        this.prefs = Preferences.userNodeForPackage(AutoUpdater.class);
    }
    
    public static AutoUpdater getInstance() {
        if (instance == null) {
            instance = new AutoUpdater();
        }
        return instance;
    }
    
    /**
     * Check for updates asynchronously
     */
    public void checkForUpdatesAsync() {
        if (!isAutoUpdateEnabled()) {
            return;
        }
        
        long lastCheck = prefs.getLong(PREF_LAST_UPDATE_CHECK, 0);
        long now = System.currentTimeMillis();
        
        if (now - lastCheck < CHECK_INTERVAL) {
            return; // Too soon since last check
        }
        
        CompletableFuture.supplyAsync(this::checkForUpdates)
            .thenAccept(this::handleUpdateCheck)
            .exceptionally(throwable -> {
                SecureErrorHandler.handleApplicationError(new Exception(throwable));
                return null;
            });
    }
    
    /**
     * Manual update check with user feedback
     */
    public void checkForUpdatesManual() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        JDialog progressDialog = new JDialog((Frame) null, "Checking for Updates", true);
        progressDialog.add(new JLabel("Checking for updates..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(null);
        
        SwingWorker<UpdateInfo, Void> worker = new SwingWorker<UpdateInfo, Void>() {
            @Override
            protected UpdateInfo doInBackground() throws Exception {
                return checkForUpdates();
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    UpdateInfo info = get();
                    handleUpdateCheck(info);
                    
                    if (info == null || !info.hasUpdate()) {
                        JOptionPane.showMessageDialog(null,
                            "You are running the latest version (" + CURRENT_VERSION + ")",
                            "No Updates Available",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    SecureErrorHandler.handleApplicationError(e);
                    JOptionPane.showMessageDialog(null,
                        "Failed to check for updates. Please check your internet connection.",
                        "Update Check Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    private UpdateInfo checkForUpdates() {
        try {
            URL url = new URL(UPDATE_CHECK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "SecureVaultApp/" + CURRENT_VERSION);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }
            
            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            
            // Parse JSON response (simple parsing for version info)
            String jsonResponse = response.toString();
            String latestVersion = extractVersion(jsonResponse);
            String downloadUrl = extractDownloadUrl(jsonResponse);
            String releaseNotes = extractReleaseNotes(jsonResponse);
            
            prefs.putLong(PREF_LAST_UPDATE_CHECK, System.currentTimeMillis());
            prefs.flush();
            
            if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                return new UpdateInfo(latestVersion, downloadUrl, releaseNotes);
            }
            
            return new UpdateInfo(CURRENT_VERSION, null, null);
            
        } catch (Exception e) {
            SecureErrorHandler.handleApplicationError(e);
            return null;
        }
    }
    
    private void handleUpdateCheck(UpdateInfo updateInfo) {
        if (updateInfo == null || !updateInfo.hasUpdate()) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> showUpdateDialog(updateInfo));
    }
    
    private void showUpdateDialog(UpdateInfo updateInfo) {
        JDialog dialog = new JDialog((Frame) null, "Update Available", true);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout());
        headerPanel.add(new JLabel("🔄"));
        headerPanel.add(new JLabel("<html><b>New Version Available!</b></html>"));
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Current Version:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(new JLabel(CURRENT_VERSION), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(new JLabel("Latest Version:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(new JLabel("<html><b>" + updateInfo.getVersion() + "</b></html>"), gbc);
        
        if (updateInfo.getReleaseNotes() != null && !updateInfo.getReleaseNotes().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 2;
            gbc.gridwidth = 2;
            contentPanel.add(new JLabel("Release Notes:"), gbc);
            
            JTextArea notesArea = new JTextArea(updateInfo.getReleaseNotes(), 5, 40);
            notesArea.setEditable(false);
            notesArea.setBackground(contentPanel.getBackground());
            JScrollPane scrollPane = new JScrollPane(notesArea);
            
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            contentPanel.add(scrollPane, gbc);
        }
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton downloadButton = new JButton("Download Update");
        JButton remindLaterButton = new JButton("Remind Later");
        JButton skipButton = new JButton("Skip This Version");
        
        downloadButton.addActionListener(e -> {
            dialog.dispose();
            openDownloadPage(updateInfo.getDownloadUrl());
        });
        
        remindLaterButton.addActionListener(e -> dialog.dispose());
        
        skipButton.addActionListener(e -> {
            prefs.put("skipped_version", updateInfo.getVersion());
            dialog.dispose();
        });
        
        buttonPanel.add(skipButton);
        buttonPanel.add(remindLaterButton);
        buttonPanel.add(downloadButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    private void openDownloadPage(String downloadUrl) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URL(downloadUrl != null ? downloadUrl : 
                    "https://github.com/Lintshiwe/VaultApp/releases/latest").toURI());
            } else {
                JOptionPane.showMessageDialog(null,
                    "Please visit: https://github.com/Lintshiwe/VaultApp/releases/latest",
                    "Download Update",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            SecureErrorHandler.handleApplicationError(e);
        }
    }
    
    private String extractVersion(String json) {
        // Simple JSON parsing for version
        String tagNameMarker = "\"tag_name\":\"";
        int start = json.indexOf(tagNameMarker);
        if (start == -1) return null;
        
        start += tagNameMarker.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        
        String version = json.substring(start, end);
        return version.startsWith("v") ? version.substring(1) : version;
    }
    
    private String extractDownloadUrl(String json) {
        // Extract download URL from assets
        String browserDownloadMarker = "\"browser_download_url\":\"";
        int start = json.indexOf(browserDownloadMarker);
        if (start == -1) return null;
        
        start += browserDownloadMarker.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        
        return json.substring(start, end);
    }
    
    private String extractReleaseNotes(String json) {
        // Extract release notes
        String bodyMarker = "\"body\":\"";
        int start = json.indexOf(bodyMarker);
        if (start == -1) return null;
        
        start += bodyMarker.length();
        int end = json.indexOf("\",", start);
        if (end == -1) {
            end = json.indexOf("\"}", start);
        }
        if (end == -1) return null;
        
        String notes = json.substring(start, end);
        return notes.replace("\\n", "\n").replace("\\r", "\r");
    }
    
    private boolean isNewerVersion(String latest, String current) {
        if (latest == null || current == null) return false;
        
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");
        
        int maxLength = Math.max(latestParts.length, currentParts.length);
        
        for (int i = 0; i < maxLength; i++) {
            int latestPart = i < latestParts.length ? 
                Integer.parseInt(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? 
                Integer.parseInt(currentParts[i]) : 0;
            
            if (latestPart > currentPart) return true;
            if (latestPart < currentPart) return false;
        }
        
        return false;
    }
    
    public boolean isAutoUpdateEnabled() {
        return prefs.getBoolean(PREF_AUTO_UPDATE_ENABLED, true);
    }
    
    public void setAutoUpdateEnabled(boolean enabled) {
        prefs.putBoolean(PREF_AUTO_UPDATE_ENABLED, enabled);
        try {
            prefs.flush();
        } catch (Exception e) {
            SecureErrorHandler.handleApplicationError(e);
        }
    }
    
    /**
     * Inner class to hold update information
     */
    private static class UpdateInfo {
        private final String version;
        private final String downloadUrl;
        private final String releaseNotes;
        
        public UpdateInfo(String version, String downloadUrl, String releaseNotes) {
            this.version = version;
            this.downloadUrl = downloadUrl;
            this.releaseNotes = releaseNotes;
        }
        
        public String getVersion() { return version; }
        public String getDownloadUrl() { return downloadUrl; }
        public String getReleaseNotes() { return releaseNotes; }
        
        public boolean hasUpdate() {
            return downloadUrl != null;
        }
    }
}
