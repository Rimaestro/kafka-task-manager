package com.kafkaapp.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Konfigurasi koneksi database
 */
public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static HikariDataSource dataSource;

    private DatabaseConfig() {
        // Private constructor to prevent instantiation
    }

    /**
     * Mendapatkan dataSource untuk koneksi database
     * 
     * @return HikariDataSource instance
     */
    public static synchronized HikariDataSource getDataSource() {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource;
    }

    /**
     * Inisialisasi dataSource dari file konfigurasi
     */
    private static void initializeDataSource() {
        try {
            Properties properties = loadDatabaseProperties();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));
            config.setDriverClassName(properties.getProperty("db.driver"));
            
            // Konfigurasi pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(30000);
            
            dataSource = new HikariDataSource(config);
            LOGGER.info("Database connection pool initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize datasource", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Memuat properti database dari file application.properties
     * 
     * @return Properties object
     * @throws IOException jika file tidak ditemukan
     */
    private static Properties loadDatabaseProperties() throws IOException {
        Properties properties = new Properties();
        
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                throw new IOException("Unable to find application.properties");
            }
            
            properties.load(input);
            return properties;
        }
    }

    /**
     * Menutup dataSource
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Database connection pool closed");
        }
    }
} 