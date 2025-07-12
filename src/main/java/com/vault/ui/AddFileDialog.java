package com.vault.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Dialog for adding file details when storing in vault
 */
public class AddFileDialog extends JDialog {
    
    private File file;
    private JTextField descriptionField;
    private JTextField tagsField;
    private JLabel fileInfoLabel;
    private boolean confirmed = false;
    
    public AddFileDialog(JFrame parent, File file) {
        super(parent, "Add File to Vault", true);
        this.file = file;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureDialog();
    }
    
    private void initializeComponents() {
        // File info
        long fileSize = file.length();
        String sizeStr = formatFileSize(fileSize);
        fileInfoLabel = new JLabel("<html><b>File:</b> " + file.getName() + 
                                  "<br><b>Size:</b> " + sizeStr + "</html>");
        fileInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Description field - flexible width
        descriptionField = new JTextField();
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionField.setPreferredSize(new Dimension(300, 25));
        descriptionField.setMinimumSize(new Dimension(200, 25));
        
        // Tags field - flexible width
        tagsField = new JTextField();
        tagsField.setFont(new Font("Arial", Font.PLAIN, 12));
        tagsField.setPreferredSize(new Dimension(300, 25));
        tagsField.setMinimumSize(new Dimension(200, 25));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // File info
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(fileInfoLabel, gbc);
        
        // Description label and field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(descLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(descriptionField, gbc);
        
        // Tags label and field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel tagsLabel = new JLabel("Tags:");
        tagsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(tagsLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(tagsField, gbc);
        
        // Tags help text
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel tagsHelp = new JLabel("(Separate multiple tags with commas)");
        tagsHelp.setFont(new Font("Arial", Font.ITALIC, 10));
        tagsHelp.setForeground(Color.GRAY);
        mainPanel.add(tagsHelp, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add to Vault");
        JButton cancelButton = new JButton("Cancel");
        
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        // Add panels
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Setup button handlers
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }
    
    private void setupEventHandlers() {
        // Enter key support
        descriptionField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tagsField.requestFocus();
            }
        });
        
        tagsField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                dispose();
            }
        });
    }
    
    private void configureDialog() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        pack();
        
        // Set minimum size for dialog
        setMinimumSize(new Dimension(400, 200));
        
        setLocationRelativeTo(getParent());
        
        // Focus on description field
        SwingUtilities.invokeLater(() -> descriptionField.requestFocus());
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getDescription() {
        String desc = descriptionField.getText().trim();
        return desc.isEmpty() ? "No description" : desc;
    }
    
    public String getTags() {
        return tagsField.getText().trim();
    }
}
