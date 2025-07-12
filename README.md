# Secure Vault Application

A desktop application for securely storing and managing files with encryption and admin authentication.

## Recent Updates

### Version 1.0.1 Features

- **Folder Upload Support**: Upload entire folders with recursive file scanning
- **Database Locking Fix**: Improved SQLite connection handling with WAL mode and timeout
- **Progress Tracking**: Visual progress bars for large folder uploads with cancellation support
- **Enhanced Space Management**: Pre-upload validation for both files and folders
- **Better Error Handling**: Robust retry mechanisms for database operations
- **Credential Change**: Admin can change username and password from Settings button

## Features

- **Secure File Storage**: Files are encrypted using AES-256 encryption before being stored
- **Admin Authentication**: Password-protected access with secure password hashing
- **Credential Management**: Change username and password from within the application
- **File Management**: Add, retrieve, search, and delete files from the vault
- **File Opening**: Open and play files directly from the vault (videos, images, documents)
- **Folder Upload**: Upload entire folders with all contained files recursively
- **Modern UI**: Clean, dark-themed interface built with Java Swing and FlatLaf
- **File Search**: Search files by name, description, or tags
- **Dynamic Space Management**: Real-time disk space monitoring and validation
- **Progress Tracking**: Visual progress bars for large folder uploads
- **Cross-platform**: Runs on Windows, macOS, and Linux

## Security Features

- **AES-256 Encryption**: All files are encrypted using industry-standard AES encryption
- **Password-Based Key Derivation**: Encryption keys are derived from user passwords using PBKDF2
- **Secure Password Storage**: Admin passwords are hashed with SHA-256 and salted
- **Secure File Names**: Original file names are replaced with cryptographically secure random names

## Default Login Credentials

- **Username**: `admin`
- **Password**: `admin123`

_Note: Please change the default password after first login for security._

## System Requirements

- Java 17 or higher
- Windows, macOS, or Linux
- Dynamic disk space allocation based on vault contents:
  - **Minimum**: Current vault size + 100MB buffer
  - **Recommended**: Current vault size + 50% expansion buffer
  - **Empty vault**: At least 100MB free space
  - **Note**: Space requirements automatically increase as you add more files

## Installation and Running

### Prerequisites

Make sure you have Java 17 or higher installed:

```bash
java -version
```

### Building from Source

1. Clone or download the project
2. Navigate to the project directory
3. Build using Maven:

```bash
mvn clean package
```

### Running the Application

After building, run the application:

**Command Prompt/Terminal:**

```bash
java -jar target/VaultApp-1.0.0.jar
```

**PowerShell:**

```powershell
Start-Process java -ArgumentList '-jar', 'target\VaultApp-1.0.0.jar'
```

**Windows Batch File:**

```bash
run.bat
```

Or run directly with Maven:

```bash
mvn exec:java -Dexec.mainClass="com.vault.VaultApplication"
```

## How to Use

### 1. Login

- Start the application
- Enter your username and password
- Click "Login" to access the vault

### 2. Adding Files and Folders

You can now add both individual files and entire folders to the vault:

#### Single File Upload

- Click "Add File/Folder" button
- Select "Select File" option
- Choose a file from your computer
- Enter a description and tags (optional)
- Click "Add to Vault"

#### Folder Upload

- Click "Add File/Folder" button
- Select "Select Folder" option
- Choose a folder from your computer
- The system will scan all files in the folder and subfolders
- Confirm the upload when prompted
- Watch the progress bar as files are processed
- You can cancel the operation at any time

**Note**: Large folders may take time to process. The system will show progress and allow cancellation.

### 3. Changing Login Credentials

You can change your username and password from within the application:

- Click the "Settings" button in the top-right toolbar
- Enter your current password for verification
- Enter a new username (must be at least 3 characters)
- Optionally change your password (leave empty to keep current password)
- If changing password, confirm the new password
- Click "Save Changes"

**Security Notes:**

- Your current password is required for verification
- New username must be unique (though only one admin exists by default)
- New password must be at least 6 characters long
- Changes take effect immediately

### 4. Opening/Playing Files

You can open files directly from the vault without permanently saving them:

- Select a file from the vault list
- Click "Open/Play" button in the toolbar
- The file will be temporarily decrypted and opened with your system's default application
- Videos will play in your default video player
- Images will open in your default image viewer
- Documents will open in appropriate applications

**Security Notes:**

- Files are temporarily extracted to a secure temp directory
- Temporary files are automatically deleted when the application closes
- Original encrypted files remain safely in the vault
- For permanent access, use the "Retrieve" button instead

### 5. Retrieving Files

- Select a file from the list
- Click "Retrieve" or double-click the file
- Choose where to save the decrypted file
- The original file will be restored

### 6. Searching Files

- Use the search box to find files by name, description, or tags
- Click "Search" or press Enter
- Click "Clear" to show all files again

### 7. Deleting Files

- Select a file from the list
- Click "Delete"
- Confirm the deletion

## File Storage

- Encrypted files are stored in: `%USERPROFILE%\.securevault\files\` (Windows)
- Database is stored as: `vault.db` in the application directory
- Original file names and metadata are stored securely in the database

## Technical Details

### Architecture

- **Model-View-Controller (MVC)** pattern
- **SQLite** for local database storage
- **Bouncy Castle** for cryptographic operations
- **FlatLaf** for modern UI theming

### Encryption

- **Algorithm**: AES-256 in CBC mode with PKCS5 padding
- **Key Derivation**: PBKDF2 with SHA-256, 10,000 iterations
- **IV**: Random 16-byte initialization vector for each file
- **Salt**: 32-byte random salt for each user

### Security Considerations

- Files are encrypted before being written to disk
- Encryption keys are derived from user passwords and never stored
- Original file names are replaced with secure random names
- Database connections use prepared statements to prevent SQL injection

## Development

### Project Structure

```text
src/main/java/com/vault/
├── VaultApplication.java          # Main application entry point
├── model/                         # Data models
│   ├── Admin.java
│   └── VaultFile.java
├── service/                       # Business logic
│   └── VaultService.java
├── util/                         # Utility classes
│   ├── DatabaseManager.java
│   └── SecurityUtil.java
└── ui/                           # User interface
    ├── LoginWindow.java
    ├── MainWindow.java
    └── AddFileDialog.java
```

### Dependencies

- **Bouncy Castle**: Cryptographic operations
- **Apache Commons IO**: File operations
- **FlatLaf**: Modern look and feel
- **SQLite JDBC**: Database connectivity
- **JUnit**: Testing framework

## License

This project is provided as-is for educational and personal use.

## Copyright and Legal Information

**© 2025 Lintshiwe Ntoampi. All Rights Reserved.**

### Important Legal Notice

This software is protected by copyright law and international treaties. The "Secure Vault Application" name and associated trademarks are the property of Lintshiwe Ntoampi.

### Licensing Terms

- **Personal Use**: Permitted under MIT License with Additional Restrictions
- **Commercial Use**: Requires explicit written permission from the copyright holder
- **Attribution**: Must be maintained in all distributions and derivative works
- **Modification**: Permitted for personal use only; commercial distribution of modifications is prohibited
- **Redistribution**: Source code redistributions must retain all copyright notices

### Restrictions

- Copyright notices may not be removed or altered
- The application name and trademarks may not be used without permission
- Unauthorized commercial use is strictly prohibited
- Violations may result in legal action

### Contact

For licensing inquiries, commercial use permissions, or legal questions:

- Email: <lintshiwe.ntoampi@example.com>
- Subject: "Secure Vault Application Licensing Inquiry"

### Enforcement

This software includes built-in copyright protection mechanisms. Tampering with or circumventing these protections constitutes a violation of the license terms and applicable copyright law.

**By using this software, you acknowledge that you have read, understood, and agree to be bound by these terms.**

## Troubleshooting

### Common Issues

1. **"Failed to initialize database"**

   - Ensure the application has write permissions in its directory
   - Check that SQLite JDBC driver is available

2. **"Failed to create vault directory"**

   - Ensure the application has write permissions to user home directory
   - Check available disk space

3. **"Authentication error"**

   - Verify username and password are correct
   - Try using the default credentials: admin/admin123

4. **"Failed to encrypt/decrypt file"**

   - Ensure the file is not corrupted
   - Check that the correct password was used for login

5. **"Database is locked" or "SQLITE_BUSY" errors**
   - This issue has been fixed in version 1.0.1 with improved connection handling
   - If the error persists, ensure no other instances of the application are running
   - Delete the `vault.db-wal` and `vault.db-shm` files if they exist and restart the application

### Getting Help

If you encounter issues:

1. Check the console output for detailed error messages
2. Ensure all system requirements are met
3. Try running with Java debugging enabled: `java -Xdebug -jar VaultApp-1.0.0.jar`

## Security Disclaimer

This application is designed for educational purposes and personal use. While it implements industry-standard encryption, it has not undergone professional security auditing. For sensitive or critical data, consider using enterprise-grade security solutions.
