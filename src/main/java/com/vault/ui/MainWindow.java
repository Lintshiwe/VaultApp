package com.vault.ui;

import com.vault.model.Admin;
import com.vault.model.VaultFile;
import com.vault.service.VaultService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Main window for vault management
 */
public class MainWindow extends JFrame {
    
    private Admin currentAdmin;
    private VaultService vaultService;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel statsLabel;
    
    public MainWindow(Admin admin, String password) {
        this.currentAdmin = admin;
        this.vaultService = VaultService.getInstance();
        
        // Set encryption key using admin's salt and provided password
        vaultService.setEncryptionKey(password, admin.getSalt());
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureWindow();
        refreshFileList();
        updateStats();
    }
    
    private void initializeComponents() {
        // Table for files
        String[] columnNames = {"Name", "Type", "Size", "Date Added", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(25);
        fileTable.getTableHeader().setReorderingAllowed(false);
        
        // Search field
        searchField = new JTextField(20);
        searchField.setToolTipText("Search files by name, description, or tags");
        
        // Status and stats labels
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(0, 150, 0));
        
        statsLabel = new JLabel("Files: 0, Total Size: 0 B");
        statsLabel.setForeground(Color.GRAY);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with title and user info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(45, 45, 45));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Secure Vault");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("Logged in as: " + currentAdmin.getUsername());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(Color.LIGHT_GRAY);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(userLabel, BorderLayout.EAST);
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(new Color(55, 55, 55));
        
        JButton addButton = createToolbarButton("Add File", "Add a new file to the vault");
        JButton retrieveButton = createToolbarButton("Retrieve", "Retrieve selected file from vault");
        JButton deleteButton = createToolbarButton("Delete", "Delete selected file from vault");
        JButton refreshButton = createToolbarButton("Refresh", "Refresh file list");
        JButton logoutButton = createToolbarButton("Logout", "Logout and close application");
        
        toolbarPanel.add(addButton);
        toolbarPanel.add(retrieveButton);
        toolbarPanel.add(deleteButton);
        toolbarPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolbarPanel.add(refreshButton);
        toolbarPanel.add(Box.createHorizontalGlue());
        toolbarPanel.add(logoutButton);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(60, 60, 60));
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Search", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            Color.WHITE
        ));
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Vault Files",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            Color.WHITE
        ));
        
        // Bottom panel with status
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(70, 70, 70));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(statsLabel, BorderLayout.EAST);
        
        // Add all panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(toolbarPanel, BorderLayout.SOUTH);
        
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(searchPanel, BorderLayout.NORTH);
        
        add(northPanel, BorderLayout.NORTH);
        add(westPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Setup event handlers
        setupButtonHandlers(addButton, retrieveButton, deleteButton, refreshButton, 
                          logoutButton, searchButton, clearButton);
    }
    
    private JButton createToolbarButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }
    
    private void setupButtonHandlers(JButton addButton, JButton retrieveButton, 
                                   JButton deleteButton, JButton refreshButton,
                                   JButton logoutButton, JButton searchButton, 
                                   JButton clearButton) {
        
        addButton.addActionListener(e -> showAddFileDialog());
        retrieveButton.addActionListener(e -> retrieveSelectedFile());
        deleteButton.addActionListener(e -> deleteSelectedFile());
        refreshButton.addActionListener(e -> {
            refreshFileList();
            updateStats();
        });
        logoutButton.addActionListener(e -> logout());
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearSearch());
        
        // Enter key for search
        searchField.addActionListener(e -> performSearch());
    }
    
    private void setupEventHandlers() {
        // Double-click to retrieve file
        fileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    retrieveSelectedFile();
                }
            }
        });
    }
    
    private void configureWindow() {
        setTitle("Secure Vault - File Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Set icon
        try {
            setIconImage(createIcon());
        } catch (Exception e) {
            // Ignore if icon creation fails
        }
    }
    
    private void showAddFileDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Show file details dialog
            AddFileDialog dialog = new AddFileDialog(this, selectedFile);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                addFileToVault(selectedFile, dialog.getDescription(), dialog.getTags());
            }
        }
    }
    
    private void addFileToVault(File file, String description, String tags) {
        setStatus("Adding file to vault...");
        
        SwingWorker<VaultFile, Void> worker = new SwingWorker<VaultFile, Void>() {
            @Override
            protected VaultFile doInBackground() throws Exception {
                return vaultService.storeFile(file, description, tags);
            }
            
            @Override
            protected void done() {
                try {
                    VaultFile vaultFile = get();
                    setStatus("File added successfully: " + vaultFile.getOriginalName());
                    refreshFileList();
                    updateStats();
                } catch (Exception e) {
                    setStatus("Failed to add file: " + e.getMessage());
                    showError("Failed to add file: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void retrieveSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a file to retrieve.");
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        VaultFile vaultFile = findVaultFileByName(fileName);
        
        if (vaultFile == null) {
            showError("Selected file not found.");
            return;
        }
        
        // Choose output directory
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setDialogTitle("Select output directory");
        
        int result = dirChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputDir = dirChooser.getSelectedFile();
            retrieveFile(vaultFile, outputDir.getAbsolutePath());
        }
    }
    
    private void retrieveFile(VaultFile vaultFile, String outputDirectory) {
        setStatus("Retrieving file...");
        
        SwingWorker<File, Void> worker = new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                return vaultService.retrieveFile(vaultFile, outputDirectory);
            }
            
            @Override
            protected void done() {
                try {
                    File retrievedFile = get();
                    setStatus("File retrieved successfully: " + retrievedFile.getName());
                    
                    int choice = JOptionPane.showConfirmDialog(
                        MainWindow.this,
                        "File retrieved to: " + retrievedFile.getAbsolutePath() + 
                        "\n\nWould you like to open the containing folder?",
                        "File Retrieved",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(retrievedFile.getParentFile());
                        } catch (Exception e) {
                            // Ignore if can't open folder
                        }
                    }
                } catch (Exception e) {
                    setStatus("Failed to retrieve file: " + e.getMessage());
                    showError("Failed to retrieve file: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void deleteSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a file to delete.");
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        VaultFile vaultFile = findVaultFileByName(fileName);
        
        if (vaultFile == null) {
            showError("Selected file not found.");
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete '" + fileName + "'?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            deleteFile(vaultFile);
        }
    }
    
    private void deleteFile(VaultFile vaultFile) {
        setStatus("Deleting file...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return vaultService.deleteFile(vaultFile);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        setStatus("File deleted successfully: " + vaultFile.getOriginalName());
                        refreshFileList();
                        updateStats();
                    } else {
                        setStatus("Failed to delete file");
                        showError("Failed to delete file");
                    }
                } catch (Exception e) {
                    setStatus("Failed to delete file: " + e.getMessage());
                    showError("Failed to delete file: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            refreshFileList();
            return;
        }
        
        setStatus("Searching...");
        
        SwingWorker<List<VaultFile>, Void> worker = new SwingWorker<List<VaultFile>, Void>() {
            @Override
            protected List<VaultFile> doInBackground() throws Exception {
                return vaultService.searchFiles(searchTerm);
            }
            
            @Override
            protected void done() {
                try {
                    List<VaultFile> files = get();
                    updateFileTable(files);
                    setStatus("Search completed. Found " + files.size() + " files.");
                } catch (Exception e) {
                    setStatus("Search failed: " + e.getMessage());
                    showError("Search failed: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void clearSearch() {
        searchField.setText("");
        refreshFileList();
    }
    
    private void refreshFileList() {
        setStatus("Loading files...");
        
        SwingWorker<List<VaultFile>, Void> worker = new SwingWorker<List<VaultFile>, Void>() {
            @Override
            protected List<VaultFile> doInBackground() throws Exception {
                return vaultService.getAllFiles();
            }
            
            @Override
            protected void done() {
                try {
                    List<VaultFile> files = get();
                    updateFileTable(files);
                    setStatus("Files loaded successfully.");
                } catch (Exception e) {
                    setStatus("Failed to load files: " + e.getMessage());
                    showError("Failed to load files: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateFileTable(List<VaultFile> files) {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add new rows
        for (VaultFile file : files) {
            Object[] row = {
                file.getOriginalName(),
                file.getFileType().toUpperCase(),
                file.getFormattedSize(),
                file.getDateAdded().toLocalDate().toString(),
                file.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStats() {
        SwingWorker<VaultService.VaultStats, Void> worker = new SwingWorker<VaultService.VaultStats, Void>() {
            @Override
            protected VaultService.VaultStats doInBackground() throws Exception {
                return vaultService.getVaultStats();
            }
            
            @Override
            protected void done() {
                try {
                    VaultService.VaultStats stats = get();
                    statsLabel.setText("Files: " + stats.getFileCount() + 
                                     ", Total Size: " + stats.getFormattedTotalSize());
                } catch (Exception e) {
                    statsLabel.setText("Stats unavailable");
                }
            }
        };
        
        worker.execute();
    }
    
    private VaultFile findVaultFileByName(String fileName) {
        try {
            List<VaultFile> allFiles = vaultService.getAllFiles();
            return allFiles.stream()
                    .filter(f -> f.getOriginalName().equals(fileName))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
            });
        }
    }
    
    private void setStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(0, 150, 0));
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Error: " + message);
        statusLabel.setForeground(Color.RED);
    }
    
    private Image createIcon() {
        int size = 32;
        Image icon = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) icon.getGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(100, 100, 200));
        g2d.fillOval(2, 2, size - 4, size - 4);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "V";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return icon;
    }
}
