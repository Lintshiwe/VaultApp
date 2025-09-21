package com.vault.ui;

import com.vault.model.Admin;
import com.vault.model.VaultFile;
import com.vault.service.VaultService;
import com.vault.service.AutoUpdater;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

/**
 * Main window for vault management with professional features
 */
public class MainWindow extends JFrame {
    
    private Admin currentAdmin;
    private VaultService vaultService;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JLabel spaceLabel;
    private JProgressBar spaceProgressBar;
    private JScrollPane scrollPane;
    private SystemTrayManager trayManager;
    
    public MainWindow(Admin admin, String password) {
        this.currentAdmin = admin;
        this.vaultService = VaultService.getInstance();
        
        // Set encryption key using admin's salt and provided password
        vaultService.setEncryptionKey(password, admin.getSalt());
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureWindow();
        setupSystemTray();
        refreshFileList();
        updateStats();
        updateSpaceInfo();
    }
    
    private void initializeComponents() {
        // Table for files with auto-resize columns
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
        fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set preferred column widths for flexible sizing
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Name
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Type
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Size
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(250); // Description
        
        // Search field with flexible sizing
        searchField = new JTextField();
        searchField.setToolTipText("Search files by name, description, or tags");
        
        // Status and stats labels
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(0, 150, 0));
        
        statsLabel = new JLabel("Files: 0, Total Size: 0 B");
        statsLabel.setForeground(Color.GRAY);
        
        spaceLabel = new JLabel("Disk Space: Calculating...");
        spaceLabel.setForeground(Color.LIGHT_GRAY);
        
        spaceProgressBar = new JProgressBar(0, 100);
        spaceProgressBar.setStringPainted(true);
        spaceProgressBar.setString("Loading...");
        spaceProgressBar.setPreferredSize(new Dimension(200, 20));
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
        
        // Toolbar with flexible layout
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(new Color(55, 55, 55));
        
        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftToolbar.setBackground(new Color(55, 55, 55));
        
        JButton addButton = createToolbarButton("Add File/Folder", "Add files or folders to the vault");
        JButton openButton = createToolbarButton("Open/Play", "Open or play selected file");
        JButton retrieveButton = createToolbarButton("Retrieve", "Retrieve selected file from vault");
        JButton deleteButton = createToolbarButton("Delete", "Delete selected file from vault");
        JButton refreshButton = createToolbarButton("Refresh", "Refresh file list");
        JButton spaceButton = createToolbarButton("Space Info", "View detailed space usage information");
        
        leftToolbar.add(addButton);
        leftToolbar.add(openButton);
        leftToolbar.add(retrieveButton);
        leftToolbar.add(deleteButton);
        leftToolbar.add(Box.createRigidArea(new Dimension(10, 0)));
        leftToolbar.add(refreshButton);
        leftToolbar.add(spaceButton);
        
        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightToolbar.setBackground(new Color(55, 55, 55));
        JButton aboutButton = createToolbarButton("About", "Copyright and license information");
        JButton settingsButton = createToolbarButton("Settings", "Change login credentials");
        JButton logoutButton = createToolbarButton("Logout", "Logout and close application");
        rightToolbar.add(aboutButton);
        rightToolbar.add(settingsButton);
        rightToolbar.add(logoutButton);
        
        toolbarPanel.add(leftToolbar, BorderLayout.WEST);
        toolbarPanel.add(rightToolbar, BorderLayout.EAST);
        
        // Search panel - make it flexible
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(60, 60, 60));
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Search", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            Color.WHITE
        ));
        
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchControls.setBackground(new Color(60, 60, 60));
        searchControls.add(new JLabel("Search:"));
        searchControls.add(searchField);
        
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        searchControls.add(searchButton);
        searchControls.add(clearButton);
        
        searchPanel.add(searchControls, BorderLayout.WEST);
        
        // Make search field expandable
        searchField.setPreferredSize(new Dimension(250, 25));
        searchField.setMinimumSize(new Dimension(200, 25));
        
        // Center panel with table
        scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Vault Files",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            Color.WHITE
        ));
        
        // Bottom panel with status and space info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(70, 70, 70));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Status on the left
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        
        // Stats and space info on the right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(new Color(70, 70, 70));
        rightPanel.add(statsLabel);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightPanel.add(spaceLabel);
        rightPanel.add(spaceProgressBar);
        
        bottomPanel.add(rightPanel, BorderLayout.EAST);
        
        // Add all panels with flexible layout
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(toolbarPanel, BorderLayout.CENTER);
        northPanel.add(searchPanel, BorderLayout.SOUTH);
        
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Setup event handlers
        setupButtonHandlers(addButton, openButton, retrieveButton, deleteButton, refreshButton, 
                          spaceButton, aboutButton, settingsButton, logoutButton, searchButton, clearButton);
    }
    
    private JButton createToolbarButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }
    
    private void setupButtonHandlers(JButton addButton, JButton openButton, JButton retrieveButton, 
                                   JButton deleteButton, JButton refreshButton,
                                   JButton spaceButton, JButton aboutButton, JButton settingsButton, JButton logoutButton, 
                                   JButton searchButton, JButton clearButton) {
        
        addButton.addActionListener(e -> showAddFileDialog());
        openButton.addActionListener(e -> openSelectedFile());
        retrieveButton.addActionListener(e -> retrieveSelectedFile());
        deleteButton.addActionListener(e -> deleteSelectedFile());
        refreshButton.addActionListener(e -> {
            refreshFileList();
            updateStats();
            updateSpaceInfo();
        });
        spaceButton.addActionListener(e -> showSpaceManagementDialog());
        aboutButton.addActionListener(e -> showAboutDialog());
        settingsButton.addActionListener(e -> showChangeCredentialsDialog());
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
        
        // Window resize listener for dynamic adjustment
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                adjustTableColumns();
            }
        });
    }
    
    /**
     * Adjust table column widths based on window size
     */
    private void adjustTableColumns() {
        if (fileTable != null && fileTable.getColumnModel() != null) {
            int tableWidth = scrollPane.getViewport().getWidth();
            if (tableWidth > 0) {
                // Calculate proportional widths
                int nameWidth = (int)(tableWidth * 0.25);
                int typeWidth = (int)(tableWidth * 0.10);
                int sizeWidth = (int)(tableWidth * 0.12);
                int dateWidth = (int)(tableWidth * 0.15);
                int descWidth = (int)(tableWidth * 0.38);
                
                fileTable.getColumnModel().getColumn(0).setPreferredWidth(nameWidth);
                fileTable.getColumnModel().getColumn(1).setPreferredWidth(typeWidth);
                fileTable.getColumnModel().getColumn(2).setPreferredWidth(sizeWidth);
                fileTable.getColumnModel().getColumn(3).setPreferredWidth(dateWidth);
                fileTable.getColumnModel().getColumn(4).setPreferredWidth(descWidth);
            }
        }
    }
    
    private void configureWindow() {
        updateWindowTitle();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Make window dynamically sized based on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(900, (int)(screenSize.width * 0.7));
        int height = Math.max(650, (int)(screenSize.height * 0.7));
        setSize(width, height);
        
        // Set minimum size to ensure usability
        setMinimumSize(new Dimension(800, 600));
        
        setLocationRelativeTo(null);
        
        // Set icon
        try {
            setIconImage(createIcon());
        } catch (Exception e) {
            // Ignore if icon creation fails
        }
    }
    
    /**
     * Setup system tray integration
     */
    private void setupSystemTray() {
        trayManager = SystemTrayManager.getInstance();
        trayManager.initialize(this, vaultService);
        
        // Override window closing behavior for tray integration
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SystemTray.isSupported() && 
                    SettingsDialog.getPreferences().getBoolean("minimize_to_tray", true)) {
                    trayManager.minimizeToTray();
                } else {
                    exitApplication();
                }
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                if (SystemTray.isSupported() && 
                    SettingsDialog.getPreferences().getBoolean("minimize_to_tray", true)) {
                    trayManager.minimizeToTray();
                }
            }
        });
    }
    
    /**
     * Show add file dialog (called from system tray)
     */
    public void showAddFileDialog() {
        SwingUtilities.invokeLater(() -> {
            AddFileDialog dialog = new AddFileDialog(this, vaultService);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                refreshFileList();
                updateStats();
                updateSpaceInfo();
            }
        });
    }
    
    /**
     * Show change password dialog (called from system tray)
     */
    public void showChangePasswordDialog() {
        SwingUtilities.invokeLater(() -> {
            ChangeCredentialsDialog dialog = new ChangeCredentialsDialog(this, currentAdmin);
            dialog.setVisible(true);
        });
    }
    
    /**
     * Exit application properly
     */
    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit Secure Vault?\n\nAll unsaved work will be lost.",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Clean up system tray
            if (trayManager != null) {
                trayManager.cleanup();
            }
            
            // Clean shutdown
            vaultService.cleanup();
            dispose();
            System.exit(0);
        }
    }
    
    private void showAddFileDialog() {
        // Create a dialog to choose between file and folder
        String[] options = {"Select File", "Select Folder", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "What would you like to add to the vault?",
            "Add to Vault",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice == 0) {
            // Select single file
            selectSingleFile();
        } else if (choice == 1) {
            // Select folder
            selectFolder();
        }
        // If cancel (choice == 2 or dialog closed), do nothing
    }
    
    private void selectSingleFile() {
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
    
    private void selectFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setMultiSelectionEnabled(false);
        
        int result = folderChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            
            // Get all files in the folder recursively
            java.util.List<File> filesToAdd = new java.util.ArrayList<>();
            collectFilesRecursively(selectedFolder, filesToAdd);
            
            if (filesToAdd.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No files found in the selected folder.", 
                    "Empty Folder", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Show confirmation dialog with file count
            int fileCount = filesToAdd.size();
            String message = String.format(
                "Found %d file(s) in the selected folder.\n" +
                "Do you want to add all files to the vault?\n\n" +
                "Note: Large folders may take some time to process.",
                fileCount
            );
            
            int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Folder Upload",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                addFolderToVault(selectedFolder, filesToAdd);
            }
        }
    }
    
    private void collectFilesRecursively(File directory, java.util.List<File> fileList) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file);
                } else if (file.isDirectory()) {
                    collectFilesRecursively(file, fileList);
                }
            }
        }
    }
    
    private void addFolderToVault(File folder, java.util.List<File> files) {
        // Create progress dialog
        JProgressBar progressBar = new JProgressBar(0, files.size());
        progressBar.setStringPainted(true);
        progressBar.setString("Preparing...");
        
        JDialog progressDialog = new JDialog(this, "Adding Folder to Vault", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(new JLabel("Adding files from: " + folder.getName()), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        
        JButton cancelButton = new JButton("Cancel");
        progressDialog.add(cancelButton, BorderLayout.SOUTH);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);
        
        // Create worker for background processing
        FolderUploadWorker worker = new FolderUploadWorker(folder, files, progressBar, progressDialog);
        
        cancelButton.addActionListener(e -> {
            worker.cancel(true);
            progressDialog.dispose();
        });
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    // Inner class for folder upload worker
    private class FolderUploadWorker extends SwingWorker<Void, String> {
        private final File folder;
        private final java.util.List<File> files;
        private final JProgressBar progressBar;
        private final JDialog progressDialog;
        private int successful = 0;
        
        public FolderUploadWorker(File folder, java.util.List<File> files, JProgressBar progressBar, JDialog progressDialog) {
            this.folder = folder;
            this.files = files;
            this.progressBar = progressBar;
            this.progressDialog = progressDialog;
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            int processed = 0;
            
            for (File file : files) {
                if (isCancelled()) {
                    break;
                }
                
                try {
                    // Create relative path for description
                    String relativePath = folder.toPath().relativize(file.toPath()).toString();
                    String description = "From folder: " + folder.getName() + " (" + relativePath + ")";
                    
                    // Check if file can be stored (space validation)
                    if (vaultService.canStoreFile(file)) {
                        vaultService.storeFile(file, description, "folder-upload");
                        successful++;
                    } else {
                        System.err.println("Insufficient space for file: " + file.getName());
                    }
                    
                } catch (Exception e) {
                    System.err.println("Failed to add file " + file.getName() + ": " + e.getMessage());
                }
                
                processed++;
                publish(String.format("Processing file %d of %d", processed, files.size()));
            }
            
            return null;
        }
        
        @Override
        protected void process(java.util.List<String> chunks) {
            if (!chunks.isEmpty()) {
                String message = chunks.get(chunks.size() - 1);
                progressBar.setString(message);
                // Update progress bar value based on the message
                if (message.contains("Processing file")) {
                    try {
                        String[] parts = message.split(" ");
                        int current = Integer.parseInt(parts[2]);
                        progressBar.setValue(current);
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
        }
        
        @Override
        protected void done() {
            progressDialog.dispose();
            
            String resultMessage = String.format(
                "Folder upload completed!\n\n" +
                "Total files: %d\n" +
                "Successfully added: %d\n" +
                "Failed/Skipped: %d",
                files.size(), successful, files.size() - successful
            );
            
            JOptionPane.showMessageDialog(
                MainWindow.this,
                resultMessage,
                "Upload Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Refresh the file list
            refreshFileList();
            updateSpaceInfo();
        }
    }
    
    private void addFileToVault(File file, String description, String tags) {
        // Check space before starting upload
        if (!checkSpaceBeforeUpload(file)) {
            return;
        }
        
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
                    MainWindow.this.updateSpaceInfo();
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
                        MainWindow.this.updateSpaceInfo();
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
                    updateSpaceDisplay(diskStatus);
                } catch (Exception e) {
                    spaceLabel.setText("Space: Unavailable");
                    spaceProgressBar.setString("Error");
                    spaceProgressBar.setValue(0);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateSpaceDisplay(VaultService.DiskSpaceStatus diskStatus) {
        VaultService.SpaceInfo spaceInfo = diskStatus.getSpaceInfo();
        
        // Update space label with current usage and recommendations
        String spaceText = String.format("Used: %s | Free: %s | Recommended: %s", 
            spaceInfo.getFormattedUsedSpace(),
            diskStatus.getFormattedFreeSpace(),
            spaceInfo.getFormattedRecommendedSpace()
        );
        spaceLabel.setText(spaceText);
        
        // Update progress bar
        double usagePercent = diskStatus.getUsagePercentage();
        spaceProgressBar.setValue((int) usagePercent);
        spaceProgressBar.setString(String.format("%.1f%% used", usagePercent));
        
        // Color coding based on space availability
        if (!diskStatus.hasEnoughSpace()) {
            spaceLabel.setForeground(Color.RED);
            spaceProgressBar.setForeground(Color.RED);
        } else if (!diskStatus.hasRecommendedSpace()) {
            spaceLabel.setForeground(Color.ORANGE);
            spaceProgressBar.setForeground(Color.ORANGE);
        } else {
            spaceLabel.setForeground(Color.GREEN);
            spaceProgressBar.setForeground(Color.GREEN);
        }
    }
    
    private boolean checkSpaceBeforeUpload(File file) {
        if (!vaultService.canStoreFile(file.length())) {
            long estimatedSpace = vaultService.getEstimatedSpaceForFile(file.length());
            String estimatedSpaceStr = formatFileSize(estimatedSpace);
            
            VaultService.DiskSpaceStatus diskStatus = vaultService.checkDiskSpace();
            String availableSpaceStr = diskStatus.getFormattedFreeSpace();
            
            String message = String.format(
                "Insufficient disk space to store this file.\n\n" +
                "File size: %s\n" +
                "Estimated space needed: %s\n" +
                "Available space: %s\n\n" +
                "Please free up some disk space or remove some files from the vault.",
                formatFileSize(file.length()),
                estimatedSpaceStr,
                availableSpaceStr
            );
            
            JOptionPane.showMessageDialog(this, message, "Insufficient Disk Space", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
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
    
    private void showSpaceManagementDialog() {
        SwingUtilities.invokeLater(() -> {
            SpaceManagementDialog dialog = new SpaceManagementDialog(this, vaultService);
            dialog.setVisible(true);
        });
    }
    
    private void showChangeCredentialsDialog() {
        SwingUtilities.invokeLater(() -> {
            ChangeCredentialsDialog dialog = new ChangeCredentialsDialog(this, currentAdmin);
            dialog.setVisible(true);
            
            // If credentials were changed, update the title bar
            if (dialog.isCredentialsChanged()) {
                updateWindowTitle();
            }
        });
    }
    
    private void updateWindowTitle() {
        setTitle("Secure Vault - " + currentAdmin.getUsername());
    }
    
    private void showAboutDialog() {
        SwingUtilities.invokeLater(() -> {
            AboutDialog dialog = new AboutDialog(this);
            dialog.setVisible(true);
        });
    }
    
    private void openSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a file to open/play.");
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, 0);
        VaultFile vaultFile = findVaultFileByName(fileName);
        
        if (vaultFile == null) {
            showError("Selected file not found.");
            return;
        }
        
        // Create a temporary file to open
        try {
            // Create temp directory
            java.nio.file.Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), ".securevault_temp");
            if (!java.nio.file.Files.exists(tempDir)) {
                java.nio.file.Files.createDirectories(tempDir);
            }
            
            // Create temp file with original extension
            String originalName = vaultFile.getOriginalName();
            String extension = "";
            int lastDot = originalName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = originalName.substring(lastDot);
            }
            
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile(tempDir, "vault_", extension);
            File outputFile = tempFile.toFile();
            
            // Set file to be deleted on exit for security
            outputFile.deleteOnExit();
            
            // Retrieve and decrypt the file
            File tempFileDir = tempFile.getParent().toFile();
            File retrievedFile = vaultService.retrieveFile(vaultFile, tempFileDir.getAbsolutePath());
            
            if (retrievedFile != null && retrievedFile.exists()) {
                // Rename to have correct extension if needed
                if (!retrievedFile.getName().equals(tempFile.getFileName().toString())) {
                    java.nio.file.Files.move(retrievedFile.toPath(), tempFile);
                    outputFile = tempFile.toFile();
                } else {
                    outputFile = retrievedFile;
                }
                outputFile.deleteOnExit();
                
                // Open the file with system default application
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    
                    if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                        desktop.open(outputFile);
                        setStatus("Opened: " + originalName);
                        
                        // Show info about temporary file
                        String message = String.format(
                            "File opened successfully!\\n\\n" +
                            "File: %s\\n" +
                            "Note: This is a temporary copy that will be deleted when the application closes.",
                            originalName
                        );
                        
                        JOptionPane.showMessageDialog(this, message, "File Opened", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showError("System does not support opening files.");
                    }
                } else {
                    showError("Desktop operations not supported on this system.");
                }
            } else {
                showError("Failed to decrypt file.");
            }
            
        } catch (Exception e) {
            setStatus("Failed to open file: " + e.getMessage());
            showError("Failed to open file: " + e.getMessage());
        }
    }
}
