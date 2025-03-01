package com.kafkaapp.producer;

import com.kafkaapp.producer.controller.TaskController;
import com.kafkaapp.producer.service.KafkaProducerService;
import com.kafkaapp.producer.view.ProducerView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aplikasi utama Producer
 */
public class ProducerApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ProducerApp.class);
    
    private KafkaProducerService kafkaProducerService;
    
    public static void main(String[] args) {
        Application.launch(ProducerApp.class, args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread: " + thread.getName(), throwable);
            Platform.runLater(() -> showErrorAlert(throwable));
        });
        
        try {
            logger.info("Starting Producer Application");
            
            // Inisialisasi Kafka Producer
            kafkaProducerService = new KafkaProducerService();
            
            // Inisialisasi Controller
            TaskController taskController = new TaskController(kafkaProducerService);
            
            // Inisialisasi View
            ProducerView producerView = new ProducerView(taskController, primaryStage);
            producerView.show();
            
            logger.info("Producer Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting Producer Application", e);
            showErrorAlert(e);
        }
    }
    
    @Override
    public void stop() {
        try {
            // Tutup koneksi Kafka
            if (kafkaProducerService != null) {
                kafkaProducerService.close();
            }
            
            logger.info("Producer Application stopped");
        } catch (Exception e) {
            logger.error("Error stopping Producer Application", e);
        }
    }
    
    private void showErrorAlert(Throwable throwable) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Terjadi kesalahan");
        alert.setContentText("Aplikasi mengalami kesalahan: " + throwable.getMessage());
        
        alert.showAndWait();
    }
}