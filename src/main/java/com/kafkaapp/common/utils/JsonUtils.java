package com.kafkaapp.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class untuk JSON serialization dan deserialization
 */
public class JsonUtils {
    
    private static final Gson gson;
    
    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        
        // Register adapter untuk LocalDateTime
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        
        gson = gsonBuilder.setPrettyPrinting().create();
    }
    
    /**
     * Serialize objek ke JSON string
     *
     * @param object Objek yang akan dikonversi ke JSON
     * @return JSON string
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }
    
    /**
     * Deserialize JSON string ke objek
     *
     * @param json JSON string
     * @param clazz Class dari objek target
     * @param <T> Tipe objek
     * @return Objek hasil deserialisasi
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
    
    /**
     * Adapter untuk serialisasi dan deserialisasi LocalDateTime
     */
    static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }
        
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
} 