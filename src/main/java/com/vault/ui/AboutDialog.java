package com.vault.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * About dialog showing copyright and licensing information
 * 
 * Copyright (c) 2025 Lintshiwe Ntoampi. All rights reserved.
 * This software is licensed under MIT License with Additional Restrictions.
 * See LICENSE file for full terms and conditions.
 */
public class AboutDialog extends JDialog {
    
    private static final String COPYRIGHT_NOTICE = "© 2025 Lintshiwe Ntoampi. All Rights Reserved.";
    private static final String VERSION = "Version 1.0.1";
    private static final String APPLICATION_NAME = "Secure Vault Application™";
    
    public AboutDialog(JFrame parent) {
        super(parent, "About " + APPLICATION_NAME, true);
        
        initializeComponents();
        setupLayout();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Main content will be set up in setupLayout
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.WHITE);
        
        // Header panel with logo and app name
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 45, 45));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel appNameLabel = new JLabel(APPLICATION_NAME);
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        appNameLabel.setForeground(Color.WHITE);
        appNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel versionLabel = new JLabel(VERSION);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setForeground(Color.LIGHT_GRAY);
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(appNameLabel, BorderLayout.CENTER);
        headerPanel.add(versionLabel, BorderLayout.SOUTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Copyright notice
        JLabel copyrightLabel = new JLabel(COPYRIGHT_NOTICE);
        copyrightLabel.setFont(new Font("Arial", Font.BOLD, 14));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        copyrightLabel.setForeground(new Color(200, 0, 0));
        
        // Description
        JLabel descLabel = new JLabel("A secure desktop application for encrypted file storage");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setForeground(Color.DARK_GRAY);
        
        // License info
        JTextArea licenseText = new JTextArea();
        licenseText.setEditable(false);
        licenseText.setBackground(Color.WHITE);
        licenseText.setFont(new Font("Arial", Font.PLAIN, 11));
        licenseText.setLineWrap(true);
        licenseText.setWrapStyleWord(true);
        licenseText.setText(
            "IMPORTANT LEGAL NOTICE:\\n\\n" +
            "This software is protected by copyright law and international treaties. " +
            "Unauthorized reproduction, distribution, or modification is strictly prohibited " +
            "and may result in severe civil and criminal penalties.\\n\\n" +
            
            "LICENSING TERMS:\\n" +
            "• Personal use is permitted under the MIT License with Additional Restrictions\\n" +
            "• Commercial use requires explicit written permission\\n" +
            "• Attribution must be maintained in all distributions\\n" +
            "• Copyright notice may not be removed or altered\\n\\n" +
            
            "TRADEMARK NOTICE:\\n" +
            "\\\"Secure Vault Application\\\" is a trademark of Lintshiwe Ntoampi. " +
            "Use of this trademark requires explicit permission.\\n\\n" +
            
            "ENFORCEMENT:\\n" +
            "Violations of these terms will result in automatic license termination " +
            "and may result in legal action. All rights reserved.\\n\\n" +
            
            "For licensing inquiries, contact: lintshiwe.ntoampi@example.com"
        );
        
        JScrollPane licenseScrollPane = new JScrollPane(licenseText);
        licenseScrollPane.setPreferredSize(new Dimension(450, 200));
        licenseScrollPane.setBorder(BorderFactory.createTitledBorder("License Information"));
        
        // Technical info
        JLabel techLabel = new JLabel("<html><center>Built with Java " + 
            System.getProperty("java.version") + "<br>Platform: " + 
            System.getProperty("os.name") + "</center></html>");
        techLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        techLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        techLabel.setForeground(Color.GRAY);
        
        // Add components with spacing
        contentPanel.add(copyrightLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(licenseScrollPane);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(techLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.addActionListener(e -> dispose());
        
        JButton licenseButton = new JButton("View Full License");
        licenseButton.setPreferredSize(new Dimension(140, 30));
        licenseButton.addActionListener(this::showFullLicense);
        
        buttonPanel.add(licenseButton);
        buttonPanel.add(okButton);
        
        // Add panels to dialog
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void showFullLicense(ActionEvent e) {
        try {
            // Try to read LICENSE file
            java.nio.file.Path licensePath = java.nio.file.Paths.get("LICENSE");
            if (java.nio.file.Files.exists(licensePath)) {
                String licenseContent = java.nio.file.Files.readString(licensePath);
                
                JTextArea textArea = new JTextArea(licenseContent);
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                textArea.setCaretPosition(0);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Full License Text", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "LICENSE file not found. Please contact the developer for licensing information.",
                    "License Not Found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error reading license file: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * Validates that copyright notices are intact
     * This method is called during application startup
     */
    public static boolean validateCopyright() {
        // Check that copyright information hasn't been tampered with
        return COPYRIGHT_NOTICE.contains("Lintshiwe Ntoampi") && 
               COPYRIGHT_NOTICE.contains("2025") && 
               APPLICATION_NAME.contains("Secure Vault Application");
    }
}
