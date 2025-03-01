package com.kafkaapp.consumer.view;

import com.kafkaapp.common.model.Task;
import com.kafkaapp.common.model.TaskEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

/**
 * Tampilan utama untuk aplikasi Consumer
 */
public class ConsumerView {
    private final Stage primaryStage;
    
    private ListView<String> activityLogListView;
    private ObservableList<String> activityLogs = FXCollections.observableArrayList();
    
    // Format untuk tampilan tanggal
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    
    public ConsumerView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Membangun dan menampilkan UI
     */
    public void show() {
        primaryStage.setTitle("Kafka Consumer - Task Event Monitor");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // Header
        VBox headerBox = createHeader();
        root.setTop(headerBox);
        
        // Log activity area
        VBox logContainer = createLogContainer();
        root.setCenter(logContainer);
        
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/modern-style.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Buat header aplikasi
     */
    private VBox createHeader() {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(20, 20, 10, 20));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Task Event Monitor");
        titleLabel.getStyleClass().add("title-label");
        
        Label subtitleLabel = new Label("Consumer Application");
        subtitleLabel.getStyleClass().add("header-label");
        
        headerBox.getChildren().addAll(titleLabel, subtitleLabel, new Separator());
        return headerBox;
    }
    
    /**
     * Buat container untuk log aktivitas
     */
    private VBox createLogContainer() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("card");
        
        Label logHeaderLabel = new Label("Aktivitas Task");
        logHeaderLabel.getStyleClass().add("header-label");
        
        // Membuat ListView dengan custom cell factory untuk styling
        activityLogListView = new ListView<>(activityLogs);
        activityLogListView.setPrefHeight(450);
        activityLogListView.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Menerapkan style berdasarkan jenis log
                    if (item.contains("[INFO]")) {
                        getStyleClass().add("info-log");
                    } else if (item.contains("[ERROR]")) {
                        getStyleClass().add("error-log");
                    } else if (item.contains("CREATE")) {
                        getStyleClass().add("create-log");
                    } else if (item.contains("UPDATE")) {
                        getStyleClass().add("update-log");
                    } else if (item.contains("DELETE")) {
                        getStyleClass().add("delete-log");
                    }
                }
            }
        });
        
        // Panel tombol
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        
        Button clearButton = new Button("Bersihkan Log");
        clearButton.setGraphic(createIcon("🧹", "12px"));
        clearButton.setOnAction(e -> activityLogs.clear());
        
        buttonPanel.getChildren().add(clearButton);
        
        container.getChildren().addAll(logHeaderLabel, activityLogListView, buttonPanel);
        return container;
    }
    
    /**
     * Membuat icon untuk tombol
     */
    private Label createIcon(String text, String fontSize) {
        Label icon = new Label(text);
        icon.setStyle("-fx-font-size: " + fontSize + ";");
        return icon;
    }
    
    /**
     * Tambahkan event task ke log aktivitas
     */
    public void addTaskEvent(TaskEvent event) {
        Platform.runLater(() -> {
            StringBuilder logBuilder = new StringBuilder();
            String timestamp = event.getEventTime().format(dateFormatter);
            
            // Menambahkan ikon berdasarkan jenis event
            String eventIcon = "";
            switch (event.getEventType()) {
                case CREATE:
                    eventIcon = "➕ ";
                    break;
                case UPDATE:
                    eventIcon = "✏️ ";
                    break;
                case DELETE:
                    eventIcon = "🗑️ ";
                    break;
            }
            
            logBuilder.append(timestamp)
                    .append(" | ")
                    .append(eventIcon)
                    .append(event.getEventType())
                    .append(" | Task: ");
            
            Task task = event.getTask();
            if (task != null) {
                logBuilder.append("ID: ").append(task.getId().substring(0, 8)).append("... | ");
                logBuilder.append("Judul: ").append(task.getTitle()).append(" | ");
                
                if (event.getEventType() != TaskEvent.EventType.DELETE) {
                    logBuilder.append("Status: ").append(task.getStatus());
                }
            }
            
            activityLogs.add(0, logBuilder.toString());
            
            // Batasi jumlah log yang ditampilkan (opsional)
            if (activityLogs.size() > 100) {
                activityLogs.remove(100, activityLogs.size());
            }
        });
    }
    
    /**
     * Tambahkan pesan informasi ke log
     */
    public void addInfoMessage(String message) {
        addLogMessage("[INFO] " + message);
    }
    
    /**
     * Tambahkan pesan error ke log
     */
    public void addErrorMessage(String message) {
        addLogMessage("[ERROR] " + message);
    }
    
    /**
     * Tambahkan pesan ke log dengan timestamp
     */
    private void addLogMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalDateTime.now().format(dateFormatter);
            activityLogs.add(0, timestamp + " | " + message);
        });
    }
}