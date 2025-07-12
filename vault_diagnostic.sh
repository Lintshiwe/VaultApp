#!/bin/bash
# Diagnostic script to check vault state

echo "=== VAULT DIAGNOSTIC REPORT ==="
echo "Date: $(date)"
echo ""

echo "=== DATABASE INFORMATION ==="
if [ -f "vault.db" ]; then
    echo "Database file exists: $(ls -lh vault.db)"
    echo ""
    
    echo "=== ADMIN TABLE ==="
    sqlite3 vault.db "SELECT id, username, substr(salt,1,20)||'...', created_at, last_login, is_active FROM admins;"
    echo ""
    
    echo "=== VAULT FILES TABLE ==="
    sqlite3 vault.db "SELECT id, original_name, substr(encrypted_path,1,50)||'...', file_type, file_size, date_added FROM vault_files LIMIT 10;"
    echo ""
    
    echo "=== ENCRYPTED FILES STATUS ==="
    for file in $(sqlite3 vault.db "SELECT encrypted_path FROM vault_files;"); do
        if [ -f "$file" ]; then
            size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
            echo "EXISTS: $file ($size bytes)"
        else
            echo "MISSING: $file"
        fi
    done
else
    echo "Database file 'vault.db' does not exist!"
fi

echo ""
echo "=== VAULT DIRECTORY ==="
vault_dir="$HOME/.securevault"
if [ -d "$vault_dir" ]; then
    echo "Vault directory exists: $(ls -lh "$vault_dir")"
    find "$vault_dir" -type f -exec ls -lh {} \;
else
    echo "Vault directory does not exist at $vault_dir"
fi

echo ""
echo "=== RECOMMENDATIONS ==="
echo "1. Check if admin password was changed without re-encrypting files"
echo "2. Try resetting to default admin:admin123 and test decryption"
echo "3. If files are corrupted, consider re-uploading them"
