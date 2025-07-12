package com.vault.ui;

import com.vault.service.VaultService;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for displaying detailed space usage and management information
 */
public class SpaceManagementDialog extends JDialog {
    
    private VaultService vaultService;
    private JLabel currentUsageLabel;
    private JLabel freeSpaceLabel;
    private JLabel recommendedSpaceLabel;
    private JLabel minimumSpaceLabel;
    private JProgressBar spaceProgressBar;
    private JProgressBar vaultProgressBar;
    
    public SpaceManagementDialog(JFrame parent, VaultService vaultService) {
        super(parent, "Vault Space Management", true);
        this.vaultService = vaultService;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureDialog();
        updateSpaceInfo();
    }
    
    private void initializeComponents() {
        currentUsageLabel = new JLabel();
        freeSpaceLabel = new JLabel();
        recommendedSpaceLabel = new JLabel();
        minimumSpaceLabel = new JLabel();
        
        spaceProgressBar = new JProgressBar(0, 100);
        spaceProgressBar.setStringPainted(true);
        spaceProgressBar.setString("Loading...");
        
        vaultProgressBar = new JProgressBar(0, 100);
        vaultProgressBar.setStringPainted(true);
        vaultProgressBar.setString("Loading...");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Vault Space Usage Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, gbc);
        
        // Disk space section
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel diskLabel = new JLabel("Disk Space Usage:");
        diskLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(diskLabel, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(spaceProgressBar, gbc);
        
        // Vault space section
        gbc.gridx = 0; gbc.gridy++;
        JLabel vaultLabel = new JLabel("Vault Efficiency:");
        vaultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(vaultLabel, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(vaultProgressBar, gbc);
        
        // Details section
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel detailsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Space Details"));
        
        detailsPanel.add(currentUsageLabel);
        detailsPanel.add(freeSpaceLabel);
        detailsPanel.add(minimumSpaceLabel);
        detailsPanel.add(recommendedSpaceLabel);
        
        mainPanel.add(detailsPanel, gbc);
        
        // Info panel
        gbc.gridy++;
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        
        JTextArea infoText = new JTextArea(4, 30);
        infoText.setText(
                "Space Management Information:\n" +
                "• Vault automatically encrypts files with ~20% overhead\n" +
                "• Minimum space: Current usage + 100MB safety buffer\n" +
                "• Recommended space: Current usage + 50% expansion room\n" +
                "• Monitor this dialog to prevent storage issues");
        infoText.setEditable(false);
        infoText.setOpaque(false);
        infoText.setFont(new Font("Arial", Font.PLAIN, 12));
        
        infoPanel.add(infoText, BorderLayout.CENTER);
        mainPanel.add(infoPanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Add panels
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Setup button handlers
        refreshButton.addActionListener(e -> updateSpaceInfo());
        closeButton.addActionListener(e -> dispose());
    }
    
    private void setupEventHandlers() {
        // ESC key to close
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void configureDialog() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        pack();
        setLocationRelativeTo(getParent());
        setMinimumSize(new Dimension(450, 400));
    }
    
    private void updateSpaceInfo() {
        SwingWorker<VaultService.DiskSpaceStatus, Void> worker = new SwingWorker<VaultService.DiskSpaceStatus, Void>() {
            @Override
            protected VaultService.DiskSpaceStatus doInBackground() throws Exception {
                return vaultService.checkDiskSpace();
            }
            
            @Override
            protected void done() {
                try {
                    VaultService.DiskSpaceStatus diskStatus = get();
                    updateDisplay(diskStatus);
                } catch (Exception e) {
                    showError("Failed to update space information: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateDisplay(VaultService.DiskSpaceStatus diskStatus) {
        VaultService.SpaceInfo spaceInfo = diskStatus.getSpaceInfo();
        
        // Update labels
        currentUsageLabel.setText("Current vault usage: " + spaceInfo.getFormattedUsedSpace() + 
                                 " (" + spaceInfo.getFileCount() + " files)");
        freeSpaceLabel.setText("Available disk space: " + diskStatus.getFormattedFreeSpace());
        minimumSpaceLabel.setText("Minimum required space: " + spaceInfo.getFormattedMinimumSpace());
        recommendedSpaceLabel.setText("Recommended free space: " + spaceInfo.getFormattedRecommendedSpace());
        
        // Update disk space progress bar
        double diskUsage = diskStatus.getUsagePercentage();
        spaceProgressBar.setValue((int) diskUsage);
        spaceProgressBar.setString(String.format("%.1f%% used (%s of %s)", 
            diskUsage, 
            diskStatus.getFormattedUsableSpace(),
            diskStatus.getFormattedTotalSpace()));
        
        // Update vault efficiency progress bar (encryption overhead)
        if (spaceInfo.getOriginalSize() > 0) {
            double efficiency = ((double) spaceInfo.getOriginalSize() / spaceInfo.getTotalUsedSpace()) * 100.0;
            vaultProgressBar.setValue((int) efficiency);
            vaultProgressBar.setString(String.format("%.1f%% efficiency (%s original → %s encrypted)", 
                efficiency,
                spaceInfo.getFormattedOriginalSize(),
                spaceInfo.getFormattedUsedSpace()));
        } else {
            vaultProgressBar.setValue(100);
            vaultProgressBar.setString("No files stored yet");
        }
        
        // Color coding
        Color statusColor;
        if (!diskStatus.hasEnoughSpace()) {
            statusColor = Color.RED;
        } else if (!diskStatus.hasRecommendedSpace()) {
            statusColor = Color.ORANGE;
        } else {
            statusColor = Color.GREEN;
        }
        
        spaceProgressBar.setForeground(statusColor);
        freeSpaceLabel.setForeground(statusColor);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
