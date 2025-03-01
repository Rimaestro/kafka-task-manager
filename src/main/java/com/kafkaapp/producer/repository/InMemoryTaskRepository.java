package com.kafkaapp.producer.repository;

import com.kafkaapp.common.model.Task;
import com.kafkaapp.common.repository.TaskRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementasi TaskRepository menggunakan penyimpanan di memory
 */
public class InMemoryTaskRepository implements TaskRepository {
    private static InMemoryTaskRepository instance;
    private final Map<String, Task> taskMap = new ConcurrentHashMap<>();
    
    private InMemoryTaskRepository() {
        // Private constructor untuk singleton
    }
    
    /**
     * Mendapatkan instance singleton dari repository
     * 
     * @return Singleton instance dari InMemoryTaskRepository
     */
    public static synchronized InMemoryTaskRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryTaskRepository();
        }
        return instance;
    }
    
    @Override
    public List<Task> findAll() {
        return new ArrayList<>(taskMap.values());
    }
    
    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(taskMap.get(id));
    }
    
    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            task.setId(UUID.randomUUID().toString());
        }
        
        // Buat salinan dari task untuk mencegah perubahan dari luar
        Task taskCopy = new Task(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
        
        taskMap.put(taskCopy.getId(), taskCopy);
        return taskCopy;
    }
    
    @Override
    public boolean deleteById(String id) {
        return taskMap.remove(id) != null;
    }
    
    @Override
    public void deleteAll() {
        taskMap.clear();
    }
    
    @Override
    public boolean existsById(String id) {
        return taskMap.containsKey(id);
    }
}