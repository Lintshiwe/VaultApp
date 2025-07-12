/*
 * Secure Vault Application
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 */
package com.vault.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Professional splash screen for application startup
 */
public class SplashScreen extends JWindow {
    private static final int SPLASH_WIDTH = 500;
    private static final int SPLASH_HEIGHT = 350;
    private static final int PROGRESS_MAX = 100;
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer progressTimer;
    private int progress = 0;
    
    public SplashScreen() {
        initializeComponents();
        setupLayout();
        centerOnScreen();
        startProgress();
    }
    
    private void initializeComponents() {
        setSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        
        // Create main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(30, 30, 30),
                    0, getHeight(), new Color(60, 60, 60)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add border
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Application logo/icon area
        JPanel logoPanel = createLogoPanel();
        
        // Progress area
        JPanel progressPanel = createProgressPanel();
        
        mainPanel.add(logoPanel, BorderLayout.CENTER);
        mainPanel.add(progressPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Application icon
        JLabel iconLabel = new JLabel("🔒");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 64));
        iconLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);
        logoPanel.add(iconLabel, gbc);
        
        // Application name
        JLabel nameLabel = new JLabel("Secure Vault Application™");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        logoPanel.add(nameLabel, gbc);
        
        // Version
        JLabel versionLabel = new JLabel("Version 1.0.1 Professional");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        logoPanel.add(versionLabel, gbc);
        
        // Copyright
        JLabel copyrightLabel = new JLabel("© 2025 Lintshiwe Ntoampi. All Rights Reserved.");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        copyrightLabel.setForeground(new Color(150, 150, 150));
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 0, 0);
        logoPanel.add(copyrightLabel, gbc);
        
        return logoPanel;
    }
    
    private JPanel createProgressPanel() {
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Status label
        statusLabel = new JLabel("Initializing Secure Vault...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar(0, PROGRESS_MAX);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.PLAIN, 10));
        progressBar.setForeground(new Color(70, 130, 180));
        progressBar.setBackground(new Color(40, 40, 40));
        
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        progressPanel.add(progressBar, BorderLayout.SOUTH);
        
        return progressPanel;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
    }
    
    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - SPLASH_WIDTH) / 2;
        int y = (screenSize.height - SPLASH_HEIGHT) / 2;
        setLocation(x, y);
    }
    
    private void startProgress() {
        String[] loadingSteps = {
            "Loading security modules...",
            "Initializing encryption engine...",
            "Setting up database connection...",
            "Configuring user interface...",
            "Validating system integrity...",
            "Starting secure vault..."
        };
        
        progressTimer = new Timer(300, new ActionListener() {
            private int stepIndex = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 2;
                progressBar.setValue(progress);
                
                if (stepIndex < loadingSteps.length && progress >= (stepIndex + 1) * (PROGRESS_MAX / loadingSteps.length)) {
                    statusLabel.setText(loadingSteps[stepIndex]);
                    stepIndex++;
                }
                
                if (progress >= PROGRESS_MAX) {
                    progressTimer.stop();
                    // Small delay before hiding splash
                    Timer hideTimer = new Timer(500, e1 -> {
                        setVisible(false);
                        dispose();
                    });
                    hideTimer.setRepeats(false);
                    hideTimer.start();
                }
            }
        });
        
        progressTimer.start();
    }
    
    /**
     * Shows the splash screen and returns when loading is complete
     */
    public static void showSplash() {
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);
        });
        
        // Wait for splash to complete
        try {
            Thread.sleep(3500); // Total splash duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
