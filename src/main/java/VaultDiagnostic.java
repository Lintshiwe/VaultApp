import java.sql.*;
import java.io.File;

public class VaultDiagnostic {
    private static final String DB_URL = "jdbc:sqlite:vault.db";
    
    public static void main(String[] args) {
        System.out.println("=== VAULT DIAGNOSTIC REPORT ===");
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        
        // Check if database file exists
        File dbFile = new File("vault.db");
        System.out.println("Database file exists: " + dbFile.exists());
        if (dbFile.exists()) {
            System.out.println("Database file size: " + dbFile.length() + " bytes");
        }
        
        try {
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                System.out.println("Database connection successful!");
                
                // Check tables
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, null, new String[]{"TABLE"});
                System.out.println("\n=== TABLES ===");
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("Table: " + tableName);
                }
                
                // Check admins
                System.out.println("\n=== ADMINS ===");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, username, substr(salt,1,20) as salt_preview, is_active FROM admins")) {
                    
                    while (rs.next()) {
                        System.out.printf("ID: %d, Username: %s, Salt: %s..., Active: %b%n", 
                            rs.getInt("id"), rs.getString("username"), 
                            rs.getString("salt_preview"), rs.getBoolean("is_active"));
                    }
                }
                
                // Check vault files
                System.out.println("\n=== VAULT FILES ===");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, original_name, file_type, file_size, encrypted_path FROM vault_files LIMIT 5")) {
                    
                    while (rs.next()) {
                        String encPath = rs.getString("encrypted_path");
                        File encFile = new File(encPath);
                        System.out.printf("ID: %d, Name: %s, Type: %s, Size: %d, Encrypted: %s, Exists: %b%n", 
                            rs.getInt("id"), rs.getString("original_name"), 
                            rs.getString("file_type"), rs.getLong("file_size"), 
                            encPath, encFile.exists());
                        
                        if (encFile.exists()) {
                            System.out.println("  Actual file size: " + encFile.length() + " bytes");
                        }
                    }
                }
                
                // Count total files
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM vault_files")) {
                    if (rs.next()) {
                        System.out.println("\nTotal files in vault: " + rs.getInt("count"));
                    }
                }
                
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Check vault directory
        String userHome = System.getProperty("user.home");
        File vaultDir = new File(userHome, ".securevault");
        System.out.println("\n=== VAULT DIRECTORY ===");
        System.out.println("Vault directory: " + vaultDir.getAbsolutePath());
        System.out.println("Vault directory exists: " + vaultDir.exists());
        
        if (vaultDir.exists()) {
            File filesDir = new File(vaultDir, "files");
            if (filesDir.exists()) {
                File[] files = filesDir.listFiles();
                System.out.println("Files in vault directory: " + (files != null ? files.length : 0));
                if (files != null) {
                    for (File f : files) {
                        System.out.println("  " + f.getName() + " (" + f.length() + " bytes)");
                    }
                }
            }
        }
    }
}
