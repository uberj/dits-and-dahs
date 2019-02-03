package com.example.uberj.test1.storage.converters;

import android.arch.persistence.room.TypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class StringListConverter {
    private static final ObjectMapper mapper = new ObjectMapper();

    @TypeConverter
    public static List<String> toList(String value) {
        try {
            return value == null ? null : mapper.readValue(value, new TypeReference<List<String>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TypeConverter
    public static String toString(List<String> value) {
        try {
            return value == null ? null : mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
