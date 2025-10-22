#!/bin/bash

# Script untuk membersihkan struktur folder dan menyesuaikan dengan template KMP resmi

echo "=== TotpGuard Library - Tool Pembersihan Struktur ==="
echo "Memulai pembersihan struktur folder..."

# Fungsi untuk memeriksa perbedaan antara src dan library/src
check_differences() {
    if [ -d "src" ] && [ -d "library/src" ]; then
        echo "Memeriksa perbedaan antara src dan library/src..."
        
        # Hitung jumlah file di setiap folder
        src_files=$(find src -type f | wc -l)
        lib_src_files=$(find library/src -type f | wc -l)
        
        echo "- File di src: $src_files"
        echo "- File di library/src: $lib_src_files"
        
        # Periksa apakah ada file unik di src yang tidak ada di library/src
        if diff -rq src library/src | grep -q "Only in src"; then
            echo -e "\n[PERHATIAN] Ada file di folder src yang tidak ada di library/src:"
            diff -rq src library/src | grep "Only in src"
            return 1
        else
            echo "Semua file dari src sudah ada di library/src."
            return 0
        fi
    fi
}

# Fungsi setup_github_properties dihapus karena tidak lagi diperlukan

# Periksa apakah folder src ada di root project
if [ -d "src" ]; then
    echo -e "\n[DITEMUKAN] Folder 'src' ada di root project (tidak sesuai template KMP)"
    
    # Periksa apakah folder library/src ada
    if [ ! -d "library/src" ]; then
        echo "Folder 'library/src' belum ada, membuat folder..."
        mkdir -p library/src
    fi
    
    # Salin semua konten dari src ke library/src
    echo "Menyalin konten dari src ke library/src..."
    rsync -av src/ library/src/
    
    # Periksa perbedaan
    check_differences
    no_differences=$?
    
    if [ $no_differences -eq 0 ]; then
        # Tanya konfirmasi sebelum menghapus folder src
        echo -e "\nSemua konten sudah disalin dengan benar ke library/src."
        read -p "Hapus folder src di root? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Menghapus folder src di root..."
            rm -rf src
            echo "✓ Folder src di root telah dihapus."
        else
            echo "Folder src di root tidak dihapus."
            echo "CATATAN: Anda harus menghapus folder src di root secara manual nanti."
        fi
    else
        echo -e "\n[PERINGATAN] Ada perbedaan antara src dan library/src."
        echo "Folder src di root TIDAK dihapus untuk mencegah kehilangan data."
        echo "Silakan periksa perbedaan dan gabungkan secara manual sebelum menghapus."
    fi
else
    echo "Folder 'src' tidak ditemukan di root project, struktur sudah benar."
fi

# Validasi struktur folder library
if [ -d "library" ]; then
    echo -e "\nValidasi struktur folder library..."
    
    # Periksa folder src di library
    if [ ! -d "library/src" ]; then
        echo "[PERINGATAN] Folder library/src tidak ditemukan!"
    else
        echo "✓ Struktur folder library/src OK."
        
        # Periksa subfolder utama
        for folder in commonMain androidMain iosMain jvmMain; do
            if [ -d "library/src/$folder" ]; then
                echo "✓ Folder library/src/$folder ditemukan."
            else
                echo "[PERINGATAN] Folder library/src/$folder tidak ditemukan!"
            fi
        done
        
        # Hitung jumlah file kotlin di library/src
        kotlin_files=$(find library/src -name "*.kt" | wc -l)
        echo "Menemukan $kotlin_files file Kotlin di library/src"
    fi
else
    echo -e "\n[PERINGATAN] Folder 'library' tidak ditemukan!"
    echo "Struktur proyek tidak sesuai dengan template KMP resmi."
    exit 1
fi

# Periksa konfigurasi build
echo -e "\nMemeriksa file konfigurasi build..."

# Periksa gradle.properties
if [ -f "gradle.properties" ]; then
    echo "✓ File gradle.properties ditemukan."
else
    echo "[PERINGATAN] gradle.properties tidak ditemukan!"
fi

# Periksa settings.gradle.kts
if [ -f "settings.gradle.kts" ]; then
    if grep -q "include(\":library\")" settings.gradle.kts; then
        echo "✓ settings.gradle.kts sudah include library module."
    else
        echo "[PERINGATAN] settings.gradle.kts tidak memiliki include(:library)!"
    fi
else
    echo "[PERINGATAN] settings.gradle.kts tidak ditemukan!"
fi

# Periksa version catalog
if [ -f "gradle/libs.versions.toml" ]; then
    echo "✓ Version catalog ditemukan di gradle/libs.versions.toml"
else
    echo "[PERINGATAN] Version catalog tidak ditemukan di gradle/libs.versions.toml!"
fi

# Pemeriksaan konfigurasi publishing yang sederhana
echo -e "\nMemeriksa konfigurasi publishing..."
if [ -f "library/build.gradle.kts" ]; then
    if grep -q "alias(libs.plugins.vanniktech.publish)" library/build.gradle.kts; then
        echo "✓ library/build.gradle.kts sudah menggunakan plugin publishing yang tepat."
    else
        echo "[INFO] Mungkin perlu menambahkan plugin publishing ke library/build.gradle.kts"
    fi
else
    echo "[PERINGATAN] library/build.gradle.kts tidak ditemukan!"
fi

echo -e "\nValidasi struktur selesai."
echo "======================="
echo "Struktur folder yang direkomendasikan untuk KMP library (sesuai template resmi):"
echo "totp-guard/"
echo "├── gradle/"
echo "│   └── libs.versions.toml"
echo "├── library/"
echo "│   ├── src/"
echo "│   │   ├── commonMain/"
echo "│   │   ├── jvmMain/"
echo "│   │   ├── androidMain/"
echo "│   │   └── iosMain/"
echo "│   ├── build.gradle.kts"
echo "│   └── README.md"
echo "├── build.gradle.kts"
echo "├── settings.gradle.kts"
echo "├── gradle.properties"
echo "└── gradlew"
echo "======================="
echo -e "\nSilakan jalankan './gradlew build' untuk memverifikasi proyek berfungsi dengan baik."