package com.vault.model;

import java.time.LocalDateTime;

/**
 * Model class representing a stored file in the vault
 */
public class VaultFile {
    private int id;
    private String originalName;
    private String encryptedPath;
    private String fileType;
    private long fileSize;
    private LocalDateTime dateAdded;
    private String description;
    private String tags;
    
    // Constructors
    public VaultFile() {}
    
    public VaultFile(String originalName, String encryptedPath, String fileType, 
                    long fileSize, String description, String tags) {
        this.originalName = originalName;
        this.encryptedPath = encryptedPath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.dateAdded = LocalDateTime.now();
        this.description = description;
        this.tags = tags;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    
    public String getEncryptedPath() { return encryptedPath; }
    public void setEncryptedPath(String encryptedPath) { this.encryptedPath = encryptedPath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    @Override
    public String toString() {
        return originalName + " (" + fileType + ")";
    }
    
    /**
     * Get formatted file size
     */
    public String getFormattedSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }
}
