# Secure Vault Application

A desktop application for securely storing and managing files with encryption and admin authentication.

## Features

- **Secure File Storage**: Files are encrypted using AES-256 encryption before being stored
- **Admin Authentication**: Password-protected access with secure password hashing
- **File Management**: Add, retrieve, search, and delete files from the vault
- **Modern UI**: Clean, dark-themed interface built with Java Swing and FlatLaf
- **File Search**: Search files by name, description, or tags
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
- At least 100MB of free disk space

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

```bash
java -jar target/VaultApp-1.0.0.jar
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

### 2. Adding Files

- Click "Add File" button
- Select a file from your computer
- Enter a description and tags (optional)
- Click "Add to Vault"

### 3. Retrieving Files

- Select a file from the list
- Click "Retrieve" or double-click the file
- Choose where to save the decrypted file
- The original file will be restored

### 4. Searching Files

- Use the search box to find files by name, description, or tags
- Click "Search" or press Enter
- Click "Clear" to show all files again

### 5. Deleting Files

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

```
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

### Getting Help

If you encounter issues:

1. Check the console output for detailed error messages
2. Ensure all system requirements are met
3. Try running with Java debugging enabled: `java -Xdebug -jar VaultApp-1.0.0.jar`

## Security Disclaimer

This application is designed for educational purposes and personal use. While it implements industry-standard encryption, it has not undergone professional security auditing. For sensitive or critical data, consider using enterprise-grade security solutions.
