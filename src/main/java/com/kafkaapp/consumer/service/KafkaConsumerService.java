package com.kafkaapp.consumer.service;

import com.kafkaapp.common.model.TaskEvent;
import com.kafkaapp.common.utils.JsonUtils;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Service untuk menerima event task dari Kafka
 */
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String TOPIC = "task-events";
    
    private final KafkaConsumer<String, String> consumer;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Consumer<TaskEvent> eventHandler;
    
    /**
     * Constructor dengan konfigurasi default
     *
     * @param groupId Group ID untuk Kafka Consumer
     * @param eventHandler Handler untuk memproses TaskEvent yang diterima
     */
    public KafkaConsumerService(String groupId, Consumer<TaskEvent> eventHandler) {
        this("localhost:9092", groupId, eventHandler);
    }
    
    /**
     * Constructor dengan bootstrap server kustom
     *
     * @param bootstrapServers URL Kafka bootstrap servers
     * @param groupId Group ID untuk Kafka Consumer
     * @param eventHandler Handler untuk memproses TaskEvent yang diterima
     */
    public KafkaConsumerService(String bootstrapServers, String groupId, Consumer<TaskEvent> eventHandler) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        this.consumer = new KafkaConsumer<>(props);
        this.executorService = Executors.newSingleThreadExecutor();
        this.eventHandler = eventHandler;
        
        logger.info("Kafka Consumer initialized with bootstrap servers: {}, group ID: {}", 
                bootstrapServers, groupId);
    }
    
    /**
     * Mulai consumer untuk berlangganan dan memproses pesan
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            executorService.submit(this::consumeMessages);
            logger.info("Kafka Consumer started");
        }
    }
    
    /**
     * Loop utama untuk konsumsi pesan
     */
    private void consumeMessages() {
        try {
            consumer.subscribe(Collections.singletonList(TOPIC));
            logger.info("Subscribed to topic: {}", TOPIC);
            
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Received message: topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());
                    
                    try {
                        TaskEvent taskEvent = JsonUtils.fromJson(record.value(), TaskEvent.class);
                        eventHandler.accept(taskEvent);
                    } catch (Exception e) {
                        logger.error("Error processing Kafka message", e);
                    }
                }
            }
        } catch (WakeupException e) {
            // Ignore, this is expected when closing the consumer
            if (running.get()) {
                logger.error("Unexpected WakeupException", e);
            }
        } catch (Exception e) {
            logger.error("Error in Kafka consumer loop", e);
        } finally {
            consumer.close();
            logger.info("Kafka Consumer closed");
            running.set(false);
        }
    }
    
    /**
     * Stop consumer
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            consumer.wakeup();
            executorService.shutdown();
            logger.info("Kafka Consumer stopping");
        }
    }
} 