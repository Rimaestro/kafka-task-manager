package com.kafkaapp.producer.service;

import com.kafkaapp.common.model.Task;
import com.kafkaapp.common.model.TaskEvent;
import com.kafkaapp.common.utils.JsonUtils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Service untuk mengirim pesan ke Kafka
 */
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "task-events";
    
    private final Producer<String, String> producer;
    
    /**
     * Constructor dengan konfigurasi default
     */
    public KafkaProducerService() {
        this("localhost:9092");
    }
    
    /**
     * Constructor dengan bootstrap server kustom
     * 
     * @param bootstrapServers URL Kafka bootstrap servers
     */
    public KafkaProducerService(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        this.producer = new KafkaProducer<>(props);
        logger.info("Kafka Producer initialized with bootstrap servers: {}", bootstrapServers);
    }
    
    /**
     * Kirim event task ke Kafka
     * 
     * @param event TaskEvent yang akan dikirim
     * @return true jika berhasil, false jika gagal
     */
    public boolean sendTaskEvent(TaskEvent event) {
        try {
            String key = event.getTask().getId();
            String value = JsonUtils.toJson(event);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);
            
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Message sent successfully to topic: {}, partition: {}, offset: {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                } else {
                    logger.error("Failed to send message to Kafka", exception);
                }
            }).get(); // Menunggu pengiriman selesai
            
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error sending task event to Kafka", e);
            return false;
        }
    }
    
    /**
     * Kirim event CREATE
     * 
     * @param task Task yang akan dibuat
     * @return true jika berhasil, false jika gagal
     */
    public boolean sendCreateEvent(Task task) {
        TaskEvent event = new TaskEvent(TaskEvent.EventType.CREATE, task);
        return sendTaskEvent(event);
    }
    
    /**
     * Kirim event UPDATE
     * 
     * @param task Task yang akan diupdate
     * @return true jika berhasil, false jika gagal
     */
    public boolean sendUpdateEvent(Task task) {
        TaskEvent event = new TaskEvent(TaskEvent.EventType.UPDATE, task);
        return sendTaskEvent(event);
    }
    
    /**
     * Kirim event DELETE
     * 
     * @param task Task yang akan dihapus
     * @return true jika berhasil, false jika gagal
     */
    public boolean sendDeleteEvent(Task task) {
        TaskEvent event = new TaskEvent(TaskEvent.EventType.DELETE, task);
        return sendTaskEvent(event);
    }
    
    /**
     * Close producer
     */
    public void close() {
        if (producer != null) {
            producer.close();
            logger.info("Kafka Producer closed");
        }
    }
} 