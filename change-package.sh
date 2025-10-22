#!/bin/bash

# Script untuk mengubah nama package dari com.dak.totpguard menjadi io.github.aribrilliantsyah.totpguard
# Perhatian: Script ini akan melakukan perubahan besar pada kode sumber Anda

echo "=== TotpGuard Library - Perubahan Nama Package ==="
echo "Mengubah package dari com.dak.totpguard menjadi io.github.aribrilliantsyah.totpguard"

# Set variabel
OLD_PACKAGE="com.dak.totpguard"
NEW_PACKAGE="io.github.aribrilliantsyah.totpguard"
OLD_PATH="com/dak/totpguard"
NEW_PATH="io/github/aribrilliantsyah/totpguard"
SRC_DIR="library/src"

# Fungsi untuk membuat struktur folder baru
create_new_folders() {
    echo -e "\n1. Membuat struktur folder baru..."
    
    # Periksa semua platform (commonMain, androidMain, iosMain, jvmMain, jvmTest)
    for platform in commonMain androidMain iosMain jvmMain jvmTest; do
        if [ -d "$SRC_DIR/$platform/kotlin/$OLD_PATH" ]; then
            echo "  - Membuat folder untuk $platform"
            mkdir -p "$SRC_DIR/$platform/kotlin/$NEW_PATH"
        fi
    done
    
    echo "✓ Struktur folder baru telah dibuat"
}

# Fungsi untuk menyalin file-file ke struktur folder baru
copy_files() {
    echo -e "\n2. Menyalin file-file ke struktur folder baru..."
    
    # Periksa semua platform dan salin file
    for platform in commonMain androidMain iosMain jvmMain jvmTest; do
        if [ -d "$SRC_DIR/$platform/kotlin/$OLD_PATH" ]; then
            echo "  - Menyalin file-file dari $platform"
            cp -r "$SRC_DIR/$platform/kotlin/$OLD_PATH"/* "$SRC_DIR/$platform/kotlin/$NEW_PATH/"
            
            # Hitung jumlah file yang disalin
            file_count=$(find "$SRC_DIR/$platform/kotlin/$NEW_PATH" -name "*.kt" | wc -l)
            echo "    $file_count file telah disalin untuk $platform"
        fi
    done
    
    echo "✓ Semua file telah disalin ke struktur folder baru"
}

# Fungsi untuk memperbarui deklarasi package dalam semua file
update_package_declarations() {
    echo -e "\n3. Memperbarui deklarasi package dalam semua file..."
    
    # Hitung total file
    total_files=$(find "$SRC_DIR" -path "*/$NEW_PATH/*.kt" -type f | wc -l)
    current=0
    
    # Perbarui file-file dalam folder baru
    find "$SRC_DIR" -path "*/$NEW_PATH/*.kt" -type f | while read -r file; do
        current=$((current+1))
        echo -ne "  - Memproses file $current/$total_files: $(basename "$file")                        \r"
        
        # Ganti deklarasi package
        sed -i "s/package $OLD_PACKAGE/package $NEW_PACKAGE/g" "$file"
        
        # Ganti import statements
        sed -i "s/import $OLD_PACKAGE/import $NEW_PACKAGE/g" "$file"
    done
    
    echo -e "\n✓ Semua deklarasi package telah diperbarui"
}

# Fungsi utama
main() {
    echo -e "\nPerubahan ini akan mengubah struktur folder dan deklarasi package."
    read -p "Apakah Anda yakin ingin melanjutkan? (y/n) " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Operasi dibatalkan."
        exit 1
    fi
    
    # Langkah 1: Membuat struktur folder baru
    create_new_folders
    
    # Langkah 2: Menyalin file-file
    copy_files
    
    # Langkah 3: Memperbarui deklarasi package
    update_package_declarations
    
    echo -e "\n=== Perubahan nama package selesai ==="
    echo "Langkah berikutnya:"
    echo "1. Jalankan './gradlew build' untuk memverifikasi bahwa semua perubahan berhasil"
    echo "2. Jika build berhasil, hapus folder lama dengan menjalankan:"
    echo "   find $SRC_DIR -path \"*/$OLD_PATH\" -type d -exec rm -rf {} \\; 2>/dev/null || true"
    echo "3. Periksa dan perbarui referensi package lain jika diperlukan"
}

# Jalankan fungsi utama
main