package com.uberj.ditsanddahs.storage.converters;

import com.uberj.ditsanddahs.flashcard.storage.FlashcardEngineEvent;

import androidx.room.TypeConverter;

public class FlashcardEventTypeConverter {
    @TypeConverter
    public static FlashcardEngineEvent.EventType getType(Integer numeral){
        for(FlashcardEngineEvent.EventType ds : FlashcardEngineEvent.EventType.values()){
            if(ds.code == numeral){
                return ds;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer getTypeInt(FlashcardEngineEvent.EventType status){

        if(status != null)
            return status.code;

        return  null;
    }
}
