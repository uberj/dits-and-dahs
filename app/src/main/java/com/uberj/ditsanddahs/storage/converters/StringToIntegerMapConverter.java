package com.uberj.ditsanddahs.storage.converters;

import androidx.room.TypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

// Can this be done with generics?
public class StringToIntegerMapConverter {
    private static final ObjectMapper mapper = new ObjectMapper();

    @TypeConverter
    public static Map<String, Integer> toMap(String value) {
        try {
            return value == null ? null : mapper.readValue(value, new TypeReference<Map<String, Integer>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TypeConverter
    public static String toString(Map<String, Integer> value) {
        try {
            return value == null ? null : mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
