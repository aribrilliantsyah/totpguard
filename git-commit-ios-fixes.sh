#!/bin/bash

# Git commit script untuk perubahan iOS fixes dan documentation
# Run this after verifying all changes are correct

echo "📝 Preparing to commit iOS fixes and documentation updates..."
echo ""

# Show current status
echo "📊 Current git status:"
git status --short
echo ""

# Stage all changes
echo "➕ Staging changes..."
git add -A

echo ""
echo "📝 Changes to be committed:"
git status --short
echo ""

# Commit message
COMMIT_MSG="fix(ios): Remove legacy IosTotpGenerator and add comprehensive docs

- Fixed: Removed IosTotpGenerator.kt that used incompatible kotlin.system.currentTimeMillis
- Fixed: All iOS platform files now use proper expect/actual pattern
- Added: USAGE_EXAMPLES.md with default parameters examples
- Added: IOS_BUILD_FIX.md guide for fixing iOS build errors
- Added: remove-ios-legacy-files.sh cleanup script
- Updated: CHANGELOG.md with all recent fixes
- Updated: README.md with links to new documentation
- Updated: TotpGuardTest.kt with default parameters tests

All iOS files now use iOS-compatible APIs:
- TimeProvider uses Foundation.NSDate
- Base64Provider uses Foundation with proper types
- QrCodeProvider uses CoreImage/CoreGraphics
- CryptoProvider uses cryptography-kotlin

Library is now ready for iOS development on macOS."

# Show commit message
echo "📄 Commit message:"
echo "---"
echo "$COMMIT_MSG"
echo "---"
echo ""

# Ask for confirmation
read -p "🤔 Do you want to commit these changes? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "✅ Committing..."
    git commit -m "$COMMIT_MSG"
    
    echo ""
    echo "✅ Commit successful!"
    echo ""
    echo "📤 To push to remote, run:"
    echo "   git push origin main"
    echo ""
    echo "🔧 Next steps on macOS:"
    echo "   1. Pull latest changes: git pull"
    echo "   2. Run cleanup script: ./remove-ios-legacy-files.sh"
    echo "   3. Build: ./gradlew clean :library:build"
else
    echo "❌ Commit cancelled"
    echo "💡 To unstage changes, run: git reset HEAD"
fi
