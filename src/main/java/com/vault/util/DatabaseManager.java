package com.vault.util;

import com.vault.model.Admin;
import com.vault.model.VaultFile;
import com.vault.config.SecurityConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database manager for SQLite operations
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:vault.db?journal_mode=WAL&busy_timeout=30000";
    
    private DatabaseManager() {}
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Initialize database and create tables
     */
    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            createTables(conn);
            createDefaultAdmin(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private void createTables(Connection conn) throws SQLException {
        // Create admins table
        String adminTable = """
            CREATE TABLE IF NOT EXISTS admins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                created_at TEXT NOT NULL,
                last_login TEXT,
                is_active BOOLEAN DEFAULT 1
            )
        """;
        
        // Create vault_files table
        String filesTable = """
            CREATE TABLE IF NOT EXISTS vault_files (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                original_name TEXT NOT NULL,
                encrypted_path TEXT NOT NULL,
                file_type TEXT,
                file_size INTEGER,
                date_added TEXT NOT NULL,
                description TEXT,
                tags TEXT
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(adminTable);
            stmt.execute(filesTable);
        }
    }
    
    private void createDefaultAdmin(Connection conn) throws SQLException {
        // Check if default admin exists
        String checkSql = "SELECT COUNT(*) FROM admins WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, "admin");
            ResultSet rs = stmt.executeQuery();
            if (rs.getInt(1) == 0) {
                // Create default admin with password "admin123"
                String salt = SecurityUtil.generateSalt();
                String passwordHash = SecurityUtil.hashPassword("admin123", salt);
                
                String insertSql = """
                    INSERT INTO admins (username, password_hash, salt, created_at, is_active)
                    VALUES (?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, passwordHash);
                    insertStmt.setString(3, salt);
                    insertStmt.setString(4, LocalDateTime.now().toString());
                    insertStmt.setBoolean(5, true);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Authenticate admin user
     */
    public Admin authenticateAdmin(String username, String password) {
        String sql = "SELECT * FROM admins WHERE username = ? AND is_active = 1";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                
                if (SecurityUtil.verifyPassword(password, storedHash, salt)) {
                    Admin admin = new Admin();
                    admin.setId(rs.getInt("id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setPasswordHash(storedHash);
                    admin.setSalt(salt);
                    admin.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
                    
                    String lastLoginStr = rs.getString("last_login");
                    if (lastLoginStr != null) {
                        admin.setLastLogin(LocalDateTime.parse(lastLoginStr));
                    }
                    
                    admin.setActive(rs.getBoolean("is_active"));
                    
                    // Update last login
                    updateLastLogin(admin.getId());
                    
                    return admin;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate admin", e);
        }
        
        return null;
    }
    
    private void updateLastLogin(int adminId) {
        String sql = "UPDATE admins SET last_login = ? WHERE id = ?";
        
        // Retry mechanism for database locks
        int maxRetries = 3;
        int retryDelay = 100; // milliseconds
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                conn.setAutoCommit(true);
                stmt.setString(1, LocalDateTime.now().toString());
                stmt.setInt(2, adminId);
                stmt.executeUpdate();
                return; // Success, exit retry loop
                
            } catch (SQLException e) {
                if (attempt == maxRetries - 1) {
                    // Last attempt failed
                    System.err.println("Failed to update last login after " + maxRetries + " attempts: " + e.getMessage());
                } else {
                    // Wait before retry
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Save vault file to database
     */
    public int saveVaultFile(VaultFile file) {
        String sql = """
            INSERT INTO vault_files (original_name, encrypted_path, file_type, file_size, 
                                   date_added, description, tags)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, file.getOriginalName());
            stmt.setString(2, file.getEncryptedPath());
            stmt.setString(3, file.getFileType());
            stmt.setLong(4, file.getFileSize());
            stmt.setString(5, file.getDateAdded().toString());
            stmt.setString(6, file.getDescription());
            stmt.setString(7, file.getTags());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save vault file", e);
        }
        
        return -1;
    }
    
    /**
     * Get all vault files
     */
    public List<VaultFile> getAllVaultFiles() {
        List<VaultFile> files = new ArrayList<>();
        String sql = "SELECT * FROM vault_files ORDER BY date_added DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                VaultFile file = new VaultFile();
                file.setId(rs.getInt("id"));
                file.setOriginalName(rs.getString("original_name"));
                file.setEncryptedPath(rs.getString("encrypted_path"));
                file.setFileType(rs.getString("file_type"));
                file.setFileSize(rs.getLong("file_size"));
                file.setDateAdded(LocalDateTime.parse(rs.getString("date_added")));
                file.setDescription(rs.getString("description"));
                file.setTags(rs.getString("tags"));
                
                files.add(file);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get vault files", e);
        }
        
        return files;
    }
    
    /**
     * Delete vault file from database
     */
    public boolean deleteVaultFile(long fileId) {
        String sql = "DELETE FROM vault_files WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, fileId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete vault file", e);
        }
    }
    
    /**
     * Search vault files by name or tags
     */
    public List<VaultFile> searchVaultFiles(String searchTerm) {
        List<VaultFile> files = new ArrayList<>();
        String sql = """
            SELECT * FROM vault_files 
            WHERE original_name LIKE ? OR tags LIKE ? OR description LIKE ?
            ORDER BY date_added DESC
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                VaultFile file = new VaultFile();
                file.setId(rs.getInt("id"));
                file.setOriginalName(rs.getString("original_name"));
                file.setEncryptedPath(rs.getString("encrypted_path"));
                file.setFileType(rs.getString("file_type"));
                file.setFileSize(rs.getLong("file_size"));
                file.setDateAdded(LocalDateTime.parse(rs.getString("date_added")));
                file.setDescription(rs.getString("description"));
                file.setTags(rs.getString("tags"));
                
                files.add(file);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search vault files", e);
        }
        
        return files;
    }
    
    /**
     * Update admin credentials (username and/or password)
     */
    public boolean updateAdminCredentials(int adminId, String newUsername, String newPassword) {
        String checkUsernameSql = "SELECT COUNT(*) FROM admins WHERE username = ? AND id != ?";
        String updateSql = "UPDATE admins SET username = ?, password_hash = ?, salt = ? WHERE id = ?";
        
        // Retry mechanism for database locks
        int maxRetries = 3;
        int retryDelay = 100; // milliseconds
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.setAutoCommit(false);
                
                // Check if new username already exists (if username is being changed)
                if (newUsername != null && !newUsername.trim().isEmpty()) {
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql)) {
                        checkStmt.setString(1, newUsername.trim());
                        checkStmt.setInt(2, adminId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.getInt(1) > 0) {
                            conn.rollback();
                            return false; // Username already exists
                        }
                    }
                }
                
                // Generate new salt and hash for password
                String newSalt = SecurityUtil.generateSalt();
                String newPasswordHash = SecurityUtil.hashPassword(newPassword, newSalt);
                
                // Update credentials
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newUsername != null ? newUsername.trim() : getCurrentUsername(adminId));
                    updateStmt.setString(2, newPasswordHash);
                    updateStmt.setString(3, newSalt);
                    updateStmt.setInt(4, adminId);
                    
                    int rowsUpdated = updateStmt.executeUpdate();
                    
                    if (rowsUpdated > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
            } catch (SQLException e) {
                if (attempt == maxRetries - 1) {
                    System.err.println("Failed to update admin credentials after " + maxRetries + " attempts: " + e.getMessage());
                    return false;
                } else {
                    // Wait before retry
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update admin credentials with specific salt (used for re-encryption)
     */
    public boolean updateAdminCredentials(int adminId, String newUsername, String newPassword, String newSalt) {
        String checkUsernameSql = "SELECT COUNT(*) FROM admins WHERE username = ? AND id != ?";
        String updateSql = "UPDATE admins SET username = ?, password_hash = ?, salt = ? WHERE id = ?";
        
        // Retry mechanism for database locks
        int maxRetries = 3;
        int retryDelay = 100; // milliseconds
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.setAutoCommit(false);
                
                // Check if new username already exists (if username is being changed)
                if (newUsername != null && !newUsername.trim().isEmpty()) {
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql)) {
                        checkStmt.setString(1, newUsername.trim());
                        checkStmt.setInt(2, adminId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.getInt(1) > 0) {
                            conn.rollback();
                            return false; // Username already exists
                        }
                    }
                }
                
                // Use provided salt and hash password
                String newPasswordHash = SecurityUtil.hashPassword(newPassword, newSalt);
                
                // Update credentials
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newUsername != null ? newUsername.trim() : getCurrentUsername(adminId));
                    updateStmt.setString(2, newPasswordHash);
                    updateStmt.setString(3, newSalt);
                    updateStmt.setInt(4, adminId);
                    
                    int rowsUpdated = updateStmt.executeUpdate();
                    
                    if (rowsUpdated > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
            } catch (SQLException e) {
                if (attempt == maxRetries - 1) {
                    System.err.println("Failed to update admin credentials after " + maxRetries + " attempts: " + e.getMessage());
                    return false;
                } else {
                    // Wait before retry
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Get current username for admin ID
     */
    private String getCurrentUsername(int adminId) {
        String sql = "SELECT username FROM admins WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("Failed to get current username: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all files from the database
     */
    public List<VaultFile> getAllFiles() {
        List<VaultFile> files = new ArrayList<>();
        String sql = "SELECT * FROM vault_files ORDER BY date_added DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                VaultFile file = new VaultFile();
                file.setId(rs.getLong("id"));
                file.setOriginalName(rs.getString("original_name"));
                file.setEncryptedPath(rs.getString("encrypted_path"));
                file.setFileSize(rs.getLong("file_size"));
                file.setFileType(rs.getString("file_type"));
                file.setDescription(rs.getString("description"));
                
                // Handle date_added
                String dateAddedStr = rs.getString("date_added");
                if (dateAddedStr != null) {
                    file.setDateAdded(LocalDateTime.parse(dateAddedStr));
                }
                
                files.add(file);
            }
            
        } catch (SQLException e) {
            SecurityConfig.secureLog(java.util.logging.Level.SEVERE, "Database error retrieving files: {0}", e.getMessage());
        }
        
        return files;
    }
    
    /**
     * Get all admins from the database
     */
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admins";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setUsername(rs.getString("username"));
                admin.setPasswordHash(rs.getString("password_hash"));
                admin.setSalt(rs.getString("salt"));
                
                String createdAtStr = rs.getString("created_at");
                if (createdAtStr != null) {
                    admin.setCreatedAt(LocalDateTime.parse(createdAtStr));
                }
                
                String lastLoginStr = rs.getString("last_login");
                if (lastLoginStr != null) {
                    admin.setLastLogin(LocalDateTime.parse(lastLoginStr));
                }
                
                admin.setActive(rs.getBoolean("is_active"));
                
                admins.add(admin);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get all admins: " + e.getMessage());
        }
        
        return admins;
    }
}
