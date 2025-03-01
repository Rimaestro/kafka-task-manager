# Aplikasi Desktop Kafka Task Manager

![Java](https://img.shields.io/badge/Java-11-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-blue)
![Kafka](https://img.shields.io/badge/Kafka-3.3.2-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

Aplikasi ini adalah implementasi sistem terdistribusi menggunakan Apache Kafka untuk manajemen task. Sistem terdiri dari dua aplikasi desktop yang saling terhubung melalui Kafka message broker:
1. **Producer**: Aplikasi desktop untuk manajemen task dengan fitur CRUD
2. **Consumer**: Aplikasi desktop untuk memantau aktivitas task secara real-time

## 📸 Screenshot

![Aplikasi Kafka Task Manager](screenshot.png)

Aplikasi ini memiliki tampilan modern dengan:
- Desain material dengan efek bayangan dan sudut melengkung
- Indikator status berwarna untuk memudahkan identifikasi
- Placeholder informatif saat tabel kosong
- Log aktivitas dengan warna berbeda untuk setiap jenis event
- Ikon intuitif pada tombol dan log aktivitas

## ✨ Fitur

### Producer
- 📋 Menampilkan daftar task dalam tabel yang informatif
- ➕ Menambahkan task baru dengan form yang user-friendly
- 📝 Mengedit task yang ada (judul, deskripsi, status)
- 🗑️ Menghapus task dengan konfirmasi
- 📤 Mengirim event ke Kafka untuk setiap operasi CRUD
- 💾 Menyimpan data ke database MySQL

### Consumer
- 📥 Berlangganan ke topik Kafka secara real-time
- 📊 Menampilkan aktivitas task (CREATE, UPDATE, DELETE)
- 🔍 Menampilkan detail task untuk setiap aktivitas
- 📝 Mencatat log aktivitas dengan timestamp

## 🛠️ Teknologi yang Digunakan

- Java 11
- JavaFX 17.0.2 - Framework UI
- Apache Kafka 3.3.2 - Message broker
- MySQL 8.0 - Database
- HikariCP 5.0.1 - Connection pooling
- Docker & Docker Compose - Kontainerisasi
- Maven - Build dan manajemen dependensi
- SLF4J 1.7.36 - Logging

## 📋 Prasyarat

- Java Development Kit (JDK) 11 atau lebih tinggi
- Maven 3.6 atau lebih tinggi
- Docker Desktop dan Docker Compose
- Minimal 4GB RAM untuk menjalankan semua komponen

## 🚀 Cara Menjalankan

### 1. Clone Repository

```bash
git clone https://github.com/Rimaestro/kafka-task-manager.git
cd kafka-task-manager
```

### 2. Menjalankan Kafka dan MySQL dengan Docker

```bash
# Masuk ke direktori docker
cd docker

# Jalankan Kafka, Zookeeper, dan MySQL
docker-compose up -d

# Verifikasi container berjalan
docker ps
```

### 3. Build Aplikasi dengan Maven

```bash
# Kembali ke root direktori
cd ..

# Build aplikasi
mvn clean package
```

### 4. Menjalankan Aplikasi

#### Producer

```bash
# Menggunakan Maven
mvn javafx:run@producer

# ATAU menggunakan JAR
java -jar target/producer-app.jar
```

#### Consumer

```bash
# Menggunakan Maven
mvn javafx:run@consumer

# ATAU menggunakan JAR
java -jar target/consumer-app.jar
```

## 🗄️ Struktur Database

### Tabel `tasks`

```sql
CREATE TABLE tasks (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('TODO', 'IN_PROGRESS', 'DONE') NOT NULL DEFAULT 'TODO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 📁 Struktur Proyek

```
kafka-desktop-app/
├── docker/                      # Konfigurasi Docker
│   ├── docker-compose.yml       # Konfigurasi services
│   └── mysql-init/              # Script inisialisasi MySQL
├── src/
│   ├── main/
│   │   ├── java/com/kafkaapp/
│   │   │   ├── producer/        # Aplikasi Producer
│   │   │   ├── consumer/        # Aplikasi Consumer
│   │   │   └── common/          # Shared code
│   │   └── resources/
│   │       ├── styles/          # CSS files
│   │       └── application.properties
└── pom.xml                      # Maven configuration
```

## 🔄 Alur Kerja Sistem

1. **Producer**:
   - Menyimpan data task ke database MySQL
   - Mengirim event (CREATE/UPDATE/DELETE) ke Kafka
   - Memperbarui tampilan UI

2. **Consumer**:
   - Berlangganan ke topik Kafka
   - Menerima event secara real-time
   - Menampilkan aktivitas di UI
   - Mencatat log aktivitas

## ⚠️ Troubleshooting

### Koneksi Kafka/MySQL Gagal

1. Periksa status container:
```bash
docker ps
docker logs kafka
docker logs mysql
```

2. Verifikasi port tidak digunakan:
```bash
netstat -an | findstr "9092"  # Windows
netstat -an | grep "9092"     # Linux/Mac
```

### Database Error

1. Periksa kredensial di `application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/taskdb
db.username=root
db.password=password
```

2. Reset data (jika diperlukan):
```bash
docker-compose down -v
docker-compose up -d
```

## 🤝 Kontribusi

Kontribusi selalu diterima! Silakan:

1. Fork repositori
2. Buat branch fitur (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## 📝 Lisensi

Didistribusikan di bawah Lisensi MIT. Lihat `LICENSE` untuk informasi lebih lanjut.

## 📧 Kontak

Rimaestro - [GitHub](https://github.com/Rimaestro) - aku.mayesta@gmail.com

Project Link: [https://github.com/Rimaestro/kafka-task-manager](https://github.com/Rimaestro/kafka-task-manager) 