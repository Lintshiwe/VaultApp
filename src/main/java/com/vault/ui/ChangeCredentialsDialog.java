package com.vault.ui;

import com.vault.model.Admin;
import com.vault.util.DatabaseManager;
import com.vault.util.SecurityUtil;
import com.vault.service.VaultService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for changing admin credentials
 */
public class ChangeCredentialsDialog extends JDialog {
    
    private final Admin currentAdmin;
    private JTextField usernameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private boolean credentialsChanged = false;
    
    public ChangeCredentialsDialog(JFrame parent, Admin currentAdmin) {
        super(parent, "Change Login Credentials", true);
        this.currentAdmin = currentAdmin;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Username field
        usernameField = new JTextField(currentAdmin.getUsername());
        usernameField.setFont(new Font("Arial", Font.PLAIN, 12));
        usernameField.setPreferredSize(new Dimension(250, 25));
        
        // Password fields
        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        currentPasswordField.setPreferredSize(new Dimension(250, 25));
        
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        newPasswordField.setPreferredSize(new Dimension(250, 25));
        
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        confirmPasswordField.setPreferredSize(new Dimension(250, 25));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Create main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("Change Login Credentials");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Add some space
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("New Username:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);
        
        // Current password
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(currentPasswordField, gbc);
        
        // New password
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(newPasswordField, gbc);
        
        // Confirm password
        gbc.gridx = 0;
        gbc.gridy++;
        mainPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);
        
        // Instructions
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel instructionLabel = new JLabel("<html><i>Leave password fields empty to keep current password</i></html>");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(Color.GRAY);
        mainPanel.add(instructionLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setPreferredSize(new Dimension(120, 30));
        cancelButton.setPreferredSize(new Dimension(120, 30));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Store button references for event handlers
        saveButton.addActionListener(this::saveChanges);
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void setupEventHandlers() {
        // Enter key on password fields
        ActionListener enterAction = e -> saveChanges(e);
        currentPasswordField.addActionListener(enterAction);
        newPasswordField.addActionListener(enterAction);
        confirmPasswordField.addActionListener(enterAction);
        usernameField.addActionListener(enterAction);
    }
    
    private void configureDialog() {
        setResizable(false);
        pack();
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Focus on current password field
        SwingUtilities.invokeLater(() -> currentPasswordField.requestFocus());
    }
    
    private void saveChanges(ActionEvent e) {
        try {
            // Get input values
            String newUsername = usernameField.getText().trim();
            char[] currentPasswordChars = currentPasswordField.getPassword();
            char[] newPasswordChars = newPasswordField.getPassword();
            char[] confirmPasswordChars = confirmPasswordField.getPassword();
            
            String currentPassword = new String(currentPasswordChars);
            String newPassword = new String(newPasswordChars);
            String confirmPassword = new String(confirmPasswordChars);
            
            // Clear password arrays for security
            java.util.Arrays.fill(currentPasswordChars, ' ');
            java.util.Arrays.fill(newPasswordChars, ' ');
            java.util.Arrays.fill(confirmPasswordChars, ' ');
            
            // Validate current password
            if (currentPassword.isEmpty()) {
                showError("Please enter your current password.");
                currentPasswordField.requestFocus();
                return;
            }
            
            // Verify current password
            if (!SecurityUtil.verifyPassword(currentPassword, currentAdmin.getPasswordHash(), currentAdmin.getSalt())) {
                showError("Current password is incorrect.");
                currentPasswordField.selectAll();
                currentPasswordField.requestFocus();
                return;
            }
            
            // Validate new username
            if (newUsername.isEmpty()) {
                showError("Username cannot be empty.");
                usernameField.requestFocus();
                return;
            }
            
            if (newUsername.length() < 3) {
                showError("Username must be at least 3 characters long.");
                usernameField.requestFocus();
                return;
            }
            
            // Determine if password is being changed
            boolean changingPassword = !newPassword.isEmpty();
            final String passwordToUse = changingPassword ? newPassword : currentPassword;
            
            if (changingPassword) {
                // Validate new password
                if (newPassword.length() < 6) {
                    showError("New password must be at least 6 characters long.");
                    newPasswordField.requestFocus();
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    showError("New passwords do not match.");
                    newPasswordField.selectAll();
                    newPasswordField.requestFocus();
                    return;
                }
                
                if (newPassword.equals(currentPassword)) {
                    showError("New password must be different from current password.");
                    newPasswordField.requestFocus();
                    return;
                }
                
                // Show warning about re-encryption
                int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Changing your password will require re-encrypting all vault files.\n" +
                    "This process may take some time depending on the number of files.\n\n" +
                    "Do you want to continue?",
                    "Password Change Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Check if anything actually changed
            if (newUsername.equals(currentAdmin.getUsername()) && !changingPassword) {
                showInfo("No changes to save.");
                return;
            }
            
            // Update credentials in database
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            if (changingPassword) {
                // First, re-encrypt all files with the new password
                VaultService vaultService = VaultService.getInstance();
                
                // Show progress dialog
                javax.swing.JDialog progressDialog = new javax.swing.JDialog(this, "Re-encrypting Files", true);
                javax.swing.JLabel progressLabel = new javax.swing.JLabel("Re-encrypting vault files with new password...");
                progressLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
                progressDialog.add(progressLabel);
                progressDialog.pack();
                progressDialog.setLocationRelativeTo(this);
                progressDialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
                
                // Perform re-encryption in background thread
                javax.swing.SwingWorker<Boolean, Void> worker = new javax.swing.SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        // Generate new salt that will be used in database
                        String newSalt = SecurityUtil.generateSalt();
                        
                        // Re-encrypt all files
                        boolean reencryptSuccess = vaultService.reEncryptAllFiles(
                            currentPassword, 
                            currentAdmin.getSalt(), 
                            newPassword, 
                            newSalt
                        );
                        
                        if (reencryptSuccess) {
                            // Update the current admin's salt for database update
                            currentAdmin.setSalt(newSalt);
                        }
                        
                        return reencryptSuccess;
                    }
                    
                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        try {
                            boolean success = get();
                            if (!success) {
                                showError("Failed to re-encrypt files. Password change cancelled.");
                                return;
                            }
                            
                            // Now update credentials in database with the new salt
                            boolean dbSuccess = dbManager.updateAdminCredentials(
                                currentAdmin.getId(), 
                                newUsername, 
                                passwordToUse,
                                currentAdmin.getSalt()  // Use the salt from re-encryption
                            );
                            
                            if (dbSuccess) {
                                credentialsChanged = true;
                                currentAdmin.setUsername(newUsername);
                                
                                String message = "Credentials updated successfully!";
                                message += "\n\nAll vault files have been re-encrypted with your new password.";
                                message += "\nPlease remember your new login details:\nUsername: " + newUsername;
                                
                                showInfo(message);
                                dispose();
                            } else {
                                showError("Database update failed. Please try again.");
                            }
                            
                        } catch (Exception e) {
                            showError("An error occurred during re-encryption: " + e.getMessage());
                        }
                    }
                };
                
                worker.execute();
                progressDialog.setVisible(true);
                
            } else {
                // Just username change, no re-encryption needed
                boolean success = dbManager.updateAdminCredentials(
                    currentAdmin.getId(), 
                    newUsername, 
                    passwordToUse
                );
                
                if (success) {
                    credentialsChanged = true;
                    currentAdmin.setUsername(newUsername);
                    showInfo("Username updated successfully!");
                    dispose();
                } else {
                    if (!newUsername.equals(currentAdmin.getUsername())) {
                        showError("Username '" + newUsername + "' is already taken. Please choose a different username.");
                        usernameField.selectAll();
                        usernameField.requestFocus();
                    } else {
                        showError("Failed to update credentials. Please try again.");
                    }
                }
            }
            
        } catch (Exception ex) {
            showError("An error occurred while updating credentials: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public boolean isCredentialsChanged() {
        return credentialsChanged;
    }
}
