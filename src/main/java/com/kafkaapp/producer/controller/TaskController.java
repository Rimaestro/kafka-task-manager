package com.kafkaapp.producer.controller;

import com.kafkaapp.common.model.Task;
import com.kafkaapp.producer.repository.InMemoryTaskRepository;
import com.kafkaapp.common.repository.TaskRepository;
import com.kafkaapp.producer.service.KafkaProducerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller untuk mengelola operasi CRUD pada Task
 */
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    private final TaskRepository taskRepository;
    private final KafkaProducerService kafkaProducerService;
    private final List<Consumer<List<Task>>> listeners = new ArrayList<>();
    
    public TaskController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.taskRepository = InMemoryTaskRepository.getInstance();
        logger.info("TaskController diinisialisasi dengan InMemoryTaskRepository");
    }
    
    /**
     * Tambahkan listener untuk perubahan data
     * 
     * @param listener Consumer yang akan dipanggil ketika data berubah
     */
    public void addChangeListener(Consumer<List<Task>> listener) {
        listeners.add(listener);
        // Panggil listener dengan data saat ini
        listener.accept(getAllTasks());
    }
    
    /**
     * Notifikasi semua listener tentang perubahan data
     */
    private void notifyListeners() {
        List<Task> taskList = getAllTasks();
        for (Consumer<List<Task>> listener : listeners) {
            listener.accept(taskList);
        }
    }
    
    /**
     * Mendapatkan semua task
     * 
     * @return List dari semua task
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    /**
     * Mendapatkan task berdasarkan ID
     * 
     * @param id ID task
     * @return Task jika ditemukan, null jika tidak
     */
    public Task getTaskById(String id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    /**
     * Membuat task baru
     * 
     * @param title Judul task
     * @param description Deskripsi task
     * @return Task yang dibuat
     */
    public Task createTask(String title, String description) {
        Task task = new Task(title, description);
        
        // Simpan di memory
        task = taskRepository.save(task);
        
        // Kirim event ke Kafka
        kafkaProducerService.sendCreateEvent(task);
        
        // Notifikasi listener
        notifyListeners();
        
        return task;
    }
    
    /**
     * Mengupdate task yang ada
     * 
     * @param id ID task
     * @param title Judul baru
     * @param description Deskripsi baru
     * @param status Status baru
     * @return Task yang diupdate, null jika tidak ditemukan
     */
    public Task updateTask(String id, String title, String description, Task.TaskStatus status) {
        Task task = getTaskById(id);
        if (task == null) {
            return null;
        }
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        
        // Simpan update di memory
        task = taskRepository.save(task);
        
        // Kirim event ke Kafka
        kafkaProducerService.sendUpdateEvent(task);
        
        // Notifikasi listener
        notifyListeners();
        
        return task;
    }
    
    /**
     * Menghapus task
     * 
     * @param id ID task
     * @return true jika berhasil dihapus, false jika tidak
     */
    public boolean deleteTask(String id) {
        Task task = getTaskById(id);
        if (task == null) {
            return false;
        }
        
        // Hapus dari memory
        boolean deleted = taskRepository.deleteById(id);
        if (!deleted) {
            return false;
        }
        
        // Kirim event ke Kafka
        kafkaProducerService.sendDeleteEvent(task);
        
        // Notifikasi listener
        notifyListeners();
        
        return true;
    }
} 