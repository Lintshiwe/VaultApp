# Version 1.0.1 Update Summary

## Overview

This update adds folder upload support, file opening capabilities, credential management, copyright protection, fixes database locking issues, and resolves all Maven build warnings in the Secure Vault Application.

## New Features Added

### 1. Folder Upload Support

**UI Changes:**

- Button text updated from "Add File" to "Add File/Folder"
- New selection dialog with options: "Select File", "Select Folder", or "Cancel"

**Functionality:**

- Recursive folder scanning to find all files in selected folder and subfolders
- Batch upload processing with progress tracking
- Automatic file naming based on folder structure
- Space validation before each file upload
- Cancellable operation with user-friendly progress dialog

**Implementation Details:**

- Added `selectFolder()` method in `MainWindow.java`
- Added `collectFilesRecursively()` method for folder scanning
- Added `addFolderToVault()` method for batch processing
- Created `FolderUploadWorker` inner class extending SwingWorker
- Added overloaded `canStoreFile(File file)` method in `VaultService.java`

### 2. Database Locking Fix

**Root Cause:**

- SQLite database locking issues when multiple operations occurred quickly
- Default SQLite configuration causing "SQLITE_BUSY" errors

**Solution:**

- Updated database URL to include WAL mode and busy timeout:
  `jdbc:sqlite:vault.db?journal_mode=WAL&busy_timeout=30000`
- Implemented retry mechanism with exponential backoff for `updateLastLogin()`
- Added proper connection handling with auto-commit

**Technical Changes:**

- Modified `DatabaseManager.DB_URL` constant
- Enhanced `updateLastLogin()` method with retry logic
- Added thread safety and timeout handling

### 3. Maven Build Warnings Fix

**Warnings Resolved:**

- **JDK 17 Module System**: Updated compiler plugin to use `--release 17` instead of separate source/target
- **Module-info Conflicts**: Excluded module-info.class files from shaded JAR to prevent conflicts
- **Dependency Overlaps**: Resolved overlapping classes between bcprov and sqlite-jdbc dependencies
- **Clean Build**: Eliminated all Maven compiler and shade plugin warnings

**Technical Changes:**

- Updated `maven-compiler-plugin` configuration to use `<release>17</release>`
- Enhanced `maven-shade-plugin` filters to exclude module-info files
- Added `createDependencyReducedPom` setting to reduce build artifacts
- Improved JAR packaging for better compatibility

### 4. Credential Management System

**UI Changes:**

- Added "Settings" button to the main toolbar
- Created comprehensive credential change dialog
- Window title now displays current username

**Functionality:**

- Admin can change username and password from within the application
- Current password verification for security
- Username uniqueness validation
- Password strength requirements (minimum 6 characters)
- Option to change only username or only password
- Real-time validation and user feedback

**Implementation Details:**

- Added `ChangeCredentialsDialog.java` for user interface
- Enhanced `DatabaseManager.java` with `updateAdminCredentials()` method
- Added username availability checking
- Secure password verification before changes
- Updated MainWindow with Settings button and dialog integration

### 5. File Opening/Playing System

**UI Changes:**

- Added "Open/Play" button to the main toolbar
- Integrated with system default applications for file types

**Functionality:**

- Direct file opening from vault without permanent extraction
- Temporary decryption to secure temp directory
- Automatic file cleanup when application closes
- Support for videos, images, documents, and all file types
- System default application integration (video players, image viewers, etc.)

**Implementation Details:**

- Enhanced `MainWindow.java` with `openSelectedFile()` method
- Secure temporary file handling with automatic cleanup
- Java Desktop API integration for system application launching
- File extension preservation for proper application association

### 6. Copyright Protection System

**Legal Framework:**

- Added comprehensive LICENSE file with additional restrictions
- Copyright headers in all source files
- Trademark protection for application name
- Built-in copyright validation system

**Protection Features:**

- Copyright integrity checking on application startup
- About dialog with detailed licensing information
- Tamper detection and violation warnings
- Contact information for licensing inquiries

**Implementation Details:**

- Added `AboutDialog.java` for copyright and license display
- Enhanced `VaultApplication.java` with copyright validation
- License violation detection and error handling
- Full legal notice integration

## Enhanced User Experience

**Progress Tracking:**

- Visual progress bars for folder uploads
- Real-time file count and processing status
- Cancellation support during upload
- Success/failure summary at completion

**Error Handling:**

- Graceful handling of file access errors
- Space validation before each file upload
- Detailed error messages for troubleshooting
- Automatic retry for transient database issues

## Files Modified

### Core Application Files

1. **MainWindow.java**

   - Added folder selection functionality
   - Implemented progress dialog with SwingWorker
   - Updated UI button text and tooltips
   - Added file opening functionality

2. **VaultService.java**

   - Added `canStoreFile(File file)` overload method

3. **DatabaseManager.java**

   - Updated database URL with WAL mode and timeout
   - Enhanced `updateLastLogin()` with retry mechanism

4. **pom.xml**

   - Updated compiler plugin to use `--release 17`
   - Enhanced shade plugin to exclude module-info conflicts
   - Resolved all Maven build warnings

5. **ChangeCredentialsDialog.java** (New)

   - Complete credential management interface
   - Password verification and validation
   - User-friendly error handling and feedback

6. **ChangeCredentialsDialog.java**

   - Added credential change dialog implementation

7. **Settings.java**

   - Added settings management for username and password

8. **AboutDialog.java** (New)

   - Copyright and license information display
   - Tamper detection and violation warnings

### Documentation

1. **README.md**
   - Added folder upload instructions
   - Updated feature list
   - Added troubleshooting for database issues
   - Included credential management usage
   - Added copyright protection information

## Testing Results

✅ **Compilation**: Successful with no errors
✅ **Database Issues**: Fixed SQLITE_BUSY errors  
✅ **Folder Upload**: Successfully tested with multiple files
✅ **Progress Tracking**: Working correctly with cancellation support
✅ **Space Management**: Pre-upload validation working
✅ **Error Handling**: Graceful failure recovery
✅ **Credential Management**: Username and password changes working
✅ **File Opening**: Files opening correctly in default applications
✅ **Copyright Protection**: Validation and about dialog working

## Usage Instructions

### Uploading Folders

1. Click "Add File/Folder" button
2. Select "Select Folder" option
3. Choose any folder from your system
4. Review file count in confirmation dialog
5. Click "Yes" to proceed with upload
6. Monitor progress bar (cancel if needed)
7. Review completion summary

### Changing Credentials

1. Click "Settings" button in the top-right toolbar
2. Enter your current password for verification
3. Enter new username (optional, must be at least 3 characters)
4. Enter new password (optional, must be at least 6 characters)
5. Confirm new password if changing password
6. Click "Save Changes"
7. Window title will update to show new username

### Opening Files

1. Select a file from the vault list
2. Click "Open/Play" button in the toolbar
3. File will be temporarily decrypted and opened
4. System default application will launch the file
5. Use and enjoy the file (watch video, view image, etc.)
6. Temporary file will be cleaned up automatically

### Expected Behavior

- All files in folder and subfolders will be uploaded
- Files maintain their relative path structure in descriptions
- Progress bar shows current file being processed
- Upload can be cancelled at any time
- Space validation prevents problematic uploads
- Database operations are now retry-safe
- Username and password can be changed within the app
- Real-time validation ensures credential security
- Files can be opened directly from the vault
- Temporary files are securely handled and deleted

## Technical Notes

- Uses SwingWorker for background processing
- Implements proper thread safety for UI updates
- SQLite WAL mode improves concurrent access
- Exponential backoff prevents database contention
- File validation ensures upload reliability
- Secure password handling for credential management
- Java Desktop API used for opening files with system applications

This update significantly enhances the vault application's usability and reliability, making it suitable for managing large collections of files and folders.
