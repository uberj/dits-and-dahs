package com.uberj.pocketmorsepro.storage.converters;

import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticEngineEvent;

import androidx.room.TypeConverter;

public class EventTypeConverter {
    @TypeConverter
    public static SocraticEngineEvent.EventType getType(Integer numeral){
        for(SocraticEngineEvent.EventType ds : SocraticEngineEvent.EventType.values()){
            if(ds.code == numeral){
                return ds;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer getTypeInt(SocraticEngineEvent.EventType status){

        if(status != null)
            return status.code;

        return  null;
    }
}
