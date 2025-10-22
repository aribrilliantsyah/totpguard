#!/bin/bash

# Skrip untuk membantu proses development TOTP-GUARD

function print_help() {
  echo "TOTP-GUARD Development Helper"
  echo "=============================="
  echo ""
  echo "Penggunaan: ./dev.sh [perintah]"
  echo ""
  echo "Perintah yang tersedia:"
  echo "  build              - Build library"
  echo "  publish-local      - Publish ke Maven local (~/.m2)"
  echo "  publish-dev        - Publish ke local dev-repo dengan timestamp"
  echo "  publish-github     - Publish ke GitHub Packages"
  echo "  sync-source        - Salin source code dari src ke library/src"
  echo "  clean              - Clean build files"
  echo "  help               - Tampilkan bantuan ini"
  echo ""
}

function build() {
  ./gradlew :library:build
}

function publish_local() {
  ./gradlew :library:publishToMavenLocal
  echo "Library dipublikasikan ke Maven local (~/.m2/repository)"
}

function publish_dev() {
  ./gradlew devVersion :library:publishAllPublicationsToDevRepoRepository
  echo "Library dipublikasikan ke ./dev-repo dengan timestamp version"
}

function publish_github() {
  if [ ! -f "github.properties" ]; then
    echo "Error: github.properties tidak ditemukan."
    echo "Membuat github.properties dari template..."
    cp github.properties.template github.properties
    echo "Silakan edit github.properties dan masukkan token GitHub Anda, lalu jalankan perintah ini lagi."
    echo "Token dapat dibuat di https://github.com/settings/tokens"
    echo "Pilih scope: read:packages, write:packages, dan delete:packages"
    exit 1
  fi
  
  # Periksa apakah token sudah diisi
  TOKEN=$(grep "github.token" github.properties | cut -d'=' -f2)
  if [ "$TOKEN" == "your-personal-access-token" ]; then
    echo "Error: Token GitHub belum diperbarui di github.properties"
    echo "Silakan edit github.properties dan masukkan token GitHub Anda yang valid."
    exit 1
  fi
  
  ./gradlew :library:publishAllPublicationsToGitHubPackagesRepository
  echo "Library dipublikasikan ke GitHub Packages dengan akun aribrilliantsyah"
  echo "Untuk menggunakan library dari GitHub Packages di proyek lain, tambahkan konfigurasi ini:"
  echo ""
  echo "// settings.gradle.kts"
  echo "repositories {"
  echo "    maven {"
  echo "        name = \"GitHubPackages\""
  echo "        url = uri(\"https://maven.pkg.github.com/aribrilliantsyah/totpguard\")"
  echo "        credentials {"
  echo "            username = \"<github_username>\""
  echo "            password = \"<github_token>\""
  echo "        }"
  echo "    }"
  echo "}"
}

function sync_source() {
  echo "Menyinkronkan source code dari src ke library/src..."
  rsync -av --progress src/ library/src/ --exclude=".DS_Store"
  echo "Sinkronisasi selesai"
}

function clean() {
  ./gradlew clean
  echo "Build files dibersihkan"
}

if [ "$1" == "build" ]; then
  build
elif [ "$1" == "publish-local" ]; then
  publish_local
elif [ "$1" == "publish-dev" ]; then
  publish_dev
elif [ "$1" == "publish-github" ]; then
  publish_github
elif [ "$1" == "sync-source" ]; then
  sync_source
elif [ "$1" == "clean" ]; then
  clean
else
  print_help
fi