#!/bin/bash

# Script untuk menghapus file legacy iOS yang tidak kompatibel
# Jalankan script ini di root directory project Anda

echo "🔍 Mencari file IosTotpGenerator.kt yang legacy..."

# Cari dan hapus file IosTotpGenerator.kt
LEGACY_FILE="library/src/iosMain/kotlin/io/github/aribrilliantsyah/totpguard/platform/IosTotpGenerator.kt"

if [ -f "$LEGACY_FILE" ]; then
    echo "✅ Found: $LEGACY_FILE"
    echo "🗑️  Removing..."
    rm -f "$LEGACY_FILE"
    echo "✅ Successfully removed IosTotpGenerator.kt"
else
    echo "ℹ️  File IosTotpGenerator.kt not found (already removed or doesn't exist)"
fi

# Verifikasi bahwa file-file yang benar ada
echo ""
echo "📋 Verifying iOS platform files..."
IOS_PLATFORM_DIR="library/src/iosMain/kotlin/io/github/aribrilliantsyah/totpguard/platform"

if [ -d "$IOS_PLATFORM_DIR" ]; then
    echo "✅ iOS platform directory exists"
    echo ""
    echo "📁 Expected files in iOS platform:"
    
    for file in "Base64Provider.kt" "CryptoProvider.kt" "QrCodeProvider.kt" "TimeProvider.kt"; do
        if [ -f "$IOS_PLATFORM_DIR/$file" ]; then
            echo "  ✅ $file"
        else
            echo "  ❌ $file (MISSING - This is required!)"
        fi
    done
    
    echo ""
    echo "📁 Current files in iOS platform directory:"
    ls -1 "$IOS_PLATFORM_DIR" | sed 's/^/  /'
    
    # Check for any unexpected files
    echo ""
    UNEXPECTED=$(ls -1 "$IOS_PLATFORM_DIR" | grep -v -E '^(Base64Provider|CryptoProvider|QrCodeProvider|TimeProvider)\.kt$')
    if [ -n "$UNEXPECTED" ]; then
        echo "⚠️  Unexpected files found (you may want to review these):"
        echo "$UNEXPECTED" | sed 's/^/  /'
    else
        echo "✅ No unexpected files found"
    fi
else
    echo "❌ iOS platform directory not found!"
fi

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "🔧 Next steps:"
echo "   1. Run: ./gradlew clean"
echo "   2. Run: ./gradlew :library:build"
echo "   3. If building on macOS, iOS targets should compile successfully"
