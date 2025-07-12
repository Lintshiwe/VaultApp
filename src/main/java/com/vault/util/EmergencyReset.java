package com.vault.util;

import java.sql.*;

/**
 * Emergency utility to reset admin credentials to default values
 * Use this when files cannot be decrypted due to credential mismatch
 */
public class EmergencyReset {
    
    private static final String DB_URL = "jdbc:sqlite:vault.db";
    
    public static void main(String[] args) {
        System.out.println("=== EMERGENCY VAULT RESET ===");
        System.out.println("This will reset admin credentials to default (admin/admin123)");
        System.out.println("WARNING: Only use this if you're locked out of your vault!");
        
        try {
            // Connect to database
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                System.out.println("Connected to database successfully");
                
                // Check current admin
                String selectSql = "SELECT id, username, salt FROM admins WHERE is_active = 1 LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(selectSql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    if (rs.next()) {
                        int adminId = rs.getInt("id");
                        String currentUsername = rs.getString("username");
                        String currentSalt = rs.getString("salt");
                        
                        System.out.println("Current admin: " + currentUsername);
                        System.out.println("Current salt: " + currentSalt.substring(0, 20) + "...");
                        
                        // Generate default credentials
                        String defaultSalt = SecurityUtil.generateSalt();
                        String defaultPasswordHash = SecurityUtil.hashPassword("admin123", defaultSalt);
                        
                        // Update admin to default credentials
                        String updateSql = "UPDATE admins SET username = ?, password_hash = ?, salt = ? WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, "admin");
                            updateStmt.setString(2, defaultPasswordHash);
                            updateStmt.setString(3, defaultSalt);
                            updateStmt.setInt(4, adminId);
                            
                            int updated = updateStmt.executeUpdate();
                            if (updated > 0) {
                                System.out.println("Successfully reset admin credentials to:");
                                System.out.println("  Username: admin");
                                System.out.println("  Password: admin123");
                                System.out.println("  New salt: " + defaultSalt.substring(0, 20) + "...");
                                System.out.println("");
                                System.out.println("IMPORTANT: Your encrypted files may not be recoverable");
                                System.out.println("if they were encrypted with different credentials!");
                                System.out.println("Try logging in and opening a file to test.");
                            } else {
                                System.out.println("Failed to update admin credentials");
                            }
                        }
                    } else {
                        System.out.println("No active admin found in database");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Reset failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
