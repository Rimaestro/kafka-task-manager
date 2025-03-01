package com.kafkaapp.common.repository;

import com.kafkaapp.common.config.DatabaseConfig;
import com.kafkaapp.common.model.Task;
import com.kafkaapp.common.model.Task.TaskStatus;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementasi MySQL dari TaskRepository
 */
public class MySqlTaskRepository implements TaskRepository {
    private static final Logger LOGGER = Logger.getLogger(MySqlTaskRepository.class.getName());
    private static MySqlTaskRepository instance;
    private final HikariDataSource dataSource;

    private MySqlTaskRepository() {
        this.dataSource = DatabaseConfig.getDataSource();
        initializeTable();
    }

    /**
     * Mendapatkan instance singleton dari repository
     * 
     * @return instance repository
     */
    public static synchronized MySqlTaskRepository getInstance() {
        if (instance == null) {
            instance = new MySqlTaskRepository();
        }
        return instance;
    }

    /**
     * Memastikan tabel tasks ada di database
     */
    private void initializeTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                + "id VARCHAR(36) PRIMARY KEY,"
                + "title VARCHAR(255) NOT NULL,"
                + "description TEXT,"
                + "status VARCHAR(20) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ")";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            LOGGER.info("Tabel tasks berhasil diinisialisasi");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal menginisialisasi tabel tasks", e);
        }
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mengambil daftar tasks", e);
        }

        return tasks;
    }

    @Override
    public Optional<Task> findById(String id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Task task = mapResultSetToTask(rs);
                    return Optional.of(task);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mencari task dengan id: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isEmpty() || !existsById(task.getId())) {
            return insert(task);
        } else {
            return update(task);
        }
    }

    private Task insert(Task task) {
        String sql = "INSERT INTO tasks (id, title, description, status) VALUES (?, ?, ?, ?)";
        
        // Generate new ID jika belum ada
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(UUID.randomUUID().toString());
        }
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, task.getId());
                stmt.setString(2, task.getTitle());
                stmt.setString(3, task.getDescription());
                stmt.setString(4, task.getStatus().toString());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Gagal membuat task, tidak ada baris yang terpengaruh.");
                }
                
                conn.commit(); // Commit transaksi
                LOGGER.info("Task berhasil disimpan dengan id: " + task.getId());
                return task;
            }
        } catch (SQLException e) {
            // Rollback transaksi jika terjadi error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Gagal rollback transaksi", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Gagal menyimpan task", e);
            throw new RuntimeException("Gagal menyimpan task: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset autocommit
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Gagal menutup koneksi", e);
                }
            }
        }
    }

    private Task update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, task.getTitle());
                stmt.setString(2, task.getDescription());
                stmt.setString(3, task.getStatus().toString());
                stmt.setString(4, task.getId());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Gagal memperbarui task, task dengan id: " + task.getId() + " tidak ditemukan.");
                }
                
                conn.commit(); // Commit transaksi
                LOGGER.info("Task berhasil diperbarui dengan id: " + task.getId());
                return task;
            }
        } catch (SQLException e) {
            // Rollback transaksi jika terjadi error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Gagal rollback transaksi", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Gagal memperbarui task dengan id: " + task.getId(), e);
            throw new RuntimeException("Gagal memperbarui task: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset autocommit
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Gagal menutup koneksi", e);
                }
            }
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                LOGGER.warning("Tidak ada task yang dihapus dengan id: " + id);
                return false;
            }
            
            LOGGER.info("Task berhasil dihapus dengan id: " + id);
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal menghapus task dengan id: " + id, e);
            return false;
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM tasks";
        
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            
            int affectedRows = stmt.executeUpdate(sql);
            LOGGER.info("Semua task berhasil dihapus. Jumlah baris: " + affectedRows);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal menghapus semua task", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal memeriksa keberadaan task dengan id: " + id, e);
        }
        
        return false;
    }

    /**
     * Memetakan ResultSet ke Task object
     * 
     * @param rs ResultSet
     * @return Task
     * @throws SQLException jika terjadi error
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getString("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        return task;
    }
} 