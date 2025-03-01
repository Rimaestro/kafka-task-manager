package com.kafkaapp.common.repository;

import com.kafkaapp.common.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * Interface repository untuk operasi CRUD Task
 */
public interface TaskRepository {
    
    /**
     * Mendapatkan semua task dari database
     * 
     * @return List dari task
     */
    List<Task> findAll();
    
    /**
     * Mencari task berdasarkan ID
     * 
     * @param id ID task
     * @return Optional berisi task jika ditemukan
     */
    Optional<Task> findById(String id);
    
    /**
     * Menyimpan task baru
     * 
     * @param task Task yang akan disimpan
     * @return Task yang disimpan (dengan ID jika baru)
     */
    Task save(Task task);
    
    /**
     * Menghapus task berdasarkan ID
     * 
     * @param id ID task
     * @return true jika berhasil dihapus
     */
    boolean deleteById(String id);
    
    /**
     * Menghapus semua task
     */
    void deleteAll();
    
    /**
     * Memeriksa apakah task dengan ID tertentu ada
     * 
     * @param id ID task
     * @return true jika task ada
     */
    boolean existsById(String id);
} 