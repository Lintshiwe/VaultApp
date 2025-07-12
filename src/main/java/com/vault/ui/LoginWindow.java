package com.vault.ui;

import com.vault.model.Admin;
import com.vault.util.DatabaseManager;
import com.vault.util.SecureErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Login window for admin authentication
 */
public class LoginWindow extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private DatabaseManager dbManager;
    
    public LoginWindow() {
        this.dbManager = DatabaseManager.getInstance();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Username field with flexible sizing
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernameField.setMinimumSize(new Dimension(150, 30));
        
        // Password field with flexible sizing
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setMinimumSize(new Dimension(150, 30));
        
        // Buttons with consistent sizing
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exitButton.setPreferredSize(new Dimension(100, 35));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(45, 45, 45));
        JLabel titleLabel = new JLabel("Secure Vault");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(60, 60, 60));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username label and field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(Color.WHITE);
        mainPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(usernameField, gbc);
        
        // Password label and field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(Color.WHITE);
        mainPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(60, 60, 60));
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(70, 70, 70));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel infoLabel = new JLabel("<html><center>Default login:<br/>Username: admin<br/>Password: admin123</center></html>");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(Color.LIGHT_GRAY);
        infoPanel.add(infoLabel);
        
        // Add panels to frame
        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // Enter key support
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        });
    }
    
    private void configureWindow() {
        setTitle("Secure Vault - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        pack();
        
        // Set minimum size for login window
        setMinimumSize(new Dimension(400, 300));
        
        setLocationRelativeTo(null);
        
        // Set icon
        try {
            setIconImage(createIcon());
        } catch (Exception e) {
            // Ignore if icon creation fails
        }
        
        // Focus on username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }
        
        // Disable login button during authentication
        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        
        // Perform authentication in background thread
        SwingWorker<Admin, Void> worker = new SwingWorker<Admin, Void>() {
            @Override
            protected Admin doInBackground() throws Exception {
                return dbManager.authenticateAdmin(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    Admin admin = get();
                    
                    if (admin != null) {
                        // Authentication successful
                        dispose();
                        
                        // Open main window
                        SwingUtilities.invokeLater(() -> {
                            try {
                                MainWindow mainWindow = new MainWindow(admin, password);
                                mainWindow.setVisible(true);
                            } catch (Exception e) {
                                showError("Failed to open main window");
                                SecureErrorHandler.handleApplicationError(e);
                            }
                        });
                    } else {
                        // Authentication failed
                        showError("Invalid username or password.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    showError("Authentication error occurred");
                    SecureErrorHandler.handleAuthenticationError(e);
                } finally {
                    // Re-enable login button
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        
        worker.execute();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private Image createIcon() {
        // Create a simple icon
        int size = 32;
        Image icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
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
