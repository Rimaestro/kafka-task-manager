package com.kafkaapp.producer.view;

import com.kafkaapp.common.model.Task;
import com.kafkaapp.producer.controller.TaskController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Tampilan utama untuk aplikasi Producer
 */
public class ProducerView {
    private final TaskController taskController;
    private final Stage primaryStage;
    
    private TableView<Task> taskTable;
    private ObservableList<Task> taskData = FXCollections.observableArrayList();
    
    // Format untuk tampilan tanggal
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    
    public ProducerView(TaskController taskController, Stage primaryStage) {
        this.taskController = taskController;
        this.primaryStage = primaryStage;
        
        // Register untuk memperbarui tampilan ketika data berubah
        this.taskController.addChangeListener(this::updateTaskList);
    }
    
    /**
     * Perbarui daftar task di tabel
     * 
     * @param tasks List task yang akan ditampilkan
     */
    private void updateTaskList(List<Task> tasks) {
        Platform.runLater(() -> {
            taskData.clear();
            taskData.addAll(tasks);
        });
    }
    
    /**
     * Membangun dan menampilkan UI
     */
    public void show() {
        primaryStage.setTitle("Kafka Producer - Task Manager");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // Header
        VBox headerBox = createHeader();
        root.setTop(headerBox);
        
        // Tabel Tasks
        taskTable = createTaskTable();
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(10));
        tableContainer.getChildren().add(taskTable);
        
        // Tombol aksi
        HBox actionButtons = createActionButtons();
        tableContainer.getChildren().add(actionButtons);
        
        root.setCenter(tableContainer);
        
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
        
        Label titleLabel = new Label("Task Manager");
        titleLabel.getStyleClass().add("title-label");
        
        Label subtitleLabel = new Label("Producer Application");
        subtitleLabel.getStyleClass().add("header-label");
        
        headerBox.getChildren().addAll(titleLabel, subtitleLabel, new Separator());
        return headerBox;
    }
    
    /**
     * Buat tabel task
     */
    private TableView<Task> createTaskTable() {
        TableView<Task> tableView = new TableView<>();
        tableView.setItems(taskData);
        tableView.setPlaceholder(createEmptyTablePlaceholder());
        
        // Kolom ID
        TableColumn<Task, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(220);
        
        // Kolom Judul
        TableColumn<Task, String> titleColumn = new TableColumn<>("Judul");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(200);
        titleColumn.setCellFactory(column -> new TableCell<Task, String>() {
            private Text text;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (text == null) {
                        text = new Text();
                        text.setWrappingWidth(180);
                    }
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        
        // Kolom Deskripsi
        TableColumn<Task, String> descColumn = new TableColumn<>("Deskripsi");
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descColumn.setPrefWidth(250);
        descColumn.setCellFactory(column -> new TableCell<Task, String>() {
            private Text text;
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (text == null) {
                        text = new Text();
                        text.setWrappingWidth(230);
                    }
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        
        // Kolom Status
        TableColumn<Task, Task.TaskStatus> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(120);
        statusColumn.setCellFactory(column -> new TableCell<Task, Task.TaskStatus>() {
            @Override
            protected void updateItem(Task.TaskStatus item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label();
                    
                    switch (item) {
                        case TODO:
                            statusLabel.setText("TODO");
                            statusLabel.getStyleClass().add("status-todo");
                            break;
                        case IN_PROGRESS:
                            statusLabel.setText("IN PROGRESS");
                            statusLabel.getStyleClass().add("status-in-progress");
                            break;
                        case DONE:
                            statusLabel.setText("DONE");
                            statusLabel.getStyleClass().add("status-done");
                            break;
                    }
                    
                    setGraphic(statusLabel);
                }
            }
        });
        
        // Kolom Tanggal Dibuat
        TableColumn<Task, String> createdAtColumn = new TableColumn<>("Dibuat Pada");
        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        createdAtColumn.setPrefWidth(150);
        
        // Menambahkan kolom satu per satu untuk menghindari peringatan type safety
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(titleColumn);
        tableView.getColumns().add(descColumn);
        tableView.getColumns().add(statusColumn);
        tableView.getColumns().add(createdAtColumn);
        
        // Memuat data awal
        taskController.getAllTasks();
        
        return tableView;
    }
    
    /**
     * Membuat placeholder untuk tabel kosong
     */
    private Node createEmptyTablePlaceholder() {
        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.getStyleClass().add("empty-table-placeholder");
        
        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label messageLabel = new Label("Belum ada task yang ditambahkan");
        messageLabel.setStyle("-fx-font-weight: bold;");
        
        Label subMessageLabel = new Label("Klik tombol 'Tambah Task' untuk membuat task baru");
        
        placeholder.getChildren().addAll(iconLabel, messageLabel, subMessageLabel);
        return placeholder;
    }
    
    /**
     * Buat tombol aksi
     */
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button addButton = new Button("Tambah Task");
        addButton.setGraphic(createIcon("➕", "12px"));
        addButton.setOnAction(e -> showAddTaskDialog());
        
        Button editButton = new Button("Edit Task");
        editButton.setGraphic(createIcon("✏️", "12px"));
        editButton.setOnAction(e -> {
            Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                showEditTaskDialog(selectedTask);
            } else {
                showAlert("Tidak Ada Task Dipilih", "Silakan pilih task yang ingin diedit.", Alert.AlertType.WARNING);
            }
        });
        
        Button deleteButton = new Button("Hapus Task");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setGraphic(createIcon("🗑️", "12px"));
        deleteButton.setOnAction(e -> {
            Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                showDeleteConfirmationDialog(selectedTask);
            } else {
                showAlert("Tidak Ada Task Dipilih", "Silakan pilih task yang ingin dihapus.", Alert.AlertType.WARNING);
            }
        });
        
        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);
        return buttonBox;
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
     * Tampilkan dialog untuk menambah task
     */
    private void showAddTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Tambah Task Baru");
        dialog.setHeaderText("Masukkan detail task baru");
        
        // Set tombol
        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Buat form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Judul Task");
        titleField.setPrefWidth(300);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Deskripsi");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        
        Label titleLabel = new Label("Judul:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label descLabel = new Label("Deskripsi:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descriptionArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/modern-style.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(500);
        
        // Request focus pada judul
        Platform.runLater(titleField::requestFocus);
        
        // Convert hasil dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText();
                String description = descriptionArea.getText();
                
                if (title.isEmpty()) {
                    showAlert("Judul Kosong", 
                             "Judul task tidak boleh kosong.", 
                             Alert.AlertType.ERROR);
                    return null;
                }
                
                return taskController.createTask(title, description);
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Tampilkan dialog untuk mengedit task
     */
    private void showEditTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit detail task");
        
        // Set tombol
        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Buat form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        TextField titleField = new TextField(task.getTitle());
        titleField.setPrefWidth(300);
        
        TextArea descriptionArea = new TextArea(task.getDescription());
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);
        
        ComboBox<Task.TaskStatus> statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList(Task.TaskStatus.values()));
        statusComboBox.setValue(task.getStatus());
        statusComboBox.setPrefWidth(300);
        
        Label titleLabel = new Label("Judul:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label descLabel = new Label("Deskripsi:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(descLabel, 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(statusLabel, 0, 2);
        grid.add(statusComboBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/modern-style.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(500);
        
        // Request focus pada judul
        Platform.runLater(titleField::requestFocus);
        
        // Convert hasil dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String title = titleField.getText();
                String description = descriptionArea.getText();
                Task.TaskStatus status = statusComboBox.getValue();
                
                if (title.isEmpty()) {
                    showAlert("Judul Kosong", 
                             "Judul task tidak boleh kosong.", 
                             Alert.AlertType.ERROR);
                    return null;
                }
                
                return taskController.updateTask(task.getId(), title, description, status);
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Tampilkan konfirmasi hapus task
     */
    private void showDeleteConfirmationDialog(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Task");
        alert.setContentText("Apakah Anda yakin ingin menghapus task: " + task.getTitle() + "?");
        
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/modern-style.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskController.deleteTask(task.getId());
        }
    }
    
    /**
     * Tampilkan alert
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/modern-style.css").toExternalForm());
        
        alert.showAndWait();
    }
} 