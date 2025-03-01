package com.kafkaapp.consumer;

import com.kafkaapp.common.config.DatabaseConfig;
import com.kafkaapp.common.model.Task;
import com.kafkaapp.common.model.TaskEvent;
import com.kafkaapp.common.repository.MySqlTaskRepository;
import com.kafkaapp.common.repository.TaskRepository;
import com.kafkaapp.consumer.service.KafkaConsumerService;
import com.kafkaapp.consumer.view.ConsumerView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aplikasi utama Consumer
 */
public class ConsumerApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerApp.class);
    
    private KafkaConsumerService kafkaConsumerService;
    private ConsumerView consumerView;
    private TaskRepository taskRepository;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread: " + thread.getName(), throwable);
            Platform.runLater(() -> showErrorAlert(throwable));
        });
        
        try {
            logger.info("Starting Consumer Application");
            
            // Inisialisasi repository
            taskRepository = MySqlTaskRepository.getInstance();
            
            // Inisialisasi View
            consumerView = new ConsumerView(primaryStage);
            consumerView.show();
            
            // Inisialisasi Kafka Consumer Service dengan handler untuk task events
            kafkaConsumerService = new KafkaConsumerService(
                    "task-consumer-group",
                    this::handleTaskEvent);
            
            // Tambahkan pesan info ke log
            consumerView.addInfoMessage("Aplikasi Consumer dimulai, berlangganan ke topik task-events...");
            consumerView.addInfoMessage("Consumer akan menyimpan semua event ke database");
            
            // Mulai consumer
            kafkaConsumerService.start();
            
            logger.info("Consumer Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting Consumer Application", e);
            showErrorAlert(e);
        }
    }
    
    /**
     * Menangani event task yang diterima dari Kafka dan menyimpannya ke database
     */
    private void handleTaskEvent(TaskEvent event) {
        logger.info("Received task event: {}", event);
        
        try {
            Task task = event.getTask();
            
            switch (event.getEventType()) {
                case CREATE:
                    try {
                        taskRepository.save(task);
                        logger.info("Task created in database: {}", task.getId());
                        consumerView.addInfoMessage(String.format("Database: Berhasil membuat task dengan ID %s", task.getId()));
                    } catch (Exception e) {
                        logger.error("Gagal membuat task di database", e);
                        consumerView.addErrorMessage(String.format("Gagal membuat task: %s - %s", task.getId(), e.getMessage()));
                        throw e;
                    }
                    break;
                case UPDATE:
                    try {
                        taskRepository.save(task);
                        logger.info("Task updated in database: {}", task.getId());
                        consumerView.addInfoMessage(String.format("Database: Berhasil memperbarui task dengan ID %s", task.getId()));
                    } catch (Exception e) {
                        logger.error("Gagal memperbarui task di database", e);
                        consumerView.addErrorMessage(String.format("Gagal memperbarui task: %s - %s", task.getId(), e.getMessage()));
                        throw e;
                    }
                    break;
                case DELETE:
                    try {
                        boolean deleted = taskRepository.deleteById(task.getId());
                        if (deleted) {
                            logger.info("Task deleted from database: {}", task.getId());
                            consumerView.addInfoMessage(String.format("Database: Berhasil menghapus task dengan ID %s", task.getId()));
                        } else {
                            logger.warn("Task tidak ditemukan untuk dihapus: {}", task.getId());
                            consumerView.addErrorMessage(String.format("Task dengan ID %s tidak ditemukan untuk dihapus", task.getId()));
                        }
                    } catch (Exception e) {
                        logger.error("Gagal menghapus task dari database", e);
                        consumerView.addErrorMessage(String.format("Gagal menghapus task: %s - %s", task.getId(), e.getMessage()));
                        throw e;
                    }
                    break;
            }
            
            // Tambahkan event ke view
            consumerView.addTaskEvent(event);
            
        } catch (Exception e) {
            logger.error("Error processing task event", e);
            consumerView.addErrorMessage("Error processing task event: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        try {
            if (kafkaConsumerService != null) {
                kafkaConsumerService.stop();
            }
            
            // Tutup koneksi database
            DatabaseConfig.closeDataSource();
            
            logger.info("Consumer Application stopped");
        } catch (Exception e) {
            logger.error("Error stopping Consumer Application", e);
        }
    }
    
    private void showErrorAlert(Throwable throwable) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(throwable.getMessage());
        alert.showAndWait();
    }
} 