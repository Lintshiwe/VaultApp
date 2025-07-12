@echo off
echo === VAULT DIAGNOSTIC REPORT ===
echo Date: %DATE% %TIME%
echo.

echo === DATABASE INFORMATION ===
if exist "vault.db" (
    echo Database file exists
    dir vault.db
    echo.
    
    echo === CHECKING DATABASE TABLES ===
    echo Admins table:
    sqlite3 vault.db "SELECT 'ID: ' || id || ', Username: ' || username || ', Salt: ' || substr(salt,1,20) || '..., Active: ' || is_active FROM admins;"
    echo.
    
    echo Vault files table:
    sqlite3 vault.db "SELECT 'ID: ' || id || ', Name: ' || original_name || ', Type: ' || file_type || ', Size: ' || file_size FROM vault_files LIMIT 10;"
    echo.
    
    echo === CHECKING ENCRYPTED FILES ===
    for /f "tokens=*" %%i in ('sqlite3 vault.db "SELECT encrypted_path FROM vault_files;"') do (
        if exist "%%i" (
            echo EXISTS: %%i
        ) else (
            echo MISSING: %%i
        )
    )
) else (
    echo Database file 'vault.db' does not exist!
)

echo.
echo === VAULT DIRECTORY ===
set "vault_dir=%USERPROFILE%\.securevault"
if exist "%vault_dir%" (
    echo Vault directory exists at %vault_dir%
    dir "%vault_dir%" /s
) else (
    echo Vault directory does not exist at %vault_dir%
)

echo.
echo === RECOMMENDATIONS ===
echo 1. Check if admin password was changed without re-encrypting files
echo 2. Try logging in with default credentials admin:admin123
echo 3. If decryption still fails, the file may have been corrupted
echo 4. Consider re-uploading files if they cannot be recovered
